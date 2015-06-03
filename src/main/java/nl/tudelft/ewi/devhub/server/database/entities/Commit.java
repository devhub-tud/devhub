package nl.tudelft.ewi.devhub.server.database.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name="commit")
@ToString(exclude="comments")
@IdClass(Commit.CommitId.class)
@EqualsAndHashCode(of={"repository", "commitId"})
public class Commit implements Comparable<Commit> {

	@Data
	@EqualsAndHashCode
	public static class CommitId implements Serializable {
		private Group repository;
		private String commitId;
	}

	@Id
	@ManyToOne
	@JoinColumn(name = "repository_name", referencedColumnName = "repository_name")
	private Group repository;
	
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
	
	@OneToMany(mappedBy = "commit", fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	private List<CommitComment> comments;

	@Override
	public int compareTo(Commit o) {
		return ComparisonChain.start()
			.compare(getCommitTime(), o.getCommitTime(), Ordering.natural().nullsLast())
			.result();
	}
}
