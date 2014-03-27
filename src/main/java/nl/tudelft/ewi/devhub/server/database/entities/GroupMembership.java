package nl.tudelft.ewi.devhub.server.database.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@SuppressWarnings("serial")
@Table(name = "group_memberships")
public class GroupMembership implements Serializable {

	@Id
	@ManyToOne
	@JoinColumn(name = "group_id")
	private Group group;
	
	@Id
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
}
