package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.DiffView;
import nl.tudelft.ewi.devhub.webtests.views.ProjectView;
import nl.tudelft.ewi.devhub.webtests.views.ProjectView.Commit;
import nl.tudelft.ewi.git.client.BranchMock;
import nl.tudelft.ewi.git.client.CommitMock;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClientMock;
import nl.tudelft.ewi.git.client.RepositoryMock;
import nl.tudelft.ewi.git.models.ChangeType;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.UserModel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProjectTest extends WebTest {

	public static final String COMMIT_ID = "6f69819c39b87566a65a2a005a6553831f6d7e7c";
	public static final String COMMIT_MESSAGE = "Initial commit";

	private static GitServerClientMock gitServerClient;
	private static RepositoryMock repository;
	private static UserModel user;
	private static CommitMock commit;

	@BeforeClass
	public static void setUpRepository() throws Exception {
		gitServerClient = getGitServerClient();
		user = gitServerClient.users().ensureExists(NET_ID);
		repository = gitServerClient.repositories().retrieve("courses/ti1705/group-1");
		commit = createInitialCommit(repository);
	}

	private static CommitMock createInitialCommit(RepositoryMock repository) throws GitClientException{
		DetailedCommitModel commit = new DetailedCommitModel();
		commit.setAuthor(user.getName());
		commit.setCommit(COMMIT_ID);
		commit.setParents(new String[]{});
		commit.setTime(System.currentTimeMillis());
		commit.setFullMessage(COMMIT_MESSAGE);

		BranchMock master = repository.retrieveBranch("master");
		return master.addCommit(commit);
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
	public void testListCommits() throws GitClientException {
		ProjectView view = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click();

		List<Commit> commits = view.listCommits();
		List<CommitModel> expected = repository.retrieveBranch("master")
				.retrieveCommits(0, 25)
				.getCommits();
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
		DiffBlameModel diffBlameModel = emptyDiffBlameModel();
		commit.setDiffBlameModel(diffBlameModel);

		DiffView view = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click()
				.listCommits()
				.get(0).click();

		List<DiffView.DiffElement> list = view.listDiffs();
		assertEquals(commit.getAuthor(), view.getAuthorHeader());
		assertEquals(commit.getTitle(), view.getMessageHeader());
		assertTrue("Expected empty list", list.isEmpty());
	}

	private static DiffBlameModel emptyDiffBlameModel() {
		DiffBlameModel diffBlameModel = new DiffBlameModel();
		diffBlameModel.setNewCommit(commit.getCommitModel());
		diffBlameModel.setCommits(Lists.newArrayList(commit.getCommitModel()));
		diffBlameModel.setDiffs(Lists.newArrayList());
		return diffBlameModel;
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

		DiffBlameModel diffBlameModel = filledDiffBlameModel();
		commit.setDiffBlameModel(diffBlameModel);

		DiffView view = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click()
				.listCommits()
				.get(0).click();

		List<DiffView.DiffElement> list = view.listDiffs();
		DiffView.DiffElement result = list.get(0);

		assertEquals(commit.getAuthor(), view.getAuthorHeader());
		assertEquals(commit.getTitle(), view.getMessageHeader());
		result.assertEqualTo(diffBlameModel.getDiffs().get(0));
	}

	private static DiffBlameModel filledDiffBlameModel() {
		String commitId = commit.getCommit();

		DiffBlameModel diffBlameModel = new DiffBlameModel();
		diffBlameModel.setNewCommit(commit.getCommitModel());
		diffBlameModel.setCommits(Lists.newArrayList(commit.getCommitModel()));

		DiffBlameModel.DiffBlameFile file = new DiffBlameModel.DiffBlameFile();
		file.setType(ChangeType.ADD);
		file.setNewPath("README.md");
		file.setOldPath("/dev/null");

		DiffBlameModel.DiffBlameContext context = new DiffBlameModel.DiffBlameContext();

		context.setLines(ImmutableList.of(
			createLine(1, "A readme file with a bit of contents", commitId),
			createLine(2, "Now we've altered the readme a bit to work on the diffs", commitId)
		));

		file.setContexts(ImmutableList.of(context));
		diffBlameModel.setDiffs(ImmutableList.of(file));
		return diffBlameModel;
	}

	private static DiffBlameModel.DiffBlameLine createLine(Integer lineNumber, String content, String commitId) {
		DiffBlameModel.DiffBlameLine res = new DiffBlameModel.DiffBlameLine();
		res.setSourceLineNumber(lineNumber);
		res.setNewLineNumber(lineNumber);
		res.setSourceFilePath("README.md");
		res.setContent(content);
		res.setSourceCommitId(commitId);
		return res;
	}

}
