package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import nl.tudelft.ewi.devhub.server.database.Base;
import nl.tudelft.ewi.devhub.server.database.entities.Commit.CommitId;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.net.URI;

@Data
@Entity
@Table(name = "build_results")
@ToString(exclude = "log")
@EqualsAndHashCode(of = "commit")
public class BuildResult implements Base {

	public static BuildResult newBuildResult(final Commit commit) {
		BuildResult result = new BuildResult();
		result.setCommit(commit);
		result.setSuccess(null);
		result.setLog(null);
		return result;
	}

	/*
	 * Technically it should have been enough to annotate BuildResult#commit with @Id,
	 * but due to a bug this did not work:
	  *
	 *    https://hibernate.atlassian.net/browse/HHH-3993
	 *
	 * Using the Commit.CommitId as @EmbeddedId combined with a @MapsId works just fine.
	 */
	@EmbeddedId
	private CommitId commitId;

	@MapsId
	@JoinColumns(value = {
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

	@Override
	public URI getURI() {
		return getCommit().getURI().resolve("build/");
	}

}
