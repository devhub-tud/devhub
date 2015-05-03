package nl.tudelft.ewi.devhub.server.backend;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import nl.tudelft.ewi.devhub.server.database.controllers.GroupMemberships;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupMembership;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ProjectsBackendTest extends BackendTest {
	
	private static final GroupMemberships groupMemberships = mock(GroupMemberships.class);
	private static final Groups groups = mock(Groups.class);
	private static final Users users = mock(Users.class);
	private static GitServerClient gitClient;
	private ProjectsBackend projectsBackend;

	private Course course;
	
	private User user;

	@BeforeClass
	public static void initGitServerClientMock() {
		gitClient = Mockito.mock(GitServerClient.class);
		when(gitClient.users()).thenReturn(mock(nl.tudelft.ewi.git.client.Users.class));
		when(gitClient.groups()).thenReturn(mock(nl.tudelft.ewi.git.client.Groups.class));
		when(gitClient.repositories()).thenReturn(mock(Repositories.class));
	}
	
	@Before
	public void beforeTest() {
		projectsBackend = new ProjectsBackend(
				new ValueProvider<GroupMemberships>(groupMemberships),
				new ValueProvider<Groups>(groups),
				new ValueProvider<Users>(users),
				gitClient);

		course = createCourse();
		user = createUser();
		when(groups.find(course)).thenReturn(Lists.<Group>newArrayList());
		when(groupMemberships.ofGroup(Mockito.any(Group.class)))
			.thenReturn(Lists.<GroupMembership>newArrayList());
	}
	
	@Test
	public void testCreateProject() throws ApiError, GitClientException {
		projectsBackend.setupProject(course, Lists.newArrayList(user));
		ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
		verify(groups).persist(groupCaptor.capture());
		Group group = groupCaptor.getValue();
		verifyPersistedGroup(group, course, user);
		verifyProvisionRepository(group);
	}

	private void verifyProvisionRepository(Group group) throws GitClientException {
		Map<String, RepositoryModel.Level> expectedPermissions =
			ImmutableMap.of(
					user.getNetId(), RepositoryModel.Level.READ_WRITE,
					"@" + course.getCode().toLowerCase(), RepositoryModel.Level.ADMIN);

		CreateRepositoryModel expectedRepoModel = new CreateRepositoryModel();
		expectedRepoModel.setName(group.getRepositoryName());
		expectedRepoModel.setTemplateRepository(course.getTemplateRepositoryUrl());
		expectedRepoModel.setPermissions(expectedPermissions);
		verify(gitClient.repositories()).create(expectedRepoModel);
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

}
