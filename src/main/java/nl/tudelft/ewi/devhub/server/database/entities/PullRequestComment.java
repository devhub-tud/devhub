package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
		@JoinColumn(name="issue_id", referencedColumnName="id"),
		@JoinColumn(name="repository_name", referencedColumnName="repository_name")
	})
	private PullRequest pullRequest;

}
