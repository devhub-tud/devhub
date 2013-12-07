package nl.devhub.client.docker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import nl.devhub.client.docker.models.Container;
import nl.devhub.client.docker.models.ContainerStart;
import nl.devhub.client.docker.models.Identifiable;
import nl.devhub.client.docker.models.LxcConf;
import nl.devhub.client.docker.models.StatusCode;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.ImmutableMap;

@Slf4j
public class DockerManager {

	private static final String HOST = "http://192.168.178.36:4243";
	
	private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);

	public static void main(String[] args) throws IOException, InterruptedException {
		new DockerManager().run();
		executor.shutdown();
	}
	
	public void run() throws InterruptedException {
		Identifiable container = create();
		start(container);
		Future<?> future = fetchLog(container, new Log() {
			@Override
			public void onNextLine(String line) {
				log.info(line);
			}
			@Override
			public void onClose() {
				log.info("TERMINATED");
			}
		});
		
		awaitTermination(container);
		future.cancel(true);
		
		stop(container);
		delete(container);
	}
	
	public Identifiable create() {
		final Container container = new Container()
			.setTty(true)
			.setCmd(new String[] { "mvn", "clean", "package" })
			.setWorkingDir("/workspace")
			.setVolumes(ImmutableMap.<String, Object>of("/workspace", ImmutableMap.<String, Object>of()))
			.setImage("devhub/builder:latest");
		
		return request(new Request<Identifiable>() {
			@Override
			public Identifiable request(Client client) {
				log.debug("Creating container: {}", container);
				return client.target(HOST + "/containers/create")
					.request(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.post(Entity.json(container), Identifiable.class);
			}
		});
	}
	
	public void start(final Identifiable container) {
		perform(new Action() {
			@Override
			public void perform(Client client) {
				ContainerStart start = new ContainerStart()
					.setBinds(new String[] { "/workspace:/workspace:rw" })
					.setLxcConf(new LxcConf[] { new LxcConf("lxc.utsname", "docker") });
		
				log.debug("Starting container: {}", container.getId());
				client.target(HOST + "/containers/" + container.getId() + "/start")
						.request(MediaType.APPLICATION_JSON)
						.accept(MediaType.TEXT_PLAIN)
						.post(Entity.json(start));
			}
		});
	}
	
	public Future<?> fetchLog(final Identifiable container, final Log collector) {
		return executor.submit(new Runnable() {
			@Override
			public void run() {
				perform(new Action() {
					@Override
					public void perform(Client client) {
						log.debug("Streaming log from container: {}", container.getId());
		
						String url = "/containers/" + container.getId() + "/attach?logs=1&stream=1&stdout=1&stderr=1";
						final Reader logs = client.target(HOST + url)
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
							collector.onClose();
						}
					}
				});
			}
		});
	}
	
	public StatusCode awaitTermination(final Identifiable container) {
		log.debug("Awaiting termination of container: {}", container.getId());
		
		StatusCode status;
		while (true) {
			status = getStatus(container);
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
	
	public StatusCode getStatus(final Identifiable container) {
		return request(new Request<StatusCode>() {
			@Override
			public StatusCode request(Client client) {
				log.debug("Retrieving status of container: {}", container.getId());
				return client.target(HOST + "/containers/" + container.getId() + "/wait")
					.request()
					.accept(MediaType.APPLICATION_JSON)
					.post(null, StatusCode.class);
			}
		});
	}
	
	public void stop(final Identifiable container) {
		perform(new Action() {
			@Override
			public void perform(Client client) {
				log.debug("Stopping container: {}", container.getId());
				client.target(HOST + "/containers/" + container.getId() + "/stop?t=5")
					.request(MediaType.APPLICATION_JSON)
					.accept(MediaType.TEXT_PLAIN)
					.post(null);
			}
		}); 
	}
	
	public void delete(final Identifiable container) {
		perform(new Action() {
			@Override
			public void perform(Client client) {
				log.debug("Removing container: {}", container.getId());
				client.target(HOST + "/containers/" + container.getId() + "?v=1")
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
