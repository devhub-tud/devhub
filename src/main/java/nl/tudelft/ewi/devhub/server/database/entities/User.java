package nl.tudelft.ewi.devhub.server.database.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.*;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.Notification;
import org.hibernate.annotations.Immutable;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;
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
	@Column(name = "name", length = 128)
	private String name;

	@Size(max = 255)
	@Column(name = "email")
	private String email;

	@Size(max = 20)
	@Column(name = "student_number", length = 20, unique = true)
	private String studentNumber;

	@JsonIgnore
	@Basic(fetch = FetchType.LAZY)
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Column(name = "password")
	private String password;
	
	@Column(name = "admin")
	private boolean admin;

	@JsonIgnore
	@ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
	private List<Group> groups = new ArrayList<>();

	@JsonIgnore
	@ManyToMany(mappedBy = "assistants", fetch = FetchType.LAZY)
	private Set<CourseEdition> assists;

	@Deprecated
	@JsonIgnore
	public List<Group> listGroups() {
		return getGroups();
	}

	@Immutable
	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	@ManyToMany(cascade = CascadeType.REFRESH)
	@JoinTable(
			name = "notifications_to_users",
			joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "notification_id", referencedColumnName = "id")
	)
	public List<Notification> notifications;

	@JsonIgnore
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
