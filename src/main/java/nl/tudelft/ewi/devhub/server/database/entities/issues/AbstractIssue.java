package nl.tudelft.ewi.devhub.server.database.entities.issues;

import lombok.*;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.TimestampEvent;
import nl.tudelft.ewi.devhub.server.database.entities.identity.FKSegmentedIdentifierGenerator;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Parameter;
import java.io.Serializable;

/**
 * Created by Jan-Willem on 8/11/2015.
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(of = {"id"})
@IdClass(AbstractIssue.IssueId.class)
public abstract class AbstractIssue extends TimestampEvent {

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
	@GenericGenerator(name = "seq_issue_id", strategy = "nl.tudelft.ewi.devhub.server.database.entities.identity.FKSegmentedIdentifierGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = FKSegmentedIdentifierGenerator.TABLE_PARAM, value = "seq_issue_id"),
		@org.hibernate.annotations.Parameter(name = FKSegmentedIdentifierGenerator.CLUSER_COLUMN, value = "repository_id")
	})
	@GeneratedValue(generator = "seq_issue_id")
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
