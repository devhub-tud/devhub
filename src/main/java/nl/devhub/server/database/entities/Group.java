package nl.devhub.server.database.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
@Entity
@Table(name = "groups")
public class Group {

	@Id
	@Column(name = "id")
	private long groupId;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "project_id")
	private Project project;

	@NotNull
	@Column(name = "group_number")
	private long groupNumber;

}
