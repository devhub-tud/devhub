package nl.tudelft.ewi.devhub.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import com.google.inject.Singleton;

@Slf4j
@Singleton
public class Config {

	private final Properties properties;

	public Config() {
		this.properties = new Properties();
		reload();
	}

	public void reload() {
		try (InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/config.properties"))) {
			properties.load(reader);
		}
		catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public int getHttpPort() {
		return Integer.parseInt(properties.getProperty("http.port", "8080"));
	}

	public String getHttpUrl() {
		return properties.getProperty("http.url");
	}

	public String getGitServerHost() {
		return properties.getProperty("git-server.host");
	}

	public String getSmtpHost() {
		return properties.getProperty("smtp-server.host");
	}

	public String getSmtpUser() {
		return properties.getProperty("smtp-server.user");
	}

	public String getSmtpPass() {
		return properties.getProperty("smtp-server.pass");
	}

	public String getSmtpOrigin() {
		return properties.getProperty("smtp-server.origin");
	}

}
