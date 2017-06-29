package nl.tudelft.ewi.devhub.server.database.entities.comments;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.AbstractIssue;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Set;

@Data
@Entity
@ToString(exclude="issue", callSuper = true)
@Table(name = "repository_issue_comments")
@EqualsAndHashCode(callSuper = true)
public class IssueComment extends Comment  {

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumns({
		@JoinColumn(name="repository_id", referencedColumnName="repository_id"),
		@JoinColumn(name="issue_id", referencedColumnName="issue_id")
	})
	private AbstractIssue issue;

	@Override
	public RepositoryEntity getRepository() {
		return getIssue().getRepository();
	}

	@Override
	public URI getURI() {
		return getIssue().getURI();
	}

	@Override
	public Set<User> getWatchers() {
		return getIssue().getWatchers();
	}

}
