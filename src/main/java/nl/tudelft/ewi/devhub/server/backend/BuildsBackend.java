package nl.tudelft.ewi.devhub.server.backend;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.build.client.BuildServerBackend;
import nl.tudelft.ewi.build.client.BuildServerBackendImpl;
import nl.tudelft.ewi.build.jaxrs.models.BuildRequest;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildServers;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.BuildServer;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;

/**
 * The {@link BuildServerClient} allows you to query and manipulate data from the build-server.
 */
@Slf4j
public class BuildsBackend {

	private final BuildServers buildServers;
	private final Provider<BuildSubmitter> submitters;
	private final ConcurrentLinkedQueue<BuildRequestWithResultMapping> buildQueue;
	private final ScheduledExecutorService executor;
	private final AtomicBoolean running;

	@Inject
	BuildsBackend(BuildServers buildServers, Provider<BuildSubmitter> submitters) {
		this.buildServers = buildServers;
		this.submitters = submitters;
		this.buildQueue = Queues.newConcurrentLinkedQueue();
		this.executor = new ScheduledThreadPoolExecutor(1);
		this.running = new AtomicBoolean(false);
	}
	
	public List<BuildServer> listActiveBuildServers() {
		return buildServers.listAll();
	}
	
	public boolean authenticate(String name, String secret) {
		try {
			buildServers.findByCredentials(name, secret);
			return true;
		}
		catch (NotFoundException e) {
			return false;
		}
	}
	
	public void addBuildServer(BuildServer server) throws ApiError {
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
	
	public void offerBuild(BuildRequest request, long buildResultId) {
		synchronized (running) {
			buildQueue.offer(new BuildRequestWithResultMapping(request, buildResultId));
			if (running.compareAndSet(false, true)) {
				BuildSubmitter task = submitters.get();
				task.initialize(this);
				executor.submit(task);
			}
		}
	}
	
	public void shutdown() throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.MINUTES);
	}
	
	@Data
	private static class BuildRequestWithResultMapping {
		private final BuildRequest request;
		private final long id;
	}
	
	private static class BuildSubmitter extends RunnableInUnitOfWork {
		
		private static final int NO_CAPACITY_DELAY = 5000;
		
		private final Provider<BuildServers> buildServersProvider;
		private final Provider<BuildResults> buildResultsProvider;
		
		private BuildsBackend backend;

		@Inject
		BuildSubmitter(Provider<BuildServers> buildServersProvider, 
				Provider<BuildResults> buildResultsProvider, 
				Provider<UnitOfWork> workProvider) {
			
			super(workProvider);
			this.buildServersProvider = buildServersProvider;
			this.buildResultsProvider = buildResultsProvider;
		}
		
		void initialize(BuildsBackend backend) {
			this.backend = backend;
		}

		@Override
		@Transactional
		protected void runInUnitOfWork() {
			ConcurrentLinkedQueue<BuildRequestWithResultMapping> buildQueue = backend.buildQueue;
			AtomicBoolean running = backend.running;
			BuildServers buildServers = buildServersProvider.get();
			BuildResults buildResults = buildResultsProvider.get();
			
			try {
				while (!buildQueue.isEmpty()) {
					boolean delivered = false;
					BuildRequestWithResultMapping buildRequestWithId = buildQueue.peek();
					List<BuildServer> allBuildServers = buildServers.listAll();
					
					while (!allBuildServers.isEmpty()) {
						BuildServer buildServer = allBuildServers.remove(0);
						String host = buildServer.getHost();
						String name = buildServer.getName();
						String secret = buildServer.getSecret();
						
						BuildServerBackend backend = new BuildServerBackendImpl(host, name, secret);
						if (backend.offerBuildRequest(buildRequestWithId.getRequest())) {
							delivered = true;
							buildQueue.poll();
							log.info("One build request was succesfully handed to: {}", name);
							
							BuildResult result = buildResults.find(buildRequestWithId.getId());
							result.setStarted(new Date());
							buildResults.merge(result);
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
	}

}
