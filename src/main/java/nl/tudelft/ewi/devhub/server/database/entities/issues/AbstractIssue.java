package nl.tudelft.ewi.devhub.server.database.entities.issues;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.Base;
import nl.tudelft.ewi.devhub.server.database.entities.Event;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.comments.IssueComment;
import nl.tudelft.ewi.devhub.server.database.entities.identity.FKSegmentedIdentifierGenerator;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Sets;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Jan-Willem on 8/11/2015.
 */
@Data
@Entity
@Table(name="repository_issues")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("CASE WHEN branch_name IS NOT NULL THEN 'PULL_REQUEST' " +
	"WHEN branch_name IS NULL THEN 'ISSUE' end")
@ToString(of = {"repository", "issueId"})
@EqualsAndHashCode(of = {"repository", "issueId"})
@IdClass(AbstractIssue.IssueId.class)
public abstract class AbstractIssue implements Event, Base {

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
		@org.hibernate.annotations.Parameter(name = FKSegmentedIdentifierGenerator.CLUSER_COLUMN_PARAM, value = "repository_id"),
		@org.hibernate.annotations.Parameter(name = FKSegmentedIdentifierGenerator.PROPERTY_PARAM, value = "repository")
	})
	@GeneratedValue(generator = "seq_issue_id")
	@Column(name = "issue_id")
	private long issueId;

    @Column(name="open")
    private boolean open;

	@CreationTimestamp
	@Column(name = "created_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	@Column(name = "closed_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date closed;

	@NotEmpty
	@Column(name="title")
	private String title;

	@Column(name="description", nullable=true)
	@Type(type = "org.hibernate.type.TextType")
	private String description;
	
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "assignee")
	private User assignee;
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "issue_labels", joinColumns = 
		{ 
			@JoinColumn(name = "repository_id", referencedColumnName = "repository_id", nullable = false),
			@JoinColumn(name = "issue_id", referencedColumnName = "issue_id", nullable = false)
		}, 
		inverseJoinColumns = { 
			@JoinColumn(name = "label_id", referencedColumnName = "label_id", nullable = false)
		}
	)
	private Set<IssueLabel> labels = Sets.newHashSet();

	@OrderBy("timestamp ASC")
	@OneToMany(mappedBy = "issue", fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REMOVE}, orphanRemoval = true)
	private List<IssueComment> comments;

    /**
     * @return true if the pull request is closed
     */
    public boolean isClosed() {
        return !isOpen();
    }

	public void addLabel(IssueLabel issueLabel) {
		labels.add(issueLabel);
	}

}
