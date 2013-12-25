package nl.devhub.client.settings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.Data;

@Data
public class Settings {

	public static Settings load(String path) throws IOException {
		return new Settings(path);
	}
	
	private final Model<Integer> maxConcurrentContainers = new Model<Integer>(0, "docker.max-containers", Integer.class);
	private final Model<File> stagingDirectory = new Model<File>(null, "docker.staging-directory", File.class);
	private final Model<File> remoteDirectory = new Model<File>(null, "docker.remote-directory", File.class);
	private final Model<String> host = new Model<String>("http://localhost:4243", "docker.host", String.class);
	
	private final String path;
	
	private Settings(String path) throws IOException {
		this.path = path;
		reload();
	}
	
	public void reload() throws IOException {
		Properties properties = new Properties();
		try (InputStream stream = Settings.class.getResourceAsStream(path)) {
			properties.load(stream);
		}
		maxConcurrentContainers.update(properties);
		stagingDirectory.update(properties);
		remoteDirectory.update(properties);
		host.update(properties);
	}

}
