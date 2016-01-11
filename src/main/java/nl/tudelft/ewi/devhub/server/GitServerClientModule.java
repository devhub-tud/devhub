package nl.tudelft.ewi.devhub.server;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.web.api.BaseApi;
import nl.tudelft.ewi.git.web.api.GroupsApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.UsersApi;
import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import javax.annotation.Nullable;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

/**
 * Dependency injection module for that initializes a pooled {@link ResteasyClient} for
 * git server access. This module exposes bindings for:
 *
 * <ul>
 *    <li>{@link UsersApi}</li>
 *    <li>{@link GroupsApi}</li>
 *    <li>{@link RepositoriesApi}</li>
 * </ul>
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class GitServerClientModule extends AbstractModule {

	private final Config config;
	private final LifeCycle lifeCycle;

	public GitServerClientModule(Config config, LifeCycle lifeCycle) {
		this.config = config;
		this.lifeCycle = lifeCycle;
	}

	@Override
	protected void configure() {
		bind(Config.class).annotatedWith(Names.named("git.server.config")).toInstance(config);
		bind(GitServerClientInitializer.class).asEagerSingleton();
		bind(LifeCycle.class).toInstance(lifeCycle);
	}

	/**
	 * The {@code GitServerClientInitializer} initializes a proxy of {@link BaseApi} using
	 * a pooled {@link ResteasyClient} based on the available {@link Config}. Initialization
	 * is done on eager singleton instantiation.
	 *
	 * The {@code GitServerClientInitializer} listens
	 * to an available {@link LifeCycle} in order to close the {@code Client} when the lifecycle
	 * terminates.
	 */
	@Singleton
	protected static class GitServerClientInitializer extends AbstractLifeCycleListener {

		private final ResteasyClient resteasyClient;

		@Getter
		private final BaseApi baseApi;

		@Inject
		public GitServerClientInitializer(@Named("git.server.config") Config config, @Nullable LifeCycle lifeCycle) {
			resteasyClient = new ResteasyClientBuilder()
				.connectionPoolSize(config.getGitServerConnectionPoolSize())
				.build();
			if(lifeCycle != null) lifeCycle.addLifeCycleListener(this);
			baseApi = resteasyClient.target(config.getGitServerHost()).proxy(BaseApi.class);

			checkApiEndpointAnnotations();

			log.info("Initialized git server client with target {} and pool size {}",
				config.getGitServerHost(),
				config.getGitServerConnectionPoolSize());
		}

		@Override
		public void lifeCycleStopping(LifeCycle event) {
			resteasyClient.close();
		}

		private void checkApiEndpointAnnotations() {
			Class<?>[] proxyClasses = new Class<?>[] { UsersApi.class, RepositoriesApi.class, GroupsApi.class };
			for(Class<?> clasz : proxyClasses) {
				if(clasz.isAnnotationPresent(Path.class) || clasz.isAnnotationPresent(Provider.class)) {
					throw new RuntimeException("Class " + clasz + " is annotated with @Path or @Provider. These bindings " +
						"will cause Reasteasy-Guice to expose them as API endpoints, which is most likely unwanted for " +
						"Resteasy client proxies injected through Guice.");
				}
			}
		}

	}

	@Provides
	public UsersApi users(GitServerClientInitializer gitServerClient) {
		return gitServerClient.getBaseApi().users();
	}

	@Provides
	public GroupsApi groups(GitServerClientInitializer gitServerClient) {
		return gitServerClient.getBaseApi().groups();
	}

	@Provides
	public RepositoriesApi repositories(GitServerClientInitializer gitServerClient) {
		return gitServerClient.getBaseApi().repositories();
	}

}
