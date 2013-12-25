package nl.devhub.client.jaxrs;

import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class JobResultModel {
	
	private UUID id;
	private Integer exitCode;
	
}
