package nl.tudelft.ewi.devhub.server.database.entities.issues;

import lombok.*;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;

import javax.persistence.*;

/**
 * Created by Jan-Willem on 8/11/2015.
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(of = {"id"})
public abstract class AbstractIssue {

    @Data
    @Embeddable
    public static class IssueId {

        @Column(name = "repository_id")
        @Setter(AccessLevel.PROTECTED)
        @Getter(AccessLevel.PROTECTED)
        private long repositoryId;

        @Column(name = "issue_id")
        private long issueId;

    }

    @Delegate
    @EmbeddedId
    private IssueId id = new IssueId();

    @ManyToOne(optional = false)
    @MapsId("repositoryId")
    private RepositoryEntity repository;

    @Column(name="open")
    private boolean open;

    /**
     * @return true if the pull request is closed
     */
    public boolean isClosed() {
        return !isOpen();
    }

}
