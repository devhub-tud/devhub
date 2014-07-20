package nl.tudelft.ewi.devhub.server.database.entities;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
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
	@Column(name = "net_id")
	private String netId;

	@Column(name = "name")
	private String name;

	@Column(name = "email")
	private String email;
	
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Column(name = "password")
	private String password;

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Column(name = "salt")
	private String salt;

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
		for (GroupMembership membership : memberOf) {
			if (group.equals(membership.getGroup())) {
				return true;
			}
		}
		return false;
	}

	public boolean isAssisting(Course course) {
		for (CourseAssistant assistant : assists) {
			Course assistedCourse = assistant.getCourse();
			if (assistedCourse.getId() == course.getId()) {
				return true;
			}
		}
		return false;
	}

	public boolean isParticipatingInCourse(Course course) {
		for (GroupMembership membership : memberOf) {
			Group group = membership.getGroup();
			if (course.equals(group.getCourse())) {
				return true;
			}
		}
		return false;
	}
	
	private static Random random = new SecureRandom();
	
	public void setPassword(String password) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(password));
		synchronized(random) {
			this.salt = SHA512(new BigInteger(130, random).toString(32));
		}
		this.password = SHA512(SHA512(password).concat(this.salt));
	}
	
	public boolean isPasswordMatch(String password) {
		return password != null && this.password != null && this.salt != null
				&& SHA512(SHA512(password).concat(this.salt)).equals(this.password);
	}
	
	@SneakyThrows
	private static String SHA512(String toHash) {
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		byte[] hash = md.digest(toHash.getBytes("UTF-8"));
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			sb.append(Integer.toString((hash[i] & 0xFF) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
}
