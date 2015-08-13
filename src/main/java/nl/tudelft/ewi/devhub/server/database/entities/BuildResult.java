package nl.tudelft.ewi.devhub.server.database.entities;

import javax.persistence.*;

import lombok.*;
import org.hibernate.annotations.Type;

@Data
@Entity
@Table(name = "build_results")
public class BuildResult {

	public static BuildResult newBuildResult(Commit commit) {
		BuildResult result = new BuildResult();
		result.setCommit(commit);
		result.setSuccess(null);
		result.setLog(null);
		return result;
	}

//	@Id
//	@Column(name = "id")
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private long id;
//
//	@OneToOne(optional = false)
//	@JoinColumns({
//		@JoinColumn(name = "repository_id", referencedColumnName = "repository_id"),
//		@JoinColumn(name = "commit_id", referencedColumnName = "commit_id")
//	})

	@EmbeddedId
	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	private Commit.CommitId id = new Commit.CommitId();

	@MapsId
	@OneToOne(optional = false)
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
