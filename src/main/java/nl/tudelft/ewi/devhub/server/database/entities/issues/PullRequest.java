package nl.tudelft.ewi.devhub.server.database.entities.issues;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.database.entities.comments.PullRequestComment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URI;
import java.util.List;

@Data
@Entity
@Table(name="pull_requests")
@EqualsAndHashCode(callSuper = true)
public class PullRequest extends AbstractIssue {

	@NotNull
	@Size(max = 255)
	@Column(name="branch_name", length = 255)
	private String branchName;

	@Column(name="merged")
	private boolean merged;

	@NotNull
	@Column(name="merge_commit_id")
	private String mergeBase;

	@NotNull
	@Column(name="destination_commit_id")
	private String destination;

	@Column(name="ahead")
	private Integer ahead;

	@Column(name="behind")
	private Integer behind;

	@OrderBy("time ASC")
	@OneToMany(mappedBy = "pullRequest", fetch = FetchType.LAZY)
	private List<PullRequestComment> comments;

	@Override
	public URI getURI() {
		return getRepository().getURI().resolve("pull/" + getIssueId() + "/");
	}

}
