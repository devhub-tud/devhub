package nl.tudelft.ewi.devhub.server.database.entities.issues;

import java.net.URI;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("ISSUE")
@ToString(callSuper=true, includeFieldNames=true)
public class Issue extends AbstractIssue {

	@Override
	public URI getURI() {
		return getRepository().getURI().resolve("issue/" + getIssueId() + "/");
	}
	
}
