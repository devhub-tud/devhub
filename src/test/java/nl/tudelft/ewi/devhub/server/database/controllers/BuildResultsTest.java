package nl.tudelft.ewi.devhub.server.database.controllers;

import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.PersistedBackendTest;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;

import com.google.inject.Inject;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Created by Jan-Willem on 8/28/2015.
 */
@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class BuildResultsTest extends PersistedBackendTest {

	@Inject private BuildResults buildResults;
	@Inject private Commits commits;
	@Inject @Getter private CourseEditions courses;
	@Inject @Getter private Users users;
	@Inject @Getter private Groups groups;

	private Commit commit;
	private RepositoryEntity repository;
	private Group group;
	private CourseEdition courseEdition;

	private final static String COMMIT_ID = "06bd8721495e682eb63b5a8781e70ad02a97874b";

	@Before
	public void before() {
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
