package nl.devhub.client.docker;

import java.util.Map;

import lombok.Data;

@Data
public class DockerJob {

	private final String image;
	private final String workingDir;
	private final String command;
	private final Map<String, String> mounts;
	private final Logger logger;
	
}
