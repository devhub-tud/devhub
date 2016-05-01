package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.Base;
import nl.tudelft.ewi.devhub.server.database.entities.identity.FKSegmentedIdentifierGenerator;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.Warning;

import com.google.common.collect.ComparisonChain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "groups")
@IdClass(Group.GroupId.class)
@ToString(of = {"courseEdition", "groupNumber"})
@EqualsAndHashCode(of = {"courseEdition", "groupNumber" })
public class Group implements Comparable<Group>, Serializable, Base {

	public static final String GROUPS_PATH_PART = "groups/";

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class GroupId implements Serializable {

		private long courseEdition;

		private long groupNumber;

	}

	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "course_edition_id", referencedColumnName = "id", nullable = false)
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

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "repository_id", referencedColumnName = "id", unique = true)
	private GroupRepository repository;

	@ManyToMany(fetch = FetchType.LAZY)
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

	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	@OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<Delivery> deliveries;

	public String getGroupName() {
		return String.format("%s (Group %d)",
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

	@Override
	public URI getURI() {
		return getCourseEdition().getURI().resolve(GROUPS_PATH_PART).resolve(getGroupNumber() + "/");
	}

}
