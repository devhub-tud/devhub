package nl.tudelft.ewi.devhub.server.backend;

import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

import nl.tudelft.ewi.devhub.server.database.controllers.GroupMemberships;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupMembership;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.client.GitServerClientMock;
import nl.tudelft.ewi.git.models.MockedRepositoryModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ProjectsBackendTest {
	
	private static final GroupMemberships groupMemberships = mock(GroupMemberships.class);
	private static final Groups groups = mock(Groups.class);
	private static final Users users = mock(Users.class);
	private static final GitServerClientMock gitClient = new GitServerClientMock();
	private final static Random random = new Random();
	
	private final ProjectsBackend projectsBackend = new ProjectsBackend(
			new ValueProvider<GroupMemberships>(groupMemberships),
			new ValueProvider<Groups>(groups),
			new ValueProvider<Users>(users),
			gitClient);

	private Course course;
	
	private User user;
	
	@Before
	public void beforeTest() {
		course = createCourse();
		user = createUser();
		when(groups.find(course)).thenReturn(Lists.newArrayList());
		when(groupMemberships.ofGroup(Mockito.any(Group.class)))
			.thenReturn(Lists.newArrayList());
	}
	
	@Test
	public void testCreateProject() throws ApiError {
		projectsBackend.setupProject(course, Lists.newArrayList(user));
		ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
		verify(groups).persist(groupCaptor.capture());
		Group group = groupCaptor.getValue();
		verifyPersistedGroup(group, course, user);
		verifyProvisionRepository(group, user);
	}
	
	@Test(expected=ApiError.class)
	public void testAlreadyParticipating() throws ApiError {
		putUserInCourse(user, course);
		projectsBackend.setupProject(course, Lists.newArrayList(user));
	}
	
	protected static void putUserInCourse(User user, Course course) {
		Group group = new Group();
		group.setCourse(course);
		GroupMembership groupMembership = new GroupMembership();
		groupMembership.setUser(user);
		groupMembership.setGroup(group);
		group.setMemberships(Sets.newHashSet(groupMembership));
		user.setMemberOf(Lists.newArrayList(groupMembership));
		when(groupMemberships.ofGroup(group))
			.thenReturn(Lists.newArrayList(groupMembership));
	}
	
	protected static void verifyPersistedGroup(Group group, Course course, User... members) {
		assertNotNull(group);
		assertEquals(course, group.getCourse());
		for(User member : members)
			verifyPersistedGroupMembership(member, group);
	}
	
	protected static void verifyPersistedGroupMembership(User user, Group group) {
		GroupMembership expected = new GroupMembership();
		expected.setUser(user);
		expected.setGroup(group);
		verify(groupMemberships).persist(expected);	
	}
	
	protected static void verifyProvisionRepository(Group group, User... members) {
		MockedRepositoryModel repository =
			gitClient.repositories().retrieve(group.getRepositoryName());
		assertNotNull(repository);
		for(User member : members) {
			assertThat(repository.getPermissions(), hasKey(member.getNetId()));
		}
	}
	
	protected Course createCourse() {
		Course course = new Course();
		course.setCode(randomString().substring(0,4));
		course.setName(randomString());
		course.setStart(new Date());
		course.setMinGroupSize(2);
		course.setMaxGroupSize(2);
		return course;
	}
	
	protected User createUser() {
		User user = new User();
		user.setMemberOf(Lists.newArrayList());
		user.setNetId(randomString());
		return user;
	}
	
	protected String randomString() {
		return new BigInteger(130, random).toString(32);
	}

}
