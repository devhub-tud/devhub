package nl.tudelft.ewi.devhub.server.database.entities;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.database.Configurable;
import nl.tudelft.ewi.devhub.server.database.entities.builds.BuildInstructionEntity;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.INTEGER)
public abstract class RepositoryEntity implements Configurable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotEmpty
    @Column(name = "repository_name", unique = true)
    private String repositoryName;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "build_instruction", nullable = true)
    private BuildInstructionEntity buildInstruction;

    /**
     * @return a list of collaborators for this repository.
     * For a {@link GroupRepository} these are the {@link User Users} in the {@link Group}.
     */
    public abstract Collection<User> getCollaborators();

    /**
     * @return the title for the repository. This may be editable or managed.
     */
    public abstract String getTitle();

    /**
     * @return the base path for URI's within Devhub
     */
    public abstract URI getDevHubURI();

    /**
     * @return the custom properties for this repository. The properties may
     * be inherited from the {@link Course}.
     */
    public Map<String, String> getProperties() {
        return ImmutableMap.of();
    }

}
