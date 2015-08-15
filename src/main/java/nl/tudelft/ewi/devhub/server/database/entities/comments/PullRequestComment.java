package nl.tudelft.ewi.devhub.server.database.entities.comments;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Data
@Entity
@ToString(exclude="pullRequest")
@Table(name = "pull_request_comments")
@EqualsAndHashCode(of="commentId")
public class PullRequestComment extends Comment  {

	@NotNull
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="repository_id", referencedColumnName="repository_id"),
		@JoinColumn(name="issue_id", referencedColumnName="issue_id")
	})
	private PullRequest pullRequest;

}
