package nl.tudelft.ewi.devhub.server.backend;

import lombok.Getter;
import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.TestDatabaseModule;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.GitServerClientMock;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.UserModel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.hamcrest.Matchers;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JukitoRunner.class)
@UseModules(ProjectsBackendTest.ProjectsBackendTestModule.class)
public class ProjectsBackendTest extends PersistedBackendTest {

	private static GitServerClientMock gitClient = mock(GitServerClientMock.class);
	private static nl.tudelft.ewi.git.client.UsersMock gitUsers = spy(new nl.tudelft.ewi.git.client.UsersMock());
	private static nl.tudelft.ewi.git.client.RepositoriesMock repositoriesMock = spy(new nl.tudelft.ewi.git.client.RepositoriesMock());
	private static nl.tudelft.ewi.git.client.GroupsMock gitGroups = spy(new nl.tudelft.ewi.git.client.GroupsMock());

	public static class ProjectsBackendTestModule extends AbstractModule {

		@Override
		protected void configure() {
			install(new TestDatabaseModule());
			bind(GitServerClient.class).to(GitServerClientMock.class);
			bind(GitServerClientMock.class).toInstance(gitClient);
		}

	}

	@BeforeClass
	public static void beforeClass() {
		when(gitClient.repositories()).thenReturn(repositoriesMock);
		when(gitClient.users()).thenReturn(gitUsers);
		when(gitClient.groups()).thenReturn(gitGroups);
	}

	@Inject private ProjectsBackend projectsBackend;
	@Inject @Getter private CourseEditions courses;
	@Inject @Getter private Users users;
	@Inject @Getter private Groups groups;

	private CourseEdition course;
	private User user;

	@Before
	public void beforeTest() {
		reset(repositoriesMock, gitUsers, gitGroups);
		course = createCourseEdition();
		user = createUser();
	}

	@Override
	protected User createUser() {
		User user = super.createUser();
		UserModel userModel = new UserModel();
		userModel.setName(user.getNetId());
		gitUsers.create(userModel);
		return user;
	}

	@Test
	public void testCreateProject() throws ApiError, GitClientException {
		Group group = projectsBackend.setupProject(course, Lists.newArrayList(user));
		verifyPersistedGroup(group, course, user);
		verifyProvisionRepository(group);
	}

	private void verifyProvisionRepository(Group group) throws GitClientException {
		Map<String, RepositoryModel.Level> expectedPermissions =
			ImmutableMap.of(
				user.getNetId(), RepositoryModel.Level.READ_WRITE);

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
