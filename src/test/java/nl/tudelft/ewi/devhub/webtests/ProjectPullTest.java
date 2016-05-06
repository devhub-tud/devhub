package nl.tudelft.ewi.devhub.webtests;

import com.google.common.io.Files;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.CommitsView.Branch;
import nl.tudelft.ewi.devhub.webtests.views.PullRequestView;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;
import nl.tudelft.ewi.gitolite.repositories.Repository;
import nl.tudelft.ewi.gitolite.repositories.RepositoryNotFoundException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
public class ProjectPullTest extends WebTest {
	
	// Commit constants
	private static final String BRANCH_NAME = "my-super-branch";
	private static final String COMMIT_MESSAGE = "Adding my-file.txt";
	private static final String FILE_NAME = "my-file.txt";
	private static final String FILE_CONTENT = "Initial content";
	
	@Inject Users users;
	@Inject Groups groups;
	@Inject RepositoriesApi repositoriesApi;
	@Inject RepositoriesManager repositoriesManager;
	@Inject PullRequests pullRequests;

	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private User user;
	private Group group;
	private GroupRepository groupRepository;
	private RepositoryApi repositoryApi;
	private BranchApi masterApi;
	private CommitApi commitApi;
	private DetailedCommitModel commitModel;

	@Before
	public void setup() throws URISyntaxException, GitAPIException, IOException{
		prepareInitialCommit();
		createBranch();
	}
	
	public void prepareInitialCommit() {
		user = users.findByNetId(NET_ID);
		group = groups.listFor(user).get(0);
		groupRepository = group.getRepository();
		repositoryApi = repositoriesApi.getRepository(groupRepository.getRepositoryName());
		masterApi = repositoryApi.getBranch("master");
		commitApi = masterApi.getCommit();
		commitModel = commitApi.get();
	}

	public void createBranch() throws URISyntaxException, GitAPIException, IOException {
		Repository repository = repositoriesManager.getRepository(new URI(groupRepository.getRepositoryName() + ".git/"));
		
		Git git = Git.init().setBare(false).setDirectory(temporaryFolder.getRoot()).call();
		
		RemoteAddCommand addCommand = git.remoteAdd();
		addCommand.setName("origin");
		addCommand.setUri(new URIish(repository.getPath().toString()));
		addCommand.call();
		
		git.pull().call();

		git.checkout().setCreateBranch(true).setName(BRANCH_NAME).call();
		Files.write(FILE_CONTENT.getBytes(), new File(temporaryFolder.getRoot(), FILE_NAME));
		git.add().addFilepattern(FILE_NAME).call();
		git.commit().setMessage(COMMIT_MESSAGE).setAuthor(user.getName(), user.getEmail()).call();
		
		git.push().call();
	}

	@Test
	public void testCreatePullRequest() {
		
		// Assert branch is visible
		Branch newBranch = openLoginScreen()
			.login(NET_ID, PASSWORD)
			.toCoursesView()
			.listMyProjects()
			.get(0).click()
			.listBranches()
			.get(1);


		assertEquals(BRANCH_NAME, newBranch.getName());

		// Navigate to pull request view
		PullRequestView pullRequestView =
			newBranch.click().openCreatePullRequestView();

		// Grab the pull request instance for BRANCH_NAME from the DB.
		PullRequest pullRequest = pullRequests.findOpenPullRequest(groupRepository, "refs/heads/" + BRANCH_NAME).get();

		// Wait for the view to load
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			log.warn("", e);
		}
		
		// Assertions on pullRequestView against pullRequest
		assertEquals(pullRequest.isOpen(), pullRequestView.isOpen());

		// Assert opening users name and email are in the header (currently failing)
		String author = pullRequest.getDestination().getAuthor();
		assertEquals(author, pullRequestView.getAuthorHeader());
		
		// Assert message header is latest commit message
		assertEquals(COMMIT_MESSAGE, pullRequestView.getMessageHeader());
		
	}
	
	@After
	public void teardown() throws RepositoryNotFoundException, URISyntaxException{
		// Delete pullrequest after test
		PullRequest pullRequest = pullRequests.findOpenPullRequest(groupRepository, "refs/heads/" + BRANCH_NAME).get();
		pullRequests.delete(pullRequest);

		// Delete branch after test
		RepositoryApi repositoryApi = repositoriesApi.getRepository(groupRepository.getRepositoryName());
	
		repositoryApi.getBranch(BRANCH_NAME).deleteBranch();
	}

}
