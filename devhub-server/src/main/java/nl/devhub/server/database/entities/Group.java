package nl.devhub.server.database.entities;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;

import org.hibernate.validator.constraints.NotEmpty;

@Data
@Entity
@Table(name = "groups")
public class Group {

	@Id
	@Column(name = "group_id")
	private long groupId;
	
	@Id
	@NotNull
	@ManyToOne
	@JoinColumn(name = "project_id")
	private Project project;

	@NotEmpty
	@ManyToMany(mappedBy = "groups")
	private Set<User> members;
	
}
