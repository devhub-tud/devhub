package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import nl.tudelft.ewi.devhub.server.database.entities.notifications.Notification;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.NotificationsToUsers;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
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

	@OneToMany(mappedBy = "user")
	@JsonIgnore
	private List<NotificationsToUsers> notificationsToUsersList;

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

	public List<Notification> getNotifications() {
		List<Notification> notifications = new ArrayList<Notification>();
		for (NotificationsToUsers notificationsToUsers: notificationsToUsersList) {
			notifications.add(notificationsToUsers.getNotification());
		}
		return notifications;
	}

	public boolean hasUnreadNotifications() {
		for(NotificationsToUsers notificationUser: notificationsToUsersList) {
			if (!notificationUser.isRead()) return true;
		}
		return false;
	}
	
}
