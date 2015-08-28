package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "commit")
@IdClass(Commit.CommitId.class)
@ToString(exclude = "comments")
@EqualsAndHashCode(of = {"repository", "commitId"}, callSuper = false)
public class Commit implements Event {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CommitId implements Serializable {

		private long repository;

		private String commitId;

	}

	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "repository_id")
	private RepositoryEntity repository;

	@Id
	@Column(name = "commit_id")
	private String commitId;

	@Size(max = 255)
	@Column(name = "author", length = 255)
	private String author;

	@Column(name = "committed")
	@Temporal(TemporalType.TIMESTAMP)
	private Date commitTime;

	@Column(name = "pushed")
	@Temporal(TemporalType.TIMESTAMP)
	private Date pushTime;

	@Column(name = "merge")
	private Boolean merge;
	
	@OneToMany(mappedBy = "commit", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<CommitComment> comments;

	@OneToOne(optional = true, mappedBy = "commit", cascade = CascadeType.REMOVE)
	private BuildResult buildResult;

	@Override
	public Date getTimestamp() {
		return getPushTime();
	}

}
