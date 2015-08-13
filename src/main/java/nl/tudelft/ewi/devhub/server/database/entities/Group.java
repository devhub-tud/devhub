package nl.tudelft.ewi.devhub.server.database.entities;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.*;

@Data
@Entity
@Table(name = "groups")
@EqualsAndHashCode(of = { "groupId" })
public class Group implements Serializable {

	@Data
	@Embeddable
	public static class GroupId implements Serializable {

		@Setter(AccessLevel.PROTECTED)
		@Getter(AccessLevel.PROTECTED)
		@Column(name = "course_edition_id")
		private long courseEditionId;

		@Column(name = "group_number")
		private long groupNumber;

	}

	@Delegate
	@EmbeddedId
	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	private GroupId groupId = new GroupId();

	@NotNull
	@ManyToOne
	@MapsId("courseEditionId")
	private CourseEdition course;

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
				getCourse().getCode(),
				getCourse().getName(),
				getGroupNumber());
	}

}
