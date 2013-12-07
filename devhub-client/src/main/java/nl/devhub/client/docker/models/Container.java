package nl.devhub.client.docker.models;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true)
public class Container {
	@JsonProperty(required = false)
	private String Id;

	private String Hostname;
	private String User;
	private Integer Memory;
	private Integer MemorySwap;
	private Boolean AttachStdin;
	private Boolean AttachStdout;
	private Boolean AttachStderr;
	private Object PortSpecs;
	private Boolean Privileged;
	private Boolean Tty;
	private Boolean OpenStdin;
	private Boolean StdinOnce;
	private Object Env;
	private String[] Cmd;
	private Object Dns;
	private String Image;
	private Object Volumes;
	private String VolumesFrom;
	private String WorkingDir;
}