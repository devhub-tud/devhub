package nl.devhub.client.docker.models;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ContainerStart {
	private String[] Binds;
	private LxcConf[] LxcConf;
}