package nl.tudelft.ewi.devhub.server.database;

import lombok.SneakyThrows;

import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

/**
 * This Guice {@link AbstractModule} is responsible for specifying all required Guice bindings for
 * the persistence layer of the REST API server.
 */
public class DbModule extends AbstractModule {

	@Override
	@SneakyThrows
	protected void configure() {
		JpaPersistModule jpaModule = new JpaPersistModule("default");
		jpaModule.properties(PersistenceConfiguration.load());
		install(jpaModule);

		bind(DatabaseStructure.class).asEagerSingleton();
	}

}
