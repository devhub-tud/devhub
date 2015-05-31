package nl.tudelft.ewi.devhub.server.database.entities;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.mindrot.jbcrypt.BCrypt;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Data
@Entity
@EqualsAndHashCode(of = { "netId" })
@ToString(of = { "netId" })
@Table(name = "users")
public class User {

	private static final Comparator<Group> GROUP_COMPARATOR = new Comparator<Group>() {
		@Override
		public int compare(Group group1, Group group2) {
			Course course1 = group1.getCourse();
			Course course2 = group2.getCourse();
			String code1 = course1.getCode();
			String code2 = course2.getCode();
			int compare = code1.compareTo(code2);
			if (compare != 0) {
				return compare;
			}
			
			return (int) (group1.getGroupNumber() - group2.getGroupNumber());
		}
	};

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotNull
	@Size(max = 32)
	@Column(name = "net_id", length = 32, nullable = false)
	private String netId;

	@Size(max = 128)
	@Column(name = "name", length = 128, nullable = true)
	private String name;

	@Size(max = 255)
	@Column(name = "email", length = 255, nullable = true)
	private String email;

	@Size(max = 20)
	@Column(name = "student_number", length = 20, nullable = true)
	private String studentNumber;
	
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Column(name = "password")
	private String password;
	
	@Column(name = "admin")
	private boolean admin;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<GroupMembership> memberOf;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<CourseAssistant> assists;

	public List<Group> listGroups() {
		List<Group> groups = Lists.newArrayList();
		for (GroupMembership membership : memberOf) {
			groups.add(membership.getGroup());
		}

		Collections.sort(groups, GROUP_COMPARATOR);
		return groups;
	}
	
	public List<Group> listAssistedGroups() {
		List<Group> groups = Lists.newArrayList();
		for (CourseAssistant assist : assists) {
			groups.addAll(assist.getCourse().getGroups());
		}

		Collections.sort(groups, GROUP_COMPARATOR);
		return groups;
	}

	public boolean isMemberOf(Group group) {
		for (GroupMembership membership : getMemberOf()) {
			if (group.equals(membership.getGroup())) {
				return true;
			}
		}
		return false;
	}

	public boolean isAssisting(Course course) {
		for (CourseAssistant assistant : getAssists()) {
			Course assistedCourse = assistant.getCourse();
			if (assistedCourse.getId() == course.getId()) {
				return true;
			}
		}
		return false;
	}

	public boolean isParticipatingInCourse(Course course) {
		for (GroupMembership membership : getMemberOf()) {
			Group group = membership.getGroup();
			if (course.equals(group.getCourse())) {
				return true;
			}
		}
		return false;
	}
	
	public void setPassword(String password) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(password));
		this.password = BCrypt.hashpw(password, BCrypt.gensalt());
	}
	
	public boolean isPasswordMatch(String password) {
		return password != null && this.password != null
				&& BCrypt.checkpw(password, this.password);
	}
	
}
