package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.Assert.assertEquals;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class PullRequestsTest {

	@Inject
	private Random random;
	
	@Inject
	private Groups groups;
	
	@Inject
	private Courses courses;
	
	@Inject
	private Users users;
	
	@Inject
	private PullRequests pullRequests;

	private final static String COMMIT_A = "65191cfaca61fe538612122151a7297e34f01178";
	private final static String COMMIT_B = "55c4656b98bf694c288918a82c8193eb83a33353";

	@Test
	public void testCreatePullRequest() {
		Group group = createGroup();
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

	protected Group createGroup() {
		Group group = new Group();
		CourseEdition course = getTestCourse();
		group.setCourseEdition(course);
		group = groups.persist(group);

		GroupRepository groupRepository = new GroupRepository();
		groupRepository.setRepositoryName(course.createRepositoryName(group).toASCIIString());
		group.setRepository(groupRepository);
		return groups.merge(group);
	}

	protected CourseEdition getTestCourse() {
		return courses.find("TI1705");
	}
	
	protected User student1() {
		return users.find(1);
	}
	
}
