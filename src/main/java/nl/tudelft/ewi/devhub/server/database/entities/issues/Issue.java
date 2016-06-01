package nl.tudelft.ewi.devhub.server.database.entities.issues;

import java.net.URI;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@Entity
@Table(name="repository_issues")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Issue extends AbstractIssue {

	@Override
	public URI getURI() {
		return getRepository().getURI().resolve("issue/" + getIssueId() + "/");
	}
	
}
