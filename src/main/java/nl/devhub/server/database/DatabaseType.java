package nl.devhub.server.database;

import java.sql.Connection;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
enum DatabaseType {
	
	POSTGRESQL ("org.postgresql.Driver") {
		@Override
		public void dropDatabase(Connection conn) throws SQLException {
			try {
				conn.createStatement().executeUpdate("DROP SCHEMA public CASCADE;");
			}
			catch (SQLException e) {
				if (e.getMessage().contains("\"public\" does not exist")) {
					log.info("Already dropped");
				}
				else {
					throw e;
				}
			}
			conn.createStatement().executeUpdate("CREATE SCHEMA public;");
		}
	}, 
	H2 ("org.h2.Driver") {
		@Override
		public void dropDatabase(Connection conn) throws SQLException {
			conn.createStatement().executeUpdate("DROP ALL OBJECTS;");
		}
	};
	
	private final String driver;
	
	private DatabaseType(String driver) {
		this.driver = driver;
	}
	
	public abstract void dropDatabase(Connection conn) throws SQLException;
	
	public static DatabaseType forDriver(String driver) {
		for (DatabaseType type : values()) {
			if (type.driver.equals(driver)) {
				return type;
			}
		}
		throw new IllegalArgumentException("There is no DatabaseType available for driver: " + driver);
	}
}