package nl.tudelft.ewi.devhub.server.database.entities.issues;

import java.net.URI;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.ewi.devhub.server.database.entities.User;


@Data
@Entity
@Table(name="repository_issues")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Issue extends AbstractIssue {

	@NotNull	
	@Column(name="title", length=128)
	private String title;
	
	@NotNull
	@Column(name="description", length=2048)
	private String description; // Move this to AbstractIssue sometime?
	
	@ManyToOne(optional = true, cascade=CascadeType.DETACH)
	public User assignee;

	@Override
	public URI getURI() {
		return getRepository().getURI().resolve("issue/" + getIssueId() + "/");
	}
	
}
