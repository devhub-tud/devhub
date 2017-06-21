package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
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
import nl.tudelft.ewi.devhub.server.database.entities.builds.BuildInstructionEntity;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.NotFoundException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * The {@link BuildsBackend} allows you to query and manipulate data from the build-server.
 */
@Slf4j
@Singleton
public class BuildsBackend {

	private final Provider<RepositoriesApi> repositoriesApiProvider;
	private final Provider<BuildServers> buildServersProvider;
	private final Provider<BuildResults> buildResultsProvider;
	private final BuildSubmitter buildSubmitter;
	private final Config config;

	@Inject
	public BuildsBackend(Provider<RepositoriesApi> repositoriesApiProvider,
	                     Provider<BuildServers> buildServersProvider,
	                     Provider<BuildResults> buildResultsProvider,
	                     LifeCycle lifeCycle,
	                     BuildSubmitter buildSubmitter,
	                     Config config) {
		this.repositoriesApiProvider = repositoriesApiProvider;
		this.buildServersProvider = buildServersProvider;
		this.buildResultsProvider = buildResultsProvider;
		this.config = config;
		this.buildSubmitter = buildSubmitter;

		Thread buildSubmitterThread = new Thread(buildSubmitter, "BuildSubmitter");
		buildSubmitterThread.start();
		lifeCycle.addLifeCycleListener(new ThreadLifeCycleListener(buildSubmitterThread));
	}

	public List<BuildServer> listActiveBuildServers() {
		return buildServersProvider.get().listAll();
	}
	
	public boolean authenticate(String name, String secret) {
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(secret);
		
		try {
			buildServersProvider.get().findByCredentials(name, secret);
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
			buildServersProvider.get().persist(server);
		}
		catch (Throwable e) {
			throw new ApiError("error.could-not-add-build-server");
		}
	}
	
	@Transactional
	public void deleteBuildServer(long serverId) throws ApiError {
		try {
			BuildServer server = buildServersProvider.get().findById(serverId);
			buildServersProvider.get().delete(server);
		}
		catch (Throwable e) {
			throw new ApiError("error.could-not-remove-build-server");
		}
	}
	
	public void offerBuild(BuildRequest request) {
		Preconditions.checkNotNull(request);
		buildSubmitter.buildQueue.offer(request);
	}

	/**
	 * Build a commit
	 * @param commit
	 */
	@Transactional
	public void buildCommit(final Commit commit) {
		if (buildResultsProvider.get().exists(commit)) {
			return;
		}
		createBuildRequest(commit);
		buildResultsProvider.get().persist(BuildResult.newBuildResult(commit));
	}

	/**
	 * Rebuild a commit
	 * @param commit
	 */
	@Transactional
	public void rebuildCommit(final Commit commit) {
		BuildResult buildResult;

		try {
			buildResult = buildResultsProvider.get().find(commit);

			if(buildResult.getSuccess() == null) {
				// There is a build queued
				throw new IllegalArgumentException("There already is a build queued for this commit");
			}
			else {
				buildResult.setSuccess(null);
				buildResult.setLog(null);
				buildResultsProvider.get().merge(buildResult);
			}
		}
		catch (EntityNotFoundException e) {
			buildResult = BuildResult.newBuildResult(commit);
			buildResultsProvider.get().persist(buildResult);
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
		RepositoryModel repository = repositoriesApiProvider.get().getRepository(repositoryEntity.getRepositoryName()).getRepositoryModel();

		BuildInstructionEntity buildInstructionEntity = repositoryEntity.getBuildInstruction();
		if (buildInstructionEntity != null) {
			BuildRequest buildRequest = buildInstructionEntity.createBuildRequest(config, commit, repository);
			log.info("Submitting a build for commit: {} of repository: {}", commit, repository);
			offerBuild(buildRequest);
		}
		else {
			log.debug("Not building commit {} as there is no build instruction", commit);
		}
	}

	@Singleton
	public static class BuildSubmitter implements Runnable {
		
		private static final int NO_CAPACITY_DELAY = 5000;

		private final Provider<BuildServers> buildServersProvider;
		private final Provider<UnitOfWork> workProvider;
		private final BlockingQueue<BuildRequest> buildQueue;

		@Inject
		public BuildSubmitter(Provider<BuildServers> buildServersProvider, Provider<UnitOfWork> workProvider) {
			this.buildServersProvider = buildServersProvider;
			this.workProvider = workProvider;
			this.buildQueue = Queues.newLinkedBlockingQueue();
		}

		@Override
		@SneakyThrows
		public void run() {
			BUILDS : for (BuildRequest buildRequest; (buildRequest = buildQueue.take()) != null; ) {
				UnitOfWork unitOfWork = workProvider.get();
				unitOfWork.begin();
				int exponentialBackOff = NO_CAPACITY_DELAY;

				try {
					for (;/* multiple delivery attempts */;) {
						List<BuildServer> allBuildServers = buildServersProvider.get().listAll();
						List<BuildServer> buildServersAtCapacity = Lists.newArrayList(allBuildServers);
						Iterator<BuildServer> buildServerIterator = buildServersAtCapacity.iterator();

						while (buildServerIterator.hasNext()) {
							BuildServer buildServer = buildServerIterator.next();
							String host = buildServer.getHost();
							String name = buildServer.getName();
							String secret = buildServer.getSecret();

							BuildServerBackend backend = createBuildServerBackend(host, name, secret);
							if (backend.offerBuildRequest(buildRequest)) {
								log.info("One build request was successfully handed to: {}", name);
								continue BUILDS;
							}

							buildServerIterator.remove();
						}

						log.info("{} of {} build servers at capacity. Queue size: {}",
							buildServersAtCapacity.size(), allBuildServers.size(), buildQueue.size() + 1);

						try {
							// Sleep, but allow build result hook to wake this thread up
							synchronized (this) {
								this.wait(exponentialBackOff);
							}
							// Exponential back off, so the logs do not pollute if no build servers are available for a long time
							exponentialBackOff *= 2;
						}
						catch (InterruptedException e) {
							log.info("Woke up from sleep");
						}
					}
				}
				catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				finally {
					unitOfWork.end();
				}
			}
		}
		
		protected BuildServerBackend createBuildServerBackend(String host, String name, String secret) {
			return new BuildServerBackendImpl(host, name, secret);
		}
		
	}

	private static class ThreadLifeCycleListener extends AbstractLifeCycleListener {

		private final Thread buildSubmitterThread;

		public ThreadLifeCycleListener(Thread buildSubmitterThread) {
			this.buildSubmitterThread = buildSubmitterThread;
		}

		@Override
        public void lifeCycleStopping(LifeCycle event) {
                buildSubmitterThread.stop();
        }

	}
}
