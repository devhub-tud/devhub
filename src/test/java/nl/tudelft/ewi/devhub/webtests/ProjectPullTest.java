package nl.tudelft.ewi.devhub.webtests;

import com.google.common.io.Files;

import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.comments.PullRequestComment;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.devhub.webtests.utils.Dom;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.CommitsView.Branch;
import nl.tudelft.ewi.devhub.webtests.views.PullRequestOverViewView;
import nl.tudelft.ewi.devhub.webtests.views.PullRequestOverViewView.Comment;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;
import nl.tudelft.ewi.gitolite.repositories.Repository;
import nl.tudelft.ewi.gitolite.repositories.RepositoryNotFoundException;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.By;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ProjectPullTest extends WebTest {
	
	// Commit constants
	private static final String BRANCH_NAME = "my-super-branch";
	private static final String COMMIT_MESSAGE = "Adding my-file.txt";
	private static final String FILE_NAME = "my-file.txt";
	private static final String FILE_CONTENT_MASTER = "Initial content on master";
	private static final String FILE_CONTENT_BRANCH = "Initial content on branch";
	private static final String COMMENT_CONTENT = "Why is this useful?";
	
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
		createCommit(FILE_CONTENT_BRANCH, BRANCH_NAME);
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

	public void createCommit(String fileContent, String branchName) throws URISyntaxException, GitAPIException, IOException {
		Repository repository = repositoriesManager.getRepository(new URI(groupRepository.getRepositoryName() + ".git/"));
		
		Git git = Git.init().setBare(false).setDirectory(temporaryFolder.getRoot()).call();
		
		RemoteAddCommand addCommand = git.remoteAdd();
		addCommand.setName("origin");
		addCommand.setUri(new URIish(repository.getPath().toString()));
		addCommand.call();
		
		git.pull().call();

		boolean createBranch = git.getRepository().findRef(branchName) == null;
		if(!git.getRepository().getBranch().equals(branchName)){
			git.checkout().setCreateBranch(createBranch).setName(branchName).call();			
		}
		
		Files.write(fileContent.getBytes(), new File(temporaryFolder.getRoot(), FILE_NAME));
		git.add().addFilepattern(FILE_NAME).call();
		git.commit().setMessage(COMMIT_MESSAGE).setAuthor(user.getName(), user.getEmail()).setCommitter(user.getName(), user.getEmail()).call();
		
		git.push().call();
	}

	@Test
	public void testCreatePullRequest() throws InterruptedException {
		
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
		PullRequestOverViewView pullRequestOverViewView =
			newBranch.click().openCreatePullRequestView();

		// Wait for the view to load
		waitForCondition(3, x -> pullRequestOverViewView.isOpen());

		// Grab the pull request instance for BRANCH_NAME from the DB.
		PullRequest pullRequest = getPullRequest(BRANCH_NAME);

		// Assertions on pullRequestView against pullRequest
		assertEquals(pullRequest.isOpen(), pullRequestOverViewView.isOpen());

		String author = pullRequest.getDestination().getAuthor();
		assertEquals(author, pullRequestOverViewView.getAuthorHeader());
		
		// Assert message header is latest commit message
		assertEquals(COMMIT_MESSAGE, pullRequestOverViewView.getMessageHeader());

	}

	@Test
	public void testClosePullRequest() {
		// Assert branch is visible
		Branch newBranch = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click()
				.listBranches().get(1);

		assertEquals(BRANCH_NAME, newBranch.getName());

		// Navigate to pull request view
		PullRequestOverViewView pullRequestOverViewView = newBranch.click().openCreatePullRequestView();
		
		assertTrue(pullRequestOverViewView.isOpen());
		pullRequestOverViewView.close();		
		
		waitForCondition(3, x -> pullRequestOverViewView.isClosed());
		
		assertFalse(pullRequestOverViewView.isOpen());
		assertTrue(pullRequestOverViewView.isClosed());
		assertFalse(pullRequestOverViewView.isMerged());
		
		PullRequest pullRequest = getPullRequest(BRANCH_NAME);
		assertTrue(pullRequest.isClosed());
		assertFalse(pullRequest.isMerged());
	}

	@Test
	public void testMergePullRequest() {
		// Assert branch is visible
		Branch newBranch = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click()
				.listBranches().get(1);

		assertEquals(BRANCH_NAME, newBranch.getName());

		// Navigate to pull request view
		PullRequestOverViewView pullRequestOverViewView = newBranch.click().openCreatePullRequestView();
		
		assertTrue(pullRequestOverViewView.isOpen());
		pullRequestOverViewView.merge();
		
		// Wait for the view to load
		Dom.waitForCondition(getDriver(), 5, x -> !pullRequestOverViewView.isOpen());
		
		assertFalse(pullRequestOverViewView.isOpen());
		assertFalse(pullRequestOverViewView.isClosed());
		assertTrue(pullRequestOverViewView.isMerged());
		
		PullRequest pullRequest = getPullRequest(BRANCH_NAME);
		assertTrue(pullRequest.isClosed());
		assertTrue(pullRequest.isMerged());
		
		CommitModel lastMasterCommit = masterApi.get().getCommit();		
		Commit lastBranchCommit = pullRequest.getDestination();
		
		assertTrue(Pattern.matches("^Merge pull request #\\d+ from " + BRANCH_NAME, lastMasterCommit.getMessage()));		
		assertEquals(2, lastMasterCommit.getParents().length);
		assertTrue(ArrayUtils.contains(lastMasterCommit.getParents(), lastBranchCommit.getCommitId()));
	}
	
	@Test
	public void testDeleteBranch() throws InterruptedException{
		// Assert branch is visible
		Branch newBranch = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click()
				.listBranches().get(1);

		assertEquals(BRANCH_NAME, newBranch.getName());

		// Navigate to pull request view
		PullRequestOverViewView pullRequestOverViewView = newBranch.click().openCreatePullRequestView();
		
		assertTrue(pullRequestOverViewView.isOpen());
		pullRequestOverViewView.merge();		
		
		// Wait until branch can be removed
		waitForCondition(5, x -> Dom.isVisible(x, By.id("btn-remove-branch")));
		
		pullRequestOverViewView.removeBranch();

		// Wait until the branch is deleted at the backend
		waitForCondition(3, x -> repositoryApi.getBranches().size() == 1);
		
		// Assert master is the only available branch after deletion
		assertEquals(1, repositoryApi.getBranches().size());
		assertEquals("refs/heads/master", repositoryApi.getBranches().toArray(new BranchModel[1])[0].getName());
				
	}
	
	@Test
	public void testFailedMerge() throws RepositoryNotFoundException, URISyntaxException, IllegalStateException, GitAPIException, IOException, InterruptedException{
		createCommit(FILE_CONTENT_MASTER, "master");
		
		// Assert branch is visible
		Branch newBranch = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click()
				.listBranches().get(1);

		assertEquals(BRANCH_NAME, newBranch.getName());

		// Navigate to pull request view
		PullRequestOverViewView pullRequestOverViewView = newBranch.click().openCreatePullRequestView();
		
		assertTrue(pullRequestOverViewView.isOpen());
		pullRequestOverViewView.merge();
		
		waitForCondition(3, x -> !pullRequestOverViewView.getMergeButton().getText().startsWith("Merging"));
		
		assertEquals("Failed to merge", pullRequestOverViewView.getMergeButton().getText());
		
		PullRequest pullRequest = getPullRequest(BRANCH_NAME);
		
		assertFalse(pullRequest.isClosed());
		assertTrue(pullRequest.isOpen());
		assertFalse(pullRequest.isMerged());
		
	}
	
	@Test
	public void testComment(){
		// Assert branch is visible
		Branch newBranch = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click()
				.listBranches().get(1);

		assertEquals(BRANCH_NAME, newBranch.getName());

		// Navigate to pull request view
		PullRequestOverViewView pullRequestOverViewView = newBranch.click().openCreatePullRequestView();
		
		assertTrue(pullRequestOverViewView.isOpen());
		
		pullRequestOverViewView.addComment(COMMENT_CONTENT);		
		waitForCondition(3, x -> pullRequestOverViewView.listComments().size() == 1);
		
		Comment comment = pullRequestOverViewView.listComments().get(0);		
		assertEquals(COMMENT_CONTENT, comment.getContent());
		
		List<PullRequestComment> comments = getPullRequest(BRANCH_NAME).getComments();		
		assertEquals(1, comments.size());
		assertEquals(COMMENT_CONTENT, comments.get(0).getContent());
				
	}
	
	@After
	public void teardown() throws RepositoryNotFoundException, URISyntaxException{
		// Delete pullrequest after test
		PullRequest pullRequest = getPullRequest(BRANCH_NAME);
		pullRequests.delete(pullRequest);

		// Delete branch after test
		RepositoryApi repositoryApi = repositoriesApi.getRepository(groupRepository.getRepositoryName());
	
		repositoryApi.getBranch(BRANCH_NAME).deleteBranch();
	}


	private PullRequest getPullRequest(String branchName) {
		
		String branchPath = String.format("refs/heads/%s", branchName);
		
		return pullRequests.openPullRequestExists(groupRepository, branchPath)? 
				pullRequests.findOpenPullRequest(groupRepository, branchPath).get() :
				pullRequests.findClosedPullRequests(groupRepository).stream().filter(x -> x.getBranchName().equals(branchPath)).collect(Collectors.toList()).get(0);
	}

}
