package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
@EqualsAndHashCode(of = { "netId" })
@ToString(of = { "netId" })
@Table(name = "users")
public class User {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotNull
	@Size(max = 32)
	@Column(name = "net_id", length = 32, nullable = false, unique = true)
	private String netId;

	@Size(max = 128)
	@Column(name = "name", length = 128, nullable = true)
	private String name;

	@Size(max = 255)
	@Column(name = "email", length = 255, nullable = true)
	private String email;

	@Size(max = 20)
	@Column(name = "student_number", length = 20, nullable = true, unique = true)
	private String studentNumber;

	@Basic(fetch = FetchType.LAZY)
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Column(name = "password")
	private String password;
	
	@Column(name = "admin")
	private boolean admin;

	@ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
	private List<Group> groups;

	@ManyToMany(mappedBy = "assistants", fetch = FetchType.LAZY)
	private Set<CourseEdition> assists;

	@Deprecated
	public List<Group> listGroups() {
		return getGroups();
	}
	
	public List<Group> listAssistedGroups() {
		return getAssists().stream()
			.map(CourseEdition::getGroups)
			.flatMap(Collection::stream)
			.sorted(Comparator.naturalOrder())
			.collect(Collectors.toList());
	}

	public boolean isMemberOf(Group group) {
		return getGroups().contains(group);
	}

	public boolean isAssisting(CourseEdition course) {
		return getAssists().contains(course);
	}

	public boolean isParticipatingInCourse(CourseEdition course) {
		return getGroups().stream()
			.map(Group::getCourseEdition)
			.anyMatch(course::equals);
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
