package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
@ToString(exclude="pullRequest")
@Table(name = "pull_request_comments")
@EqualsAndHashCode(of="commentId")
public class PullRequestComment implements Comparable<PullRequestComment> {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long commentId;

	@NotNull
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="issue_id", referencedColumnName="id"),
		@JoinColumn(name="repository_name", referencedColumnName="repository_name")
	})
	private PullRequest pullRequest;

	@Lob
	@NotEmpty
	@Basic(fetch=FetchType.LAZY)
	@Column(name = "content")
	private String content;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@NotNull
	@Column(name="time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date time;

	@Override
	public int compareTo(PullRequestComment o) {
		return time.compareTo(o.time);
	}

}
