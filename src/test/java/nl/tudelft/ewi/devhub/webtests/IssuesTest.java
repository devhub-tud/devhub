package nl.tudelft.ewi.devhub.webtests;

import javax.inject.Inject;

import org.junit.Test;

import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.IssueCreateView;
import nl.tudelft.ewi.devhub.webtests.views.IssueEditView;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;

import static org.junit.Assert.*;

public class IssuesTest extends WebTest {

	private static final String NET_ID = "student1";
	private static final String PASSWORD = "student1";
	
	private final String issueTitle = "HTCPCP Compatbility";
	private final String description = "We need to implement the HTCPCP protocol\nIn order to have coffe at all times";

	private final String assignee = "student2";
	
	@Inject Users users;
	@Inject Groups groups;
	@Inject RepositoriesApi repositoriesApi;
	@Inject RepositoriesManager repositoriesManager;
	@Inject PullRequests pullRequests;
	
	@Test
	public void testCreateIssue(){
		IssueCreateView createview = openLoginScreen().login(NET_ID, PASSWORD)
			.toCoursesView().listMyProjects().get(0).click()
			.toIssuesView().addIssue();
		createview.setTitle(issueTitle);
		createview.setDescription(description);
		createview.setAssignee(assignee);
		
		IssueEditView editView = createview.create();
		
		assertEquals(issueTitle, editView.getTitle());
		assertEquals(description, editView.getDescription());
		assertEquals(assignee, editView.getAssignee());
		
		
	}

}
