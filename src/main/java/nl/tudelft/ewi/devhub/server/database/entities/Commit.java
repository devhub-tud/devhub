package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.Base;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@Table(name = "commit")
@IdClass(Commit.CommitId.class)
@ToString(exclude = {"comments", "buildResult", "parents"})
@EqualsAndHashCode(of = {"repository", "commitId"})
public class Commit implements Event, Base {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CommitId implements Serializable {

		private long repository;

		private String commitId;

	}

	@Id
	@JsonBackReference
	@ManyToOne(optional = false)
	@JoinColumn(name = "repository_id")
	private RepositoryEntity repository;

	@Id
	@Column(name = "commit_id")
	private String commitId;

	@Size(max = 255)
	@Column(name = "author")
	private String author;

	@Column(name = "committed")
	@Temporal(TemporalType.TIMESTAMP)
	private Date commitTime;

	@Column(name = "pushed")
	@Temporal(TemporalType.TIMESTAMP)
	private Date pushTime;

	@JsonIgnore
	@OneToMany(mappedBy = "commit", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CommitComment> comments;

	@JsonIgnore
	@OneToOne(cascade = {CascadeType.DETACH, CascadeType.REMOVE}, fetch = FetchType.LAZY, orphanRemoval = true)
	@PrimaryKeyJoinColumns({
		@PrimaryKeyJoinColumn(name = "repository_id", referencedColumnName = "repository_id"),
		@PrimaryKeyJoinColumn(name = "commit_id", referencedColumnName = "commit_id")
	})
	private BuildResult buildResult;

	@JsonIgnore
	@ManyToMany
	@JoinTable(name="commit_parent",
		joinColumns = {
			@JoinColumn(name="repository_id", referencedColumnName="repository_id"),
			@JoinColumn(name="commit_id", referencedColumnName="commit_id")
		},
		inverseJoinColumns= {
			@JoinColumn(name="parent_repository_id", referencedColumnName="repository_id"),
			@JoinColumn(name="parent_commit_id", referencedColumnName="commit_id")
		}
	)
	private List<Commit> parents;

	public boolean isMerge() {
		return parents.size() > 1;
	}

	@Override
	public Date getTimestamp() {
		return getPushTime();
	}

	@Override
	public URI getURI() {
		return getRepository().getURI().resolve("commits/" + getCommitId() + "/");
	}

	public URI getDiffURI() {
		return getURI().resolve("diff/");
	}

	@JsonIgnore
	public boolean hasNoBuildResult() {
		return Objects.isNull(buildResult);
	}

}
