package nl.tudelft.ewi.devhub.server.backend;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.build.client.BuildServerBackend;
import nl.tudelft.ewi.build.client.BuildServerBackendImpl;
import nl.tudelft.ewi.build.jaxrs.models.BuildRequest;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildServers;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.BuildServer;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@link BuildsBackend} allows you to query and manipulate data from the build-server.
 */
@Slf4j
public class BuildsBackend {

	private final RepositoriesApi repositoriesApi;
	private final BuildServers buildServers;
	private final Provider<BuildSubmitter> submitters;
	private final ConcurrentLinkedQueue<BuildRequest> buildQueue;
	private final ExecutorService executor;
	private final AtomicBoolean running;
	private final BuildResults buildResults;
	private final Config config;

	@Inject
	BuildsBackend(BuildServers buildServers, Provider<BuildSubmitter> submitters,
				  BuildResults buildResults, RepositoriesApi repositoriesApi, Config config, ExecutorService executor) {
		this.buildServers = buildServers;
		this.submitters = submitters;
		this.buildQueue = Queues.newConcurrentLinkedQueue();
		this.executor = executor;
		this.running = new AtomicBoolean(false);
		this.buildResults = buildResults;
		this.repositoriesApi = repositoriesApi;
		this.config = config;
	}
	
	public List<BuildServer> listActiveBuildServers() {
		return buildServers.listAll();
	}
	
	public boolean authenticate(String name, String secret) {
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(secret);
		
		try {
			buildServers.findByCredentials(name, secret);
			return true;
		}
		catch (NotFoundException e) {
			return false;
		}
	}
	
	public void addBuildServer(BuildServer server) throws ApiError {
		Preconditions.checkNotNull(server);
		
		try {
			String name = server.getName();
			Preconditions.checkArgument(name.matches("^[a-zA-Z0-9]+$"));
		}
		catch (Throwable e) {
			throw new ApiError("error.invalid-build-server-name");
		}
		
		try {
			buildServers.persist(server);
		}
		catch (Throwable e) {
			throw new ApiError("error.could-not-add-build-server");
		}
	}
	
	@Transactional
	public void deleteBuildServer(long serverId) throws ApiError {
		try {
			BuildServer server = buildServers.findById(serverId);
			buildServers.delete(server);
		}
		catch (Throwable e) {
			throw new ApiError("error.could-not-remove-build-server");
		}
	}
	
	public void offerBuild(BuildRequest request) {
		Preconditions.checkNotNull(request);
		
		synchronized (running) {
			buildQueue.offer(request);
			if (running.compareAndSet(false, true)) {
				BuildSubmitter task = submitters.get();
				task.initialize(this);
				executor.submit(task);
			}
		}
	}

	/**
	 * Build a commit
	 * @param commit
	 */
	@Transactional
	public void buildCommit(final Commit commit) {
		if (buildResults.exists(commit)) {
			return;
		}
		createBuildRequest(commit);
		buildResults.persist(BuildResult.newBuildResult(commit));
	}

	/**
	 * Rebuild a commit
	 * @param commit
	 */
	@Transactional
	public void rebuildCommit(final Commit commit) {
		BuildResult buildResult;

		try {
			buildResult = buildResults.find(commit);

			if(buildResult.getSuccess() == null) {
				// There is a build queued
				throw new IllegalArgumentException("There already is a build queued for this commit");
			}
			else {
				buildResult.setSuccess(null);
				buildResult.setLog(null);
				buildResults.merge(buildResult);
			}
		}
		catch (EntityNotFoundException e) {
			buildResult = BuildResult.newBuildResult(commit);
			buildResults.persist(buildResult);
		}
		createBuildRequest(commit);
	}

	/**
	 * Create the build request for a commit
	 * @param commit commit to be build
	 */
	@SneakyThrows
	protected void createBuildRequest(final Commit commit) {
		RepositoryEntity repositoryEntity = commit.getRepository();
		RepositoryModel repository = repositoriesApi.getRepository(repositoryEntity.getRepositoryName()).getRepositoryModel();
		BuildRequest buildRequest = repositoryEntity.getBuildInstruction().createBuildRequest(config, commit, repository);
		log.info("Submitting a build for commit: {} of repository: {}", commit, repository);
		offerBuild(buildRequest);
	}

	public void shutdown() throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.MINUTES);
	}
	
	static class BuildSubmitter extends RunnableInUnitOfWork {
		
		private static final int NO_CAPACITY_DELAY = 5000;
		
		private final Provider<BuildServers> buildServersProvider;
		
		private BuildsBackend backend;

		@Inject
		BuildSubmitter(Provider<BuildServers> buildServersProvider, Provider<UnitOfWork> workProvider) {
			super(workProvider);
			this.buildServersProvider = buildServersProvider;
		}
		
		void initialize(BuildsBackend backend) {
			this.backend = backend;
		}

		@Override
		@Transactional
		protected void runInUnitOfWork() {
			ConcurrentLinkedQueue<BuildRequest> buildQueue = backend.buildQueue;
			AtomicBoolean running = backend.running;
			BuildServers buildServers = buildServersProvider.get();
			
			try {
				while (!buildQueue.isEmpty()) {
					boolean delivered = false;
					BuildRequest buildRequest = buildQueue.peek();
					List<BuildServer> allBuildServers = buildServers.listAll();
					
					while (!allBuildServers.isEmpty()) {
						BuildServer buildServer = allBuildServers.remove(0);
						String host = buildServer.getHost();
						String name = buildServer.getName();
						String secret = buildServer.getSecret();
						
						BuildServerBackend backend = createBuildServerBackend(host, name, secret);
						if (backend.offerBuildRequest(buildRequest)) {
							delivered = true;
							buildQueue.poll();
							log.info("One build request was succesfully handed to: {}", name);
							break;
						}
					}
					
					if (!delivered) {
						try {
							log.info("All {} build servers at capacity. Queue size: {}", 
									allBuildServers.size(), buildQueue.size());
							
							Thread.sleep(NO_CAPACITY_DELAY);
						}
						catch (InterruptedException e) {
							log.warn("Sleep was interrupted...");
						}
					}
				}
			}
			finally {
				synchronized (running) {
					running.set(false);
				}
			}
		}
		
		protected BuildServerBackend createBuildServerBackend(String host, String name, String secret) {
			return new BuildServerBackendImpl(host, name, secret);
		}
		
	}

}
