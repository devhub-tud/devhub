package nl.tudelft.ewi.devhub.server.database.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.Base;
import nl.tudelft.ewi.devhub.server.database.Configurable;
import nl.tudelft.ewi.devhub.server.database.entities.builds.BuildInstructionEntity;
import nl.tudelft.ewi.devhub.server.database.entities.issues.AbstractIssue;
import nl.tudelft.ewi.devhub.server.database.entities.issues.IssueLabel;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.Watchable;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.Warning;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * The {@code Repository} information was previously stored within the
 * {@link Group} entity. Now {@code Repository} has been separated from its {@code Group}.
 * This allows for the following future improvements:
 *
 * <ul>
 *     <li>Multiple repositories per group.</li>
 *     <li>Personal (not group nor course bound) repositories.</li>
 *     <li>Separate repository from group provisioning.</li>
 * </ul>
 *
 * The {@code RepositoryEntity} is currently implemented as an extendable {@code Entity}.
 * This is so foreign key relationships can be made to abstractions of repositories.
 */
@Data
@Entity
@Table(name = "repository")
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "repositoryName"})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.INTEGER)
public abstract class RepositoryEntity implements Configurable, Base, Watchable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotEmpty
    @Pattern(regexp = "^\\w[\\w._@/+-]*[\\w._@+-]$")
    @Column(name = "repository_name", unique = true)
    private String repositoryName;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "build_instruction")
    private BuildInstructionEntity buildInstruction;

    @JsonIgnore
	@Setter(AccessLevel.NONE)
	@OneToMany(mappedBy = "repository", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<Commit> commits;

	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	@OneToMany(mappedBy = "repository", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<Warning> warnings;

    /**
     * Mapping to issues and pull requests. Do not use this method, but use
     * {@link nl.tudelft.ewi.devhub.server.database.controllers.Issues} and
     * {@link nl.tudelft.ewi.devhub.server.database.controllers.PullRequests} instead.
     */
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "repository", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<AbstractIssue> issues;
	
	@OneToMany(mappedBy = "repository", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<IssueLabel> labels;

    /**
     * @return a list of collaborators for this repository.
     * For a {@link GroupRepository} these are the {@link User Users} in the {@link Group}.
     */
    public abstract Collection<User> getCollaborators();

    /**
     * @return the title for the repository. This may be editable or managed.
     */
    public abstract String getTitle();

    @JsonIgnore
    public Set<User> getWatchers() {
        return Sets.newHashSet(getCollaborators());
    }

}
