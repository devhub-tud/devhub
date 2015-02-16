package nl.tudelft.ewi.devhub.server.database.controllers;

import java.util.Random;

import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import static org.junit.Assert.*;

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
	
	@Test
	public void testCreatePullRequest() {
		Group group = createGroup();
		PullRequest pr = new PullRequest();
		pr.setGroup(group);
		pr.setBranchName("super-branch");
		pr.setOpen(true);
		pullRequests.persist(pr);
		pullRequestEquals(pr, pullRequests.findOpenPullRequest(group, "super-branch"));
	}
	
	private static void pullRequestEquals(PullRequest expected, PullRequest actual) {
		try {
			assertEquals(expected.getBranchName(), actual.getBranchName());
			assertEquals(expected.getGroup(), actual.getGroup());
			assertEquals(expected.isOpen(), actual.isOpen());
		}
		catch(AssertionError e) {
			throw new AssertionError(String.format("Expected %s but was %s",
					expected, actual), e);
		}
	}
	
	protected Group createGroup() {
		Group group = new Group();
		Course course = getTestCourse();
		group.setGroupNumber(random.nextLong());
		group.setCourse(course);
		group.setRepositoryName(String.format("courses/%s/group-%s", group.getGroupNumber(), course.getName()));
		return groups.persist(group);
	}
	
	protected Course getTestCourse() {
		return courses.find("TI1705");
	}
	
	protected User student1() {
		return users.find(1);
	}
	
}
