package nl.tudelft.ewi.devhub.server.database.controllers;

import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.PersistedBackendTest;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;

import com.google.inject.Inject;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class IssuesTest extends PersistedBackendTest {

	@Inject @Getter private Groups groups;
	@Inject @Getter private CourseEditions courses;
	@Inject @Getter private Users users;

	@Inject
	private Issues issues;

	private User user1;
	private User user2;
	private Group group;
	
	private Issue issue1;
	private Issue issue2;
	

	@Before
	public void setUpGroup() {
		user1 = createUser();
		user2 = createUser();
		group = createGroup(createCourseEdition(), user1, user2);

		issue1 = persistIssue(user1);		
		issue2 = persistIssue(user2);
	}
	
	//@After
	public void delete(){
		issues.delete(issue1);
		issues.delete(issue1);
		
		groups.delete(group);
		users.delete(user2);
		users.delete(user1);
		
	}

	@Test
	public void testCreateIssue() {
		GroupRepository groupRepository = group.getRepository();
		List<Issue> issueQueryResult = issues.findOpenIssues(groupRepository);
		
		assertEquals(2, issueQueryResult.size());
		issueRequestEquals(issue1, issueQueryResult.get(0));
	}
	@Test
	public void testFindIssuesOfUser() {
		GroupRepository groupRepository = group.getRepository();
		
		List<Issue> issueQueryResult = issues.findIssues(groupRepository, user1);
		assertEquals(1, issueQueryResult.size());
		issueRequestEquals(issue1, issueQueryResult.get(0));
	}

	@Test
	public void testFindOpenIssues() {
		GroupRepository groupRepository = group.getRepository();
		
		// Close issue 2
		issue2.setClosed(new Date());
		issue2.setOpen(false);
		
		issues.merge(issue2);
		
		List<Issue> issueQueryResult = issues.findOpenIssues(groupRepository);
		assertEquals(1, issueQueryResult.size());
		issueRequestEquals(issue1, issueQueryResult.get(0));
	}

	@Test
	public void testFindClosedIssues() {
		GroupRepository groupRepository = group.getRepository();
		
		// Close issue 2
		issue2.setClosed(new Date());
		issue2.setOpen(false);
		
		issues.merge(issue2);
		
		List<Issue> issueQueryResult = issues.findClosedIssues(groupRepository);
		assertEquals(1, issueQueryResult.size());
		issueRequestEquals(issue2, issueQueryResult.get(0));
	}

	@Test
	public void testFindUnassignedIssues() {
		GroupRepository groupRepository = group.getRepository();
		issue1.setAssignee(null);
		
		issues.merge(issue1);
		
		List<Issue> issueQueryResult = issues.findUnassignedIssues(groupRepository);
		assertEquals(1, issueQueryResult.size());
		issueRequestEquals(issue1, issueQueryResult.get(0));
	}

	private static void issueRequestEquals(Issue expected, Issue actual) {
		try {
			assertEquals(expected.getRepository(), actual.getRepository());
			assertEquals(expected.isOpen(), actual.isOpen());
			assertEquals(expected.getAssignee(), actual.getAssignee());
		}
		catch(AssertionError e) {
			throw new AssertionError(String.format("Expected %s but was %s",
					expected, actual), e);
		}
	}
	
	private Issue persistIssue(User user){
		GroupRepository groupRepository = group.getRepository();
		
		Issue issue = new Issue();
		issue.setRepository(groupRepository);
		issue.setOpen(true);
		issue.setAssignee(user);
		
		issues.merge(issue);
		
		return issue;
	}
	
}
