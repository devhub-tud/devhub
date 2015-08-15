package nl.tudelft.ewi.devhub.server.database.entities.issues;

import lombok.*;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Jan-Willem on 8/11/2015.
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(of = {"id"})
@IdClass(AbstractIssue.IssueId.class)
public abstract class AbstractIssue {

    @Data
    @NoArgsConstructor
	@AllArgsConstructor
    public static class IssueId implements Serializable {

        private long repository;

        private long issueId;

    }

	@Id
    @ManyToOne(optional = false)
	@JoinColumn(name = "repository_id")
    private RepositoryEntity repository;

	@Id
	@Column(name = "issue_id")
	private long issueId;

    @Column(name="open")
    private boolean open;

    /**
     * @return true if the pull request is closed
     */
    public boolean isClosed() {
        return !isOpen();
    }

}
