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
@IdClass(Commit.CommitId.class)
@ToString(exclude = "comments")
@EqualsAndHashCode(of = {"repository", "commitId"})
public class Commit implements Comparable<Commit> {

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

	@Override
	public int compareTo(Commit o) {
		return ComparisonChain.start()
			.compare(getCommitTime(), o.getCommitTime(), Ordering.natural().nullsLast())
			.result();
	}
}
