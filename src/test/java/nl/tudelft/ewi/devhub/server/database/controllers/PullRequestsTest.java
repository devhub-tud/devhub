package nl.tudelft.ewi.devhub.server.database.controllers;

import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.PersistedBackendTest;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;

import com.google.inject.Inject;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class PullRequestsTest extends PersistedBackendTest {

	@Inject @Getter private Groups groups;
	@Inject @Getter private CourseEditions courses;
	@Inject @Getter private Users users;
	
	@Inject
	private PullRequests pullRequests;

	private final static String COMMIT_A = "65191cfaca61fe538612122151a7297e34f01178";
	private final static String COMMIT_B = "55c4656b98bf694c288918a82c8193eb83a33353";

	private Group group;

	@Before
	public void setUpGroup() {
		group = createGroup(createCourseEdition(), createUser());
	}

	@Test
	public void testCreatePullRequest() {
		GroupRepository groupRepository = group.getRepository();
		PullRequest pr = new PullRequest();
		pr.setIssueId(random.nextLong());
		pr.setRepository(groupRepository);
		pr.setBranchName("super-branch");
		pr.setOpen(true);
		pr.setDestination(COMMIT_A);
		pr.setMergeBase(COMMIT_B);

		pullRequests.persist(pr);
		pullRequestEquals(pr, pullRequests.findOpenPullRequest(groupRepository, "super-branch"));
	}

	private static void pullRequestEquals(PullRequest expected, PullRequest actual) {
		try {
			assertEquals(expected.getBranchName(), actual.getBranchName());
			assertEquals(expected.getRepository(), actual.getRepository());
			assertEquals(expected.isOpen(), actual.isOpen());
		}
		catch(AssertionError e) {
			throw new AssertionError(String.format("Expected %s but was %s",
					expected, actual), e);
		}
	}
	
}
