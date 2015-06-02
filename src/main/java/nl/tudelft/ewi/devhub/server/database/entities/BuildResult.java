package nl.tudelft.ewi.devhub.server.database.entities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@Entity
@Table(name = "build_results")
public class BuildResult {

	public static BuildResult newBuildResult(Commit commit) {
		return newBuildResult(commit.getRepository(), commit.getCommitId());
	}

	public static BuildResult newBuildResult(Group group, String commit) {
		BuildResult result = new BuildResult();
		result.setRepository(group);
		result.setCommitId(commit);
		result.setSuccess(null);
		result.setLog(null);
		return result;
	}

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "repository_id")
	private Group repository;

	@NotEmpty
	@Column(name = "commit_id")
	private String commitId;

	@Column(name = "success")
	private Boolean success;

	@Column(name = "log")
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String log;

	/**
	 * Check if the build is finished or queued
	 * @return true if the build is finished
	 */
	public boolean hasFinished() {
		return getSuccess() != null;
	}

	/**
	 * Check if the build has succeeded
	 * @return true if the build has succeeded
	 */
	public boolean hasSucceeded() {
		return Boolean.TRUE.equals(getSuccess());
	}

	/**
	 * Check if the build has failed
	 * @return true if the build has failed
	 */
	public boolean hasFailed() {
		return Boolean.FALSE.equals(getSuccess());
	}

}
