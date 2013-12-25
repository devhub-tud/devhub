package nl.devhub.client.docker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;
import nl.devhub.client.docker.models.Container;
import nl.devhub.client.docker.models.ContainerStart;
import nl.devhub.client.docker.models.Identifiable;
import nl.devhub.client.docker.models.LxcConf;
import nl.devhub.client.docker.models.StatusCode;
import nl.devhub.client.settings.Model.Listener;
import nl.devhub.client.settings.Settings;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Slf4j
@Singleton
public class DockerManager {

	private final ScheduledThreadPoolExecutor executor;
	private final Settings settings;

	@Inject
	public DockerManager(Settings settings) {
		int maxContainers = settings.getMaxConcurrentContainers().getValue();
		
		this.settings = settings;
		this.executor = new ScheduledThreadPoolExecutor(maxContainers);
		
		// Add listener for resizing of docker pool.
		settings.getMaxConcurrentContainers().notifyOnChange(new Listener<Integer>() {
			@Override
			public void onChange(Integer newValue) {
				executor.setMaximumPoolSize(newValue);
				log.info("Docker container pool size changed to: {}", newValue);
			}
		});
		
		// Add shutdown hook for terminating jobs.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				executor.shutdown();
			}
		});
	}
	
	public Future<?> run(final DockerJob job) throws InterruptedException {
		final String host = settings.getHost().getValue();
		log.info("Starting new job: {} on host: {}", job, host);
		
		final Identifiable container = create(host, job);
		start(host, container, job);
		return fetchLog(host, container, new Logger() {
			@Override
			public void onNextLine(String line) {
				job.getLogger().onNextLine(line);
			}

			@Override
			public void onClose(int exitCode) {
				try {
					stop(host, container);
					delete(host, container);
				}
				catch (Throwable e) {
					log.error(e.getMessage(), e);
				}
				
				job.getLogger().onClose(exitCode);
			}
		});
	}
	
	private Identifiable create(final String host, DockerJob job) {
		Map<String, Object> volumes = Maps.newHashMap();
		for (String mount : job.getMounts().values()) {
			volumes.put(mount, ImmutableMap.<String, Object>of());
		}
		
		final Container container = new Container()
			.setTty(true)
			.setCmd(CommandParser.parse(job.getCommand()))
			.setWorkingDir(job.getWorkingDir())
			.setVolumes(volumes)
			.setImage(job.getImage());
		
		return request(new Request<Identifiable>() {
			@Override
			public Identifiable request(Client client) {
				log.debug("Creating container: {}", container);
				return client.target(host + "/containers/create")
					.request(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.post(Entity.json(container), Identifiable.class);
			}
		});
	}
	
	private void start(final String host, final Identifiable container, final DockerJob job) {
		perform(new Action() {
			@Override
			public void perform(Client client) {
				List<String> mounts = Lists.newArrayList();
				for (Entry<String, String> mount : job.getMounts().entrySet()) {
					mounts.add(mount.getKey() + ":" + mount.getValue() + ":rw");
				}
				
				ContainerStart start = new ContainerStart()
					.setBinds(mounts)
					.setLxcConf(Lists.newArrayList(new LxcConf("lxc.utsname", "docker")));
		
				log.debug("Starting container: {}", container.getId());
				client.target(host + "/containers/" + container.getId() + "/start")
						.request(MediaType.APPLICATION_JSON)
						.accept(MediaType.TEXT_PLAIN)
						.post(Entity.json(start));
			}
		});
	}
	
	private Future<?> fetchLog(final String host, final Identifiable container, final Logger collector) {
		return executor.submit(new Runnable() {
			@Override
			public void run() {
				perform(new Action() {
					@Override
					public void perform(Client client) {
						log.debug("Streaming log from container: {}", container.getId());
		
						String url = "/containers/" + container.getId() + "/attach?logs=1&stream=1&stdout=1&stderr=1";
						final Reader logs = client.target(host + url)
							.request()
							.accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
							.post(null, Reader.class);
				
						try (BufferedReader reader = new BufferedReader(logs)) {
							String line;
							while ((line = reader.readLine()) != null) {
								collector.onNextLine(line);
							}
						}
						catch (IOException e) {
							log.error(e.getMessage(), e);
						}
						finally {
							StatusCode code = awaitTermination(host, container);
							collector.onClose(code.getStatusCode());
						}
					}
				});
			}
		});
	}
	
	private StatusCode awaitTermination(String host, Identifiable container) {
		log.debug("Awaiting termination of container: {}", container.getId());
		
		StatusCode status;
		while (true) {
			status = getStatus(host, container);
			Integer statusCode = status.getStatusCode();
			if (statusCode != null && statusCode >= 0) {
				break;
			}
			
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}

		log.info("Container: {} terminated with status: {}", container.getId(), status);
		return status;
	}
	
	private StatusCode getStatus(final String host, final Identifiable container) {
		return request(new Request<StatusCode>() {
			@Override
			public StatusCode request(Client client) {
				log.debug("Retrieving status of container: {}", container.getId());
				return client.target(host + "/containers/" + container.getId() + "/wait")
					.request()
					.accept(MediaType.APPLICATION_JSON)
					.post(null, StatusCode.class);
			}
		});
	}
	
	private void stop(final String host, final Identifiable container) {
		perform(new Action() {
			@Override
			public void perform(Client client) {
				log.debug("Stopping container: {}", container.getId());
				client.target(host + "/containers/" + container.getId() + "/stop?t=5")
					.request(MediaType.APPLICATION_JSON)
					.accept(MediaType.TEXT_PLAIN)
					.post(null);
			}
		}); 
	}
	
	private void delete(final String host, final Identifiable container) {
		perform(new Action() {
			@Override
			public void perform(Client client) {
				log.debug("Removing container: {}", container.getId());
				client.target(host + "/containers/" + container.getId() + "?v=1")
					.request()
					.delete();
			}
		}); 
	}
	
	private <T> T request(Request<T> request) {
		Client client = null;
		try { 
			client = ClientBuilder.newClient();
			return request.request(client);
		}
		finally {
			if (client != null) {
				client.close();
			}
		}
	}
	
	private void perform(Action action) {
		Client client = null;
		try { 
			client = ClientBuilder.newClient();
			action.perform(client);
		}
		finally {
			if (client != null) {
				client.close();
			}
		}
	}

}
