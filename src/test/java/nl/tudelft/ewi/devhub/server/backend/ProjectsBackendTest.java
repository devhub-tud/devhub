package nl.tudelft.ewi.devhub.server.backend;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel;

import org.hamcrest.Matchers;
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
	
	private static final Groups groups = mock(Groups.class);
	private static GitServerClient gitClient;
	private ProjectsBackend projectsBackend;

	private CourseEdition course;
	
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
		projectsBackend = new ProjectsBackend(new ValueProvider(groups), gitClient);

		course = createCourse();
		user = createUser();
		when(groups.find(course)).thenReturn(Lists.<Group>newArrayList());
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
		expectedRepoModel.setName(group.getRepository().getRepositoryName());
		expectedRepoModel.setTemplateRepository(course.getTemplateRepositoryUrl());
		expectedRepoModel.setPermissions(expectedPermissions);
		verify(gitClient.repositories()).create(expectedRepoModel);
	}

	@Test(expected=ApiError.class)
	public void testAlreadyParticipating() throws ApiError {
		putUserInCourse(user, course);
		projectsBackend.setupProject(course, Lists.newArrayList(user));
	}
	
	protected static void putUserInCourse(User user, CourseEdition course) {
		Group group = new Group();
		group.setMembers(Sets.newHashSet(user));
		group.setCourseEdition(course);
		course.setGroups(Lists.newArrayList(group));
	}
	
	protected static void verifyPersistedGroup(Group group, CourseEdition course, User... members) {
		assertNotNull(group);
		assertEquals(course, group.getCourse());
		assertThat(group.getMembers(), Matchers.contains(members));
	}

}
