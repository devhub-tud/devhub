package nl.tudelft.ewi.devhub.server.database.entities;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.*;

import com.google.common.collect.ComparisonChain;
import lombok.*;
import nl.tudelft.ewi.devhub.server.database.entities.identity.FKSegmentedIdentifierGenerator;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Data
@Entity
@Table(name = "groups")
@IdClass(Group.GroupId.class)
@ToString(of = {"courseEdition", "groupNumber"})
@EqualsAndHashCode(of = {"courseEdition", "groupNumber" })
public class Group implements Comparable<Group>, Serializable {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class GroupId implements Serializable {

		private long courseEdition;

		private long groupNumber;

	}

	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "course_edition_id")
	private CourseEdition courseEdition;

	@Id
	@GenericGenerator(name = "seq_group_number", strategy = "nl.tudelft.ewi.devhub.server.database.entities.identity.FKSegmentedIdentifierGenerator", parameters = {
		@Parameter(name = FKSegmentedIdentifierGenerator.TABLE_PARAM, value = "seq_group_number"),
		@Parameter(name = FKSegmentedIdentifierGenerator.CLUSER_COLUMN_PARAM, value = "course_edition_id"),
		@Parameter(name = FKSegmentedIdentifierGenerator.PROPERTY_PARAM, value = "courseEdition")
	})
	@GeneratedValue(generator = "seq_group_number")
	@Column(name = "group_number", nullable = false)
	private long groupNumber;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "repository_id", referencedColumnName = "id", unique = true)
	private GroupRepository repository;

	@ManyToMany
	@JoinTable(
		name="group_memberships",
		joinColumns={
			@JoinColumn(name="course_edition_id", referencedColumnName="course_edition_id"),
			@JoinColumn(name="group_number", referencedColumnName="group_number")
		},
		inverseJoinColumns={
			@JoinColumn(name="user_id", referencedColumnName="id")
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

	@Override
	public int compareTo(Group o) {
		return ComparisonChain.start()
			.compare(getCourseEdition(), o.getCourseEdition())
			.compare(getGroupNumber(), o.getGroupNumber())
			.result();
	}
}
