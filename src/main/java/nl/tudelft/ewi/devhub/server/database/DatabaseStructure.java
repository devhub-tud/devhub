package nl.tudelft.ewi.devhub.server.database;

import javax.inject.Inject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.google.inject.persist.PersistService;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for running Liquibase database migrations at start-up.
 */
@Slf4j
public class DatabaseStructure {

	private final Properties properties;

	/**
	 * This constructs a new {@link DatabaseStructure} using the specified {@link PersistService}.
	 * 
	 * @throws IOException
	 *             In case the persistence configuration could not be loaded.
	 */
	@Inject
	public DatabaseStructure() throws IOException {
		log.info("Starting database");

		this.properties = PersistenceConfiguration.load("default");
		Strategy strategy = getStrategy(properties);

		switch (strategy) {
			case DROP_CREATE:
				dropStructure();
				updateStructure();
				break;
			case UPDATE:
				updateStructure();
				break;
			default:
				throw new IllegalArgumentException("No \"liquibase.liquibase-strategy\" " +
						"parameter specified in persistence configuration.");
		}
	}

	/**
	 * Calling this method will attempt to migrate the database to the latest database structure.
	 */
	private void updateStructure() {
		try (Connection conn = createConnection()) {
			log.info("Preparing liquibase run...");
			
			ClassLoaderResourceAccessor classLoader = new ClassLoaderResourceAccessor();
			JdbcConnection jdbcConnection = new JdbcConnection(conn);

			log.info("Running liquibase: {}", "changelog.xml");
			new Liquibase("changelog.xml", classLoader, jdbcConnection).update("");
			conn.commit();

			log.debug("Finished processing all liquibase changesets.");
		}
		catch (LiquibaseException | ClassNotFoundException | SQLException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Calling this method will attempt to drop the entire database.
	 */
	private void dropStructure() {
		log.info("Dropping DB structure");
		try (Connection conn = createConnection()) {
			getDatabaseType().dropDatabase(conn);
		}
		catch (ClassNotFoundException | SQLException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private Connection createConnection() throws ClassNotFoundException, SQLException {
		String driver = getValue("hibernate.connection.driver_class");
		String username = getValue("javax.persistence.jdbc.user");
		String password = getValue("javax.persistence.jdbc.password");
		String url = getValue("javax.persistence.jdbc.url");

		Class.forName(driver);
		return DriverManager.getConnection(url, username, password);
	}

	private Strategy getStrategy(Properties properties) {
		return Strategy.getStrategy(properties.getProperty("liquibase.liquibase-strategy"));
	}
	
	private DatabaseType getDatabaseType() {
		String driver = getValue("hibernate.connection.driver_class");
		return DatabaseType.forDriver(driver);
	}

	private String getValue(String key) {
		if (properties.containsKey(key)) {
			return properties.get(key).toString();
		}
		return null;
	}
}
