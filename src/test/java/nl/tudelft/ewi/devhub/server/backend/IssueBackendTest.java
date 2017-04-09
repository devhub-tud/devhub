package nl.tudelft.ewi.devhub.server.backend;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.IssueLabels;
import nl.tudelft.ewi.devhub.server.database.controllers.Issues;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

@RunWith(MockitoJUnitRunner.class)
public class IssueBackendTest extends BackendTest {

	@Mock private CourseEditions courses;
	@Mock private Users users;
	@Mock private Groups groups;
	
	@Mock private Issues issues;	
	@Mock private IssueLabels issuelabels;
	
	@InjectMocks private IssueBackend issueBackend;	
	
	private Issue issue;
	
	@Mock
	private RepositoryApi repositoryApi;
	
	@Before
	public void setUp() throws Exception {
		issue = new Issue();
	}

	@Test
	public void testCreateIssue() {		
		issueBackend.createIssue(repositoryApi, issue);		
		verify(issues).persist(issue);
	}

}
