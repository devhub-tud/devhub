package nl.tudelft.ewi.devhub.server.backend;

import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import lombok.Getter;
import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Issues;
import nl.tudelft.ewi.devhub.server.database.controllers.TestDatabaseModule;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class IssueBackendTest extends PersistedBackendTest {

	private static RepositoriesApi repositoriesApi = mock(RepositoriesApi.class);

	@Inject @Getter private CourseEditions courses;
	@Inject @Getter private Users users;
	@Inject @Getter private Groups groups;
	
	private Issues issues = mock(Issues.class);	
	private IssueBackend issueBackend;	
	private Issue issue;
	
	private User user;
	
	private RepositoryApi repositoryApi;
	
	@Before
	public void setUp() throws Exception {
		issueBackend = new IssueBackend(issues);
		user = createUser();
		repositoryApi = repositoriesApi.getRepository(createGroup(createCourseEdition(), user).getRepository().getRepositoryName());
		
		issue = new Issue();
		
	}

	@Test
	public void testCreateIssue() {
		
		issueBackend.createIssue(repositoryApi, issue);		
		verify(issues).persist(issue);
	}

}
