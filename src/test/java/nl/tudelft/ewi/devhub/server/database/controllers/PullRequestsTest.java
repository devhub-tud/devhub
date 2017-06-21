package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.Inject;

import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.PersistedBackendTest;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
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
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoRule;

import static nl.tudelft.ewi.devhub.webtests.utils.EntityEqualsMatcher.isEntity;
import static org.junit.Assert.assertThat;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class PullRequestsTest extends PersistedBackendTest {

	// Ensure @Mock fields are initialized
 	@Rule public MockitoRule mockitoRule =  MockitoJUnit.rule();

	@Inject @Getter private Groups groups;
	@Inject @Getter private CourseEditions courses;
	@Inject @Getter private Users users;
	@Inject private Commits commits;

	// Jukito injects a mock here because RepositoriesApi is not bound to any implementation in TestDatabaseModule
 	@Inject RepositoriesApi repositoriesApi;
 	// Create other mocks for stubs in before()
	@Mock RepositoryApi repositoryApi;
 	@Mock CommitApi commitApi;
 	DiffModel diffModel = new DiffModel();

	@Inject
	private PullRequests pullRequests;

	private final static String COMMIT_A = "65191cfaca61fe538612122151a7297e34f01178";
	private final static String COMMIT_B = "55c4656b98bf694c288918a82c8193eb83a33353";

	private Group group;

	@Before
	public void setUpGroup() {

		group = createGroup(createCourseEdition(), createUser());

		when(repositoriesApi.getRepository(Mockito.anyString())).thenReturn(repositoryApi);
		when(repositoryApi.getCommit(COMMIT_A)).thenReturn(commitApi);
		when(repositoryApi.getCommit(COMMIT_B)).thenReturn(commitApi);
		when(commitApi.diff()).thenReturn(diffModel);
		diffModel.setDiffs(Lists.newArrayList());
	}

	@Test
	public void testCreatePullRequest() {
		GroupRepository groupRepository = group.getRepository();
		PullRequest pr = new PullRequest();
		pr.setIssueId(random.nextLong());
		pr.setRepository(groupRepository);
		pr.setBranchName("super-branch");
		pr.setOpen(true);
		pr.setDestination(commits.ensureExists(groupRepository, COMMIT_A));
		pr.setMergeBase(commits.ensureExists(groupRepository, COMMIT_B));
		pr.setTitle("super-branch");

		pullRequests.persist(pr);
		assertThat(
			pullRequests.findOpenPullRequest(groupRepository, "super-branch").get(),
			isEntity(pr)
		);
	}
	
}
