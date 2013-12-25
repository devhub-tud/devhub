package nl.devhub.client.jaxrs;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MavenTestResult {
	
	public static enum Result {
		ERROR, UNSTABLE, SUCCEEDED;
	}

	private String packageName;
	private String className;
	private String testName;
	
	private Result result;
	
}
