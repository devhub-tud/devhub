package nl.tudelft.ewi.devhub.server.database.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.Size;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import lombok.*;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;

@Data
@Entity
@Table(name = "commit")
@ToString(exclude = "comments")
@EqualsAndHashCode(of = "commitId")
public class Commit implements Comparable<Commit> {

	@Data
	@Embeddable
	@EqualsAndHashCode
	public static class CommitId implements Serializable {

		@Getter(AccessLevel.PROTECTED)
		@Setter(AccessLevel.PROTECTED)
		@Column(name = "repository_id")
		private long repositoryId;

		@Column(name = "commit_id")
		private String commitId;

	}

	@Delegate
	@EmbeddedId
	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	private CommitId id = new CommitId();

	@ManyToOne(optional = false)
	@MapsId("repositoryId")
	private RepositoryEntity repository;

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

	@Override
	public int compareTo(Commit o) {
		return ComparisonChain.start()
			.compare(getCommitTime(), o.getCommitTime(), Ordering.natural().nullsLast())
			.result();
	}
}
