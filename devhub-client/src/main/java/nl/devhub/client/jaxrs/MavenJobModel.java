package nl.devhub.client.jaxrs;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MavenJobModel {
	
	private String repositoryUrl;
	private String branch;
	private String commit;
	
}
