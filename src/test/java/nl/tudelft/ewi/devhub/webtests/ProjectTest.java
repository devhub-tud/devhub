package nl.tudelft.ewi.devhub.webtests;

import static org.junit.Assert.*;

import java.util.List;

import nl.tudelft.ewi.devhub.server.util.DiffLine;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.DiffView;
import nl.tudelft.ewi.devhub.webtests.views.DiffView.DiffElement;
import nl.tudelft.ewi.devhub.webtests.views.ProjectView;
import nl.tudelft.ewi.devhub.webtests.views.ProjectView.Commit;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.RepositoriesMock;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.DiffModel.Type;
import nl.tudelft.ewi.git.models.UserModel;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ProjectTest extends WebTest {
	
	public static final String COMMIT_ID = "6f69819c39b87566a65a2a005a6553831f6d7e7c";
	public static final String COMMIT_MESSAGE = "Initial commit";
	
	private static GitServerClient gitServerClient;
	private static DetailedRepositoryModel repository;
	private static UserModel user;
	
	@BeforeClass
	public static void setUpRepository() throws Exception {
		gitServerClient = getGitServerClient();
		user = gitServerClient.users().ensureExists(NET_ID);
		repository = gitServerClient.repositories().retrieve("courses/ti1705/group-1");
		createInitialCommit();
	}
	
	private static CommitModel createInitialCommit() {
		CommitModel commit = new CommitModel();
		commit.setAuthor(user.getName());
		commit.setCommit(COMMIT_ID);
		commit.setParents(new String[] {});
		commit.setTime(System.currentTimeMillis());
		commit.setMessage(COMMIT_MESSAGE);
		repository.setRecentCommits(Lists.newArrayList(commit));
		return commit;
	}
	
	/**
	 * <h1>Opening a project overview .</h1>
	 * 
	 * Given that:
	 * <ol>
	 *   <li>I am successfully logged in.</li>
	 *   <li>I have a project.</li>
	 *   <li>There are no commits in the project</li>
	 * </ol>
	 * When:
	 * <ol>
	 *   <li>I click on a project in the project list.</li>
	 * </ol>
	 * Then:
	 * <ol>
	 *   <li>I am redirected to the project page.</li>
	 * </ol>
	 */
	@Test
	public void testICanOpenProject() {
		repository.setRecentCommits(Lists.<CommitModel> newArrayList());
		ProjectView view = openLoginScreen().login(NET_ID, PASSWORD)
				.toProjectsView().listMyProjects().get(0).click();
		List<Commit> commits = view.listCommits();
		assertTrue("Expected an empty list of commits", commits.isEmpty());
	}
	
	/**
	 * <h1>Opening a project overview .</h1>
	 * 
	 * Given that:
	 * <ol>
	 *   <li>I am successfully logged in.</li>
	 *   <li>I have a project.</li>
	 *   <li>There is a commit in the project.</li>
	 * </ol>
	 * When:
	 * <ol>
	 *   <li>I click on a project in the project list.</li>
	 * </ol>
	 * Then:
	 * <ol>
	 *   <li>I am redirected to the project page.</li>
	 * </ol>
	 */
	@Test
	public void testListCommits() {
		ProjectView view = openLoginScreen().login(NET_ID, PASSWORD)
				.toProjectsView().listMyProjects().get(0).click();
		List<Commit> commits = view.listCommits();
		List<CommitModel> expected = gitServerClient.repositories().listCommits(repository);
		assertEquals(expected.size(), commits.size());
		
		for(int i = 0, s = expected.size(); i < s; i++) {
			Commit commit = commits.get(i);
			CommitModel model = expected.get(i);
			assertEquals(commit.getAuthor(), model.getAuthor());
			assertEquals(commit.getMessage(), model.getMessage());
		}		
	}
	
	/**
	 * <h1>Opening a project overview .</h1>
	 * 
	 * Given that:
	 * <ol>
	 *   <li>I am successfully logged in.</li>
	 *   <li>I have a project.</li>
	 *   <li>There is a commit in the project.</li>
	 * </ol>
	 * When:
	 * <ol>
	 *   <li>I click on a project in the project list.</li>
	 * </ol>
	 * Then:
	 * <ol>
	 *   <li>I am redirected to the diff page.</li>
	 * </ol>
	 */
	@Test
	public void testViewCommitDiffEmpty() {
		DiffView view = openLoginScreen().login(NET_ID, PASSWORD)
				.toProjectsView().listMyProjects().get(0).click()
				.listCommits().get(0).click();
		List<DiffElement> list = view.listDiffs();
		assertTrue("Expected empty list", list.isEmpty());
	}
	

	/**
	 * <h1>Opening a project overview .</h1>
	 * 
	 * Given that:
	 * <ol>
	 *   <li>I am successfully logged in.</li>
	 *   <li>I have a project.</li>
	 *   <li>There is a commit in the project.</li>
	 * </ol>
	 * When:
	 * <ol>
	 *   <li>I click on a project in the project list.</li>
	 * </ol>
	 * Then:
	 * <ol>
	 *   <li>I am redirected to the diff page.</li>
	 * </ol>
	 */
	@Test
	public void testViewCommitDiff() {
		DiffModel model = new DiffModel();
		model.setOldPath("the/old/path");
		model.setNewPath("the/new/path");
		model.setRaw(new String[] { "diff --git a/readme.md b/readme.md",
				"index 983cc05..da041cc 100644", "--- a/readme.md",
				"+++ b/readme.md", "@@ -1 +1,2 @@",
				" A readme file with a bit of contents",
				"+Now we've altered the readme a bit to work on the diffs" });
		model.setType(Type.ADD);
		
		((RepositoriesMock) gitServerClient.repositories()).setListDiffs(Lists
				.<DiffModel> newArrayList(model));
		DiffView view = openLoginScreen().login(NET_ID, PASSWORD)
				.toProjectsView().listMyProjects().get(0).click()
				.listCommits().get(0).click();
		
		List<DiffElement> list = view.listDiffs();
		DiffElement result = list.get(0);
		
		assertEquals(model.getType(), result.getType());
		switch (model.getType()) {
		case ADD:
		case MODIFY:
			assertEquals(model.getNewPath(), result.getNewPath());
			break;
		case DELETE:
			assertEquals(model.getOldPath(), result.getOldPath());
			break;
		default:
			assertEquals(model.getNewPath(), result.getNewPath());
			assertEquals(model.getOldPath(), result.getOldPath());
			break;
		}
		
		assertEquals(DiffLine.getLinesFor(model), result.getDiffLines());
	}
	
}
