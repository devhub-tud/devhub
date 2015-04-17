package nl.tudelft.ewi.devhub.server.database.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name="pull_requests")
@IdClass(PullRequest.PullRequestId.class)
@EqualsAndHashCode(of={"issueId", "group"})
public class PullRequest {

	@Data
	@EqualsAndHashCode
	public static class PullRequestId implements Serializable {
		private long issueId;
		private Group group;
	}

	@Id
	@NotNull
	@Column(name = "id")
	private long issueId;

	@Id
	@NotNull
	@ManyToOne
	@JoinColumn(name = "repository_name", referencedColumnName = "repository_name")
	private Group group;
	
	@NotNull
	@Column(name="branch_name")
	private String branchName;
	
	@Column(name="open")
	private boolean open;

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

	/**
	 * @return true if the pull request is closed
	 */
	public boolean isClosed() {
		return !isOpen();
	}

}
