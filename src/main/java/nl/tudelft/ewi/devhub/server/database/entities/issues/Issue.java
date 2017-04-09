package nl.tudelft.ewi.devhub.server.database.entities.issues;

import java.net.URI;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.entities.comments.IssueComment;


@Data
@Entity
@Table(name="repository_issues")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper=true, includeFieldNames=true)
public class Issue extends AbstractIssue {

	@Override
	public URI getURI() {
		return getRepository().getURI().resolve("issue/" + getIssueId() + "/");
	}

	@OrderBy("timestamp ASC")
	@OneToMany(mappedBy = "issue", fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REMOVE}, orphanRemoval = true)
	private List<IssueComment> comments;
	
}
