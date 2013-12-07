package nl.devhub.client.docker.settings;

import lombok.Data;

@Data
public class Settings {

	private final Model<Integer> maxConcurrentContainers = new Model<Integer>(0);
	private final Model<String> host = new Model<String>("http://localhost:4243");
	
}
