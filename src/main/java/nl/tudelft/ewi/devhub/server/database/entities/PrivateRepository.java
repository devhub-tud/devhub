package nl.tudelft.ewi.devhub.server.database.entities;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * A {@code PrivateRepository} is a {@link RepositoryEntity} that is owned be a {@link User},
 * can be shared with {@link PrivateRepository#getCollaborators() others} and is <strong>not</strong> managed
 * by a {@link Course}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@DiscriminatorValue("2")
@EqualsAndHashCode(callSuper = true)
public class PrivateRepository extends RepositoryEntity {

	@NotNull
	@ManyToOne
	@JoinColumn(name = "owner_id")
	private User owner;

	@NotNull
	@Column(name = "repository_title")
	private String title;

	@ManyToMany
	@JoinTable(
		name="private_repository_collaborators",
		joinColumns={@JoinColumn(name="repository_id", referencedColumnName="id")},
		inverseJoinColumns={@JoinColumn(name="user_id", referencedColumnName="id")
	})
	private List<User> collaborators;

	@ElementCollection
	@JoinTable(name="private_repository_properties", joinColumns=@JoinColumn(name="repository_id", referencedColumnName = "id"))
	@MapKeyColumn(name="property_key")
	@Column(name="property_value")
	private Map<String, String> properties;

	@Override
	public URI getURI() {
		return URI.create("/projects/").resolve(getOwner().getNetId() + "/").resolve(getTitle() + "/");
	}

}
