package nl.devhub.client.jaxrs;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class MavenJobResultModel extends JobResultModel {
	
	private List<MavenTestResult> testResults;
	
}
