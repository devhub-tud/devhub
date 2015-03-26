package nl.tudelft.ewi.devhub.server.database.entities;

import java.util.Date;

import javax.persistence.*;
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

    /**
     * The commit to which this comment is attached.
     */
	@NotNull
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="repository_name", referencedColumnName="repository_name"),
		@JoinColumn(name="commit_id", referencedColumnName="commit_id")
	})
	private Commit commit;

    /**
     * The reference to the original commit, path and line number for the line
     * to which the comment is attached. For added lines, no special treatment
     * is required, as the original commit is the current commit. But for deleted
     * and context lines, we need to know their origin, in order to match it
     * efficiently with lines in aggregated diffs.
     */
    @Embedded
    private Source source;

    @Data
    @Embeddable
	@EqualsAndHashCode
    public static class Source {

        @NotNull
        @ManyToOne(fetch=FetchType.LAZY)
        @JoinColumns({
            @JoinColumn(name="source_repository_name", referencedColumnName="repository_name"),
            @JoinColumn(name="source_commit_id", referencedColumnName="commit_id")
        })
        private Commit sourceCommit;

        @NotNull
        @Column(name = "source_line_number")
        private Integer sourceLineNumber;

        @NotNull
        @Column(name = "source_file_path")
        private String sourceFilePath;

    }
	
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
