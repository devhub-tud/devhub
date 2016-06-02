package nl.tudelft.ewi.devhub.webtests;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.sun.jna.platform.win32.Netapi32;

import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Issues;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.devhub.webtests.utils.Dom;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.IssueCreateView;
import nl.tudelft.ewi.devhub.webtests.views.IssueEditView;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class IssuesTest extends WebTest {

	private static final String NET_ID = "student1";
	private static final String PASSWORD = "student1";
	
	// Time is displayed with minute precision, therefore the 65 seconds difference
	private static final int timeDifferenceTreshold = 65 * 1000;
	
	private final String issueTitle = "HTCPCP Compatbility";
	private final String description = "We need to implement the HTCPCP protocol\r\nIn order to have coffee at all times";
	
	private final String issueTitleEdited = "HTCPCP Compatbility - v2";
	private final String descriptionEdited = "We need to implement the HTCPCP protocol\r\nIn order to have coffee at all times, and be happy :D";

	private final String assignee = "student2";
	
	@Inject Users users;
	@Inject Groups groups;
	@Inject RepositoriesApi repositoriesApi;
	@Inject RepositoriesManager repositoriesManager;
	@Inject Issues issues;
	
	private Group group;
	private User student1;
	private User student2;
	
	@Before
	public void setup(){
		student1 = users.findByNetId(NET_ID);
		student2 = users.findByNetId(assignee);
		group = groups.listFor(student1).get(0);
	}
	
	@Test
	public void testCreateIssue() throws ParseException{
		IssueCreateView createview = openLoginScreen().login(NET_ID, PASSWORD)
			.toCoursesView().listMyProjects().get(0).click()
			.toIssuesView().addIssue();
		createview.setTitle(issueTitle);
		createview.setDescription(description);
		createview.setAssignee(assignee);
		
		IssueEditView editView = createview.create();
		
		Issue issue = issues.findOpenIssues(group.getRepository()).get(0);
		
		assertEquals(issueTitle, issue.getTitle());
		assertEquals(description, issue.getDescription());
		assertEquals(student2, issue.getAssignee());
		assertTrue(issue.isOpen());
		
		assertDatesEqual(new Date(), issue.getTimestamp(), timeDifferenceTreshold);
		
		assertEquals(issueTitle, editView.getTitle());
		assertEquals(description.replace("\r", ""), editView.getDescription());
		assertEquals(assignee, editView.getAssignee());
		assertDatesEqual(new Date(), editView.getOpened(), timeDifferenceTreshold);
		
	}
	
	@Test
	public void testEditIssue() throws ParseException{
		IssueCreateView createview = openLoginScreen().login(NET_ID, PASSWORD)
			.toCoursesView().listMyProjects().get(0).click()
			.toIssuesView().addIssue();
		createview.setTitle(issueTitle);
		createview.setDescription(description);
		createview.setAssignee(assignee);
		
		IssueEditView editView = createview.create();
		
		Issue issue = issues.findOpenIssues(group.getRepository()).get(0);		
		Date creationDate = issue.getTimestamp(); // Save creation date to verify it has not changed
		
		assertEquals(issueTitle, issue.getTitle());
		assertEquals(description, issue.getDescription());
		assertEquals(student2, issue.getAssignee());
		assertTrue(issue.isOpen());		
		assertDatesEqual(new Date(), creationDate, timeDifferenceTreshold);
		
		editView.setTitle(issueTitleEdited);
		editView.setDescription(descriptionEdited);
		editView.setAssignee(NET_ID);
		
		editView.setStatus("closed");
		IssueEditView newDetailsView = editView.save();
		this.waitForCondition(10, x -> Dom.isVisible(getDriver(), By.id("timestampClosed")));
		
		Issue issueEdited = issues.findClosedIssues(group.getRepository()).get(0);
		issues.refresh(issueEdited);
		
		assertEquals(issueTitleEdited, issueEdited.getTitle());
		assertEquals(descriptionEdited, issueEdited.getDescription());
		assertEquals(student1, issueEdited.getAssignee());
		assertFalse(issueEdited.isOpen());
		assertTrue(issueEdited.isClosed());		
		assertEquals(creationDate, issueEdited.getTimestamp());
		
		assertDatesEqual(new Date(), editView.getClosed(), timeDifferenceTreshold);

		assertEquals(issueTitleEdited, newDetailsView.getTitle());
		assertEquals(descriptionEdited.replace("\r", ""), newDetailsView.getDescription());
		assertEquals(NET_ID, newDetailsView.getAssignee());
		assertEquals("closed", newDetailsView.getStatus());
		assertDatesEqual(new Date(), newDetailsView.getClosed(), timeDifferenceTreshold);
		
	}
	
	
	
	
	public static void assertDatesEqual(Date expected, Date actual, int millisTreshhold){
		assertTrue(Math.abs(expected.getTime() - actual.getTime()) < millisTreshhold);
	}

}
