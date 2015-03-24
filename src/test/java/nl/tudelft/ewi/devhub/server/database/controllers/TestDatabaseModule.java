package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.tudelft.ewi.devhub.server.database.DbModule;

import com.google.inject.persist.PersistService;

public class TestDatabaseModule extends DbModule {
	
	@Override
	protected void configure() {
		super.configure();
		bind(JPAInitializer.class).asEagerSingleton();
	}
	
	@Singleton
	public static class JPAInitializer {
 
		@Inject
		public JPAInitializer(final PersistService service) {
			service.start();
		}
		
	}

}
