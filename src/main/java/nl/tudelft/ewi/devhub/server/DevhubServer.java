package nl.tudelft.ewi.devhub.server;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import java.io.File;
import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.persist.PersistFilter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * This class bootstraps a DevHub server.
 */
@Slf4j
public class DevhubServer {

	private static File determineRootFolder() {
		File developmentFolder = new File("src/main/resources");
		if (developmentFolder.exists()) {
			return developmentFolder;
		}

		return new File("config");
	}

	public static void main(String[] args) throws Exception {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		DevhubServer server = new DevhubServer();
		server.startServer();
		server.joinThread();
	}

	private final Server server;

	/**
	 * Constructs a new {@link DevhubServer} object.
	 */
	public DevhubServer() {
		Config config = new Config();
		config.reload();

		File rootFolder = determineRootFolder();

		ResourceHandler resources = new ResourceHandler();
		resources.setBaseResource(Resource.newResource(new File(rootFolder, "static")));
		resources.setDirectoriesListed(false);
		resources.setCacheControl("max-age=3600");

		HashSessionManager hashSessionManager = new HashSessionManager();
		hashSessionManager.setMaxInactiveInterval(1800);

		DevhubHandler devhub = new DevhubHandler(config, rootFolder);
		devhub.setHandler(new SessionHandler(hashSessionManager));

		ContextHandlerCollection handlers = new ContextHandlerCollection();
		handlers.addContext("/static/", "/static").setHandler(resources);
		handlers.addContext("/", "/").setHandler(devhub);

		server = new Server(config.getHttpPort());
		server.setSessionIdManager(new HashSessionIdManager());
		server.setHandler(handlers);
	}

	/**
	 * Starts the {@link DevhubServer} object.
	 * 
	 * @throws Exception
	 *             In case the server could not be started.
	 */
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
	}
	
	public void joinThread() throws InterruptedException {
		server.join();
	}

	/**
	 * Stops the {@link DevhubServer} object.
	 * 
	 * @throws Exception
	 *             In case the server could not be stopped.
	 */
	public void stopServer() throws Exception {
		server.stop();
	}

	private static class DevhubHandler extends ServletContextHandler {

		private DevhubHandler(final Config config, final File rootFolder) {
			addEventListener(new GuiceResteasyBootstrapServletContextListener() {
				@Override
				protected List<Module> getModules(ServletContext context) {
					return ImmutableList.<Module> of(new DevhubModule(config, rootFolder));
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
