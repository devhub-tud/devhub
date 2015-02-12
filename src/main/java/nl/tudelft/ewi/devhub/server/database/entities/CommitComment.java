package nl.tudelft.ewi.devhub.server.database.entities;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@ToString(exclude="commit")
@Table(name = "commit_comment")
@EqualsAndHashCode(of="commentId")
public class CommitComment implements Comparable<CommitComment> {
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long commentId;
	
	@NotNull
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="repository_name", referencedColumnName="repository_name"),
		@JoinColumn(name="commit_id", referencedColumnName="commit_id")
	})
	private Commit commit;

	@Column(name = "old_line_number")
	private Integer oldLineNumber;
	
	@Column(name = "old_file_path")
	private String oldFilePath;
	
	@Column(name = "new_line_number")
	private Integer newLineNumber;
	
	@Column(name = "new_file_path")
	private String newFilePath;
	
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
	public int compareTo(CommitComment o) {
		return time.compareTo(o.time);
	}
	
}
