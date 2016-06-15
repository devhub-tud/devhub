package nl.tudelft.ewi.devhub.server.database.controllers;

import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.IssueBackend;
import nl.tudelft.ewi.devhub.server.backend.PersistedBackendTest;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.devhub.server.database.entities.issues.IssueLabel;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class IssuesTest extends PersistedBackendTest {

	@Inject @Getter private Groups groups;
	@Inject @Getter private CourseEditions courses;
	@Inject @Getter private Users users;

	@Inject
	private Issues issues;
	
	@Inject
	private IssueLabels issueLabels;
	
	@Inject
	private EntityManager entityManager;
	
	@Inject
	private IssueBackend issueBackend;

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
	

	@Test
	public void testCreateIssue() {
		GroupRepository groupRepository = group.getRepository();
		List<Issue> issueQueryResult = issues.findOpenIssues(groupRepository);
		
		assertEquals(2, issueQueryResult.size());
		issueEquals(issue1, issueQueryResult.get(0));
	}
	@Test
	public void testFindIssuesOfUser() {
		GroupRepository groupRepository = group.getRepository();
		
		List<Issue> issueQueryResult = issues.findAssignedIssues(groupRepository, user1);
		assertEquals(1, issueQueryResult.size());
		issueEquals(issue1, issueQueryResult.get(0));
	}

	@Test
	public void testFindOpenIssues() {
		GroupRepository groupRepository = group.getRepository();
		
		// Close issue 2
		issue2.setOpen(false);
		
		List<Issue> issueQueryResult = issues.findOpenIssues(groupRepository);
		assertEquals(1, issueQueryResult.size());
		issueEquals(issue1, issueQueryResult.get(0));
	}

	@Test
	public void testFindClosedIssues() {
		GroupRepository groupRepository = group.getRepository();
		
		// Close issue 2
		issue2.setClosed(new Date());
		issue2.setOpen(false);
		
		List<Issue> issueQueryResult = issues.findClosedIssues(groupRepository);
		assertEquals(1, issueQueryResult.size());
		issueEquals(issue2, issueQueryResult.get(0));
	}

	@Test
	public void testFindUnassignedIssues() {
		GroupRepository groupRepository = group.getRepository();
		issue1.setAssignee(null);
		
		List<Issue> issueQueryResult = issues.findUnassignedIssues(groupRepository);
		assertEquals(1, issueQueryResult.size());
		issueEquals(issue1, issueQueryResult.get(0));
	}
	
	@Test
	public void testFindIssuesById(){
		
		List<Issue> issueQueryResult = issues.findIssueById(group.getRepository(), issue1.getIssueId());
		
		assertEquals(1, issueQueryResult.size());
		issueEquals(issue1, issueQueryResult.get(0));
	}

	
	@Test
	public void testAddLabel() {
		
		IssueLabel issueLabel = issueBackend.addIssueLabelToRepository(group.getRepository(), "My Label", 0xcccccc);
		issueBackend.addLabelToIssue(issue1, issueLabel);
		
		clearEntityManager();
		issue1 = issues.findIssueById(group.getRepository(), issue1.getIssueId()).get(0);
		
		assertEquals(1, issue1.getLabels().size());
		
		IssueLabel label = issue1.getLabels().iterator().next();
		assertSame(issueLabel, label);
		
		assertEquals(1, group.getRepository().getLabels().size());
		
		label = group.getRepository().getLabels().iterator().next();
		assertSame(issueLabel, label);
		
	}
	
	@Transactional
	public void clearEntityManager(){
		entityManager.flush();
	}

	private static void issueEquals(Issue expected, Issue actual) {
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
		issue.setTitle("Issue x");
		issue.setRepository(groupRepository);
		issue.setOpen(true);
		issue.setAssignee(user);
		
		issues.persist(issue);
		
		return issue;
	}
	
}
