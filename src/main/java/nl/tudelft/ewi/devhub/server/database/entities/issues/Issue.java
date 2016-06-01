package nl.tudelft.ewi.devhub.server.database.entities.issues;

import java.net.URI;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.NoArgsConstructor;


@Entity
@Table(name="repository_issues")
@NoArgsConstructor
public class Issue extends AbstractIssue {

	@Override
	public URI getURI() {
		return getRepository().getURI().resolve("issue/" + getIssueId() + "/");
	}
	
}
