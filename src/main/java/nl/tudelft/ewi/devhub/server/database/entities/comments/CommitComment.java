package nl.tudelft.ewi.devhub.server.database.entities.comments;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Data
@Entity
@ToString(exclude="commit")
@Table(name = "commit_comment")
@EqualsAndHashCode(of="commentId")
public class CommitComment extends Comment {

    /**
     * The commit to which this comment is attached.
     */
	@NotNull
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="repository_id", referencedColumnName="repository_id"),
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

}
