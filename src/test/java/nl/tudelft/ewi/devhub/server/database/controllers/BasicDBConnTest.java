package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.PersistenceConfiguration;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

/**
 * Created by Jan-Willem on 8/15/2015.
 */
@Slf4j
@RunWith(JukitoRunner.class)
@UseModules(BasicDBConnTest.DbModule.class)
public class BasicDBConnTest {

	public static class DbModule extends AbstractModule {

		@Override
		@SneakyThrows
		protected void configure() {
			JpaPersistModule jpaModule = new JpaPersistModule("default");
			Properties properties = PersistenceConfiguration.load();
			properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
			properties.setProperty("hibernate.show_sql", "true");
			jpaModule.properties(properties);

			install(jpaModule);
			bind(TestDatabaseModule.JPAInitializer.class).asEagerSingleton();
		}

	}

	@Test
	public void test(){
		log.info("hoi");
	}

}
