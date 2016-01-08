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
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.UserModel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import nl.tudelft.ewi.git.web.api.GroupsApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.UsersApi;
import org.hamcrest.Matchers;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.ws.rs.NotFoundException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(JukitoRunner.class)
@UseModules(ProjectsBackendTest.ProjectsBackendTestModule.class)
public class ProjectsBackendTest extends PersistedBackendTest {

	private static RepositoriesApi repositoriesApi = mock(RepositoriesApi.class);
	private static UsersApi usersApi = mock(UsersApi.class);

	public static class ProjectsBackendTestModule extends AbstractModule {

		@Override
		protected void configure() {
			install(new TestDatabaseModule());
			bind(RepositoriesApi.class).toInstance(repositoriesApi);
			bind(UsersApi.class).toInstance(usersApi);
		}

	}
	
	@Inject private ProjectsBackend projectsBackend;
	@Inject @Getter private CourseEditions courses;
	@Inject @Getter private Users users;
	@Inject @Getter private Groups groups;
	@Inject GroupsApi groupsApi;

	private CourseEdition course;
	private User user;

	@Before
	public void beforeTest() {
		reset(repositoriesApi, usersApi);
		course = createCourseEdition();
		user = createUser();

		Mockito.doThrow(new NotFoundException()).when(groupsApi).getGroup(anyString());
	}

	@Override
	protected User createUser() {
		User user = super.createUser();
		UserModel userModel = new UserModel();
		userModel.setName(user.getNetId());
		usersApi.createNewUser(userModel);
		return user;
	}

	@Test
	public void testCreateProject() throws ApiError {
		Group group = projectsBackend.setupProject(course, Lists.newArrayList(user));
		verifyPersistedGroup(group, course, user);
		verifyProvisionRepository(group);
	}

	private void verifyProvisionRepository(Group group) {
		Map<String, RepositoryModel.Level> expectedPermissions =
			ImmutableMap.of(
				user.getNetId(), RepositoryModel.Level.READ_WRITE);

		CreateRepositoryModel expectedRepoModel = new CreateRepositoryModel();
		expectedRepoModel.setName(group.getRepository().getRepositoryName());
		expectedRepoModel.setTemplateRepository(course.getTemplateRepositoryUrl());
		expectedRepoModel.setPermissions(expectedPermissions);
		verify(repositoriesApi).createRepository(expectedRepoModel);
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
