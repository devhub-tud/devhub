package nl.devhub.client.docker.models;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ContainerStart {
	private List<String> Binds;
	private List<LxcConf> LxcConf;
}