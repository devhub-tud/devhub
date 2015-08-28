package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "build_results")
@ToString(exclude = "log")
@EqualsAndHashCode(of = "commit")
@IdClass(BuildResult.BuildResultId.class)
public class BuildResult {

	public static BuildResult newBuildResult(final Commit commit) {
		BuildResult result = new BuildResult();
		result.setCommit(commit);
		result.setSuccess(null);
		result.setLog(null);
		return result;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BuildResultId implements Serializable {

		private Commit.CommitId commit;

	}

	@Id
	@JoinColumns({
		@JoinColumn(name = "commit_id", referencedColumnName = "commit_id"),
		@JoinColumn(name = "repository_id", referencedColumnName = "repository_id")
	})
	@OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private Commit commit;

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

	public RepositoryEntity getRepository() {
		return getCommit().getRepository();
	}

}
