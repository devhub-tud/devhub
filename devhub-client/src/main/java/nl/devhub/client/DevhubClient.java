package nl.devhub.client;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;
import nl.devhub.client.jaxrs.JobsResource;
import nl.devhub.client.jaxrs.MavenJobModel;
import nl.devhub.client.settings.Settings;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;

@Slf4j
public class DevhubClient {
	
	public static void main(String[] args) throws Exception {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
			
		DevhubClient server = new DevhubClient(8080);
		server.startServer();
	}

	private final Server server;

	public DevhubClient(int port) throws IOException {
		this.server = new Server(port);
		
		Settings settings = Settings.load("/settings.properties");
		server.setHandler(new DevhubHandler(settings));
	}
	
	public void startServer() throws Exception {
		server.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					stopServer();
				}
				catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
		
		server.join();
	}
	
	public void stopServer() throws Exception {
		server.stop();
	}
	
	private static class DevhubHandler extends ServletContextHandler {
		
		public DevhubHandler(final Settings settings) {
			addEventListener(new GuiceResteasyBootstrapServletContextListener() {
				@Override
				protected List<Module> getModules(ServletContext context) {
					return ImmutableList.<Module>of(new ClientModule(settings));
				}
				
				@Override
				protected void withInjector(Injector injector) {
					injector.getInstance(JobsResource.class).submitJob(new MavenJobModel().setRepositoryUrl("https://github.com/avandeursen/jpacman-framework.git").setBranch("master").setCommit("de74e0cad7e948d7acb40844382d7eecabad700d"));
				}
			});
			
			addServlet(HttpServletDispatcher.class, "/");
		}
	}
	
}
