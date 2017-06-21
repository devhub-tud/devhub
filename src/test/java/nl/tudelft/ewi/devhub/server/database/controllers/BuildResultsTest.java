package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.Inject;
import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.PersistedBackendTest;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.assertj.core.util.Lists;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by Jan-Willem on 8/28/2015.
 */
@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class BuildResultsTest extends PersistedBackendTest {

	// Ensure @Mock fields are initialized
	@Rule public MockitoRule mockitoRule =  MockitoJUnit.rule();

	@Inject private BuildResults buildResults;
	@Inject private Commits commits;
	@Inject @Getter private CourseEditions courses;
	@Inject @Getter private Users users;
	@Inject @Getter private Groups groups;

	// Jukito injects a mock here because RepositoriesApi is not bound to any implementation in TestDatabaseModule
	@Inject RepositoriesApi repositoriesApi;
	// Create other mocks for stubs in before()
	@Mock RepositoryApi repositoryApi;
	@Mock CommitApi commitApi;
	DiffModel diffModel = new DiffModel();

	private Commit commit;
	private RepositoryEntity repository;
	private Group group;
	private CourseEdition courseEdition;

	private final static String COMMIT_ID = "06bd8721495e682eb63b5a8781e70ad02a97874b";

	@Before
	public void before() {
		when(repositoriesApi.getRepository(Mockito.anyString())).thenReturn(repositoryApi);
		when(repositoryApi.getCommit(COMMIT_ID)).thenReturn(commitApi);
		when(commitApi.diff()).thenReturn(diffModel);
		diffModel.setDiffs(Lists.newArrayList());

		courseEdition = createCourseEdition();
		group = createGroup(courseEdition, createUser(), createUser());
		repository = group.getRepository();
		commit = commits.ensureExists(repository, COMMIT_ID);
	}

	@Test
	public void testPersistBuildResult() {
		BuildResult buildResult = BuildResult.newBuildResult(commit);
		buildResults.persist(buildResult);
		commits.refresh(commit);
		assertEquals(commit.getBuildResult(), buildResult);
	}

}
