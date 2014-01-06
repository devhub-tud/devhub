package nl.devhub.server;

import java.io.File;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.persist.PersistFilter;

@Slf4j
public class DevhubServer {
	
	private static File determineRootFolder() {
		File developmentFolder = new File("src/main/resources");
		if (developmentFolder.exists()) {
			return developmentFolder;
		}
		
		return new File("");
	}
	
	public static void main(String[] args) throws Exception {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
			
		DevhubServer server = new DevhubServer(8080);
		server.startServer();
	}

	private final Server server;

	public DevhubServer(int port) {
		File rootFolder = determineRootFolder();

		ResourceHandler resources = new ResourceHandler();
		resources.setBaseResource(Resource.newResource(new File(rootFolder, "static")));
		resources.setDirectoriesListed(false);
		resources.setCacheControl("max-age=3600");
		
		DevhubHandler devhub = new DevhubHandler(rootFolder);
		
		ContextHandlerCollection handlers = new ContextHandlerCollection();
		handlers.addContext("/static/", "/static").setHandler(resources);
		handlers.addContext("/", "/").setHandler(devhub);
		
		server = new Server(port);
		server.setHandler(handlers);
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
		
		public DevhubHandler(final File rootFolder) {
			addEventListener(new GuiceResteasyBootstrapServletContextListener() {
				@Override
				protected List<Module> getModules(ServletContext context) {
					return ImmutableList.<Module>of(new DevhubModule(rootFolder));
				}
				
				@Override
				protected void withInjector(Injector injector) {
					FilterHolder persistFilterHolder = new FilterHolder(injector.getInstance(PersistFilter.class));
                    addFilter(persistFilterHolder, "/*", EnumSet.allOf(DispatcherType.class));
				}
			});
			
			addServlet(HttpServletDispatcher.class, "/");
		}
	}
	
}
