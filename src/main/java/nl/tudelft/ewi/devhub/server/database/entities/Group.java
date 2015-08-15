package nl.tudelft.ewi.devhub.server.database.entities;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.*;

@Data
@Entity
@Table(name = "groups")
@IdClass(Group.GroupId.class)
@EqualsAndHashCode(of = {"courseEdition", "groupNumber" })
public class Group implements Serializable {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class GroupId implements Serializable {

		private long courseEdition;

		private long groupNumber;

	}

	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "course_id")
	private CourseEdition courseEdition;

	@Id
	@Column(name = "group_number")
	private long groupNumber;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "repository_id", referencedColumnName = "id", unique = true)
	private GroupRepository repository;

	@ManyToMany
	@JoinTable(
		name="group_memberships",
		joinColumns={
				@JoinColumn(name="user_id", referencedColumnName="id")
		},
		inverseJoinColumns={
			@JoinColumn(name="course_edition_id", referencedColumnName="course_edition_id"),
			@JoinColumn(name="group_number", referencedColumnName="group_number")
		},
		uniqueConstraints = {
			@UniqueConstraint(name = "UNIQUE_GROUP_MEMBERSHIP_PER_COURSE", columnNames = {"user_id", "course_edition_id"})
		}
	)
	private Set<User> members;

	public String getGroupName() {
		return String.format("%s - %s (Group %d)",
				getCourseEdition().getCode(),
				getCourseEdition().getName(),
				getGroupNumber());
	}

	@Deprecated
	public CourseEdition getCourse() {
		return getCourseEdition();
	}

}
