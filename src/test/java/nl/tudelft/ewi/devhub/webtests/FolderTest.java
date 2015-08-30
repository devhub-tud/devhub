package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.FolderView;
import nl.tudelft.ewi.devhub.webtests.views.TextFileView;
import nl.tudelft.ewi.git.client.BranchMock;
import nl.tudelft.ewi.git.client.CommitMock;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClientMock;
import nl.tudelft.ewi.git.client.RepositoryMock;
import nl.tudelft.ewi.git.models.BlameModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.UserModel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class FolderTest extends WebTest {

	public static final String COMMIT_ID = "6f69819c39b87566a65a2a005a6553831f6d7e7c";
	public static final String COMMIT_MESSAGE = "Initial commit";
	public static final String REPO_NAME = "group-1/";
	public static final String FOLDER_NAME = "SubFolder/";
	public static final String TEXT_FILE_NAME = "File.txt";
    public static final String TEXT_FILE_CONTENTS = "A readme file\nWith some\nContents";

	private static GitServerClientMock gitServerClient;
	private static RepositoryMock repository;
	private static UserModel user;
	private static CommitMock commit;
    private static Map<String, EntryType> entries;

	@BeforeClass
	public static void setUpRepository() throws Exception {
		gitServerClient = getGitServerClient();
		user = gitServerClient.users().ensureExists(NET_ID);
		repository = gitServerClient.repositories().retrieve("courses/ti1705/TI1705/group-1");
		commit = createInitialCommit(repository);

        entries = ImmutableMap.<String, EntryType> of(
                FOLDER_NAME, EntryType.FOLDER,
                TEXT_FILE_NAME, EntryType.TEXT);
        repository.setListDirectoryEntries(entries);

        repository.setFileContents(TEXT_FILE_CONTENTS);
        commit.setBlameModel(generateBlameModelFor(TEXT_FILE_CONTENTS));
	}

    private static BlameModel generateBlameModelFor(String contents) {
        BlameModel blameModel = new BlameModel();
        BlameModel.BlameBlock block = new BlameModel.BlameBlock();
        block.setFromFilePath(TEXT_FILE_NAME);
        block.setDestinationFrom(1);
        block.setSourceFrom(1);
        block.setLength(3);
        block.setFromCommitId(COMMIT_ID);
        blameModel.setBlames(ImmutableList.of(block));
        return blameModel;
    }

    private static CommitMock createInitialCommit(RepositoryMock repository) throws GitClientException {
        DetailedCommitModel commit = new DetailedCommitModel();
        commit.setAuthor(user.getName());
        commit.setCommit(COMMIT_ID);
        commit.setParents(new String[]{});
        commit.setTime(System.currentTimeMillis());
        commit.setFullMessage(COMMIT_MESSAGE);

        BranchMock master = repository.retrieveBranch("master");
        CommitMock commitMock = master.addCommit(commit);
        DiffBlameModel diffBlameModel = emptyDiffBlameModel(commit);
        commitMock.setDiffBlameModel(diffBlameModel);
        return commitMock;
    }

    private static DiffBlameModel emptyDiffBlameModel(CommitModel commit) {
        DiffBlameModel diffBlameModel = new DiffBlameModel();
        diffBlameModel.setNewCommit(commit);
        diffBlameModel.setCommits(Lists.newArrayList(commit));
        diffBlameModel.setDiffs(Lists.newArrayList());
        return diffBlameModel;
    }

	private FolderView getFolderView() {
		FolderView view = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click()
				.listCommits()
				.get(0).click()
				.viewFiles();

		assertEquals(commit.getAuthor(), view.getAuthorHeader());
		assertEquals(commit.getTitle(), view.getMessageHeader());

		return view;
	}

	/**
	 * <h1>Opening a folder in the repository .</h1>
	 *
	 * Given that:
	 * <ol>
	 *   <li>I am successfully logged in.</li>
	 *   <li>I have a project.</li>
	 *   <li>There is a commit in the project.</li>
	 *   <li>I list the folders in the repository at the commit.</li>
	 * </ol>
	 * When:
	 * <ol>
	 *   <li>I click on List directory.</li>
	 * </ol>
	 * Then:
	 * <ol>
	 *   <li>I am redirected to the folder page.</li>
	 *   <li>The elements in the folder page match the elements in the repository.</li>
	 * </ol>
	 */
	@Test
	public void testFileExplorer() throws InterruptedException {
		FolderView view = getFolderView();
		assertThat(view.getPath(), containsString(REPO_NAME));
		Map<String, EntryType> actual = view.getDirectoryEntries();
		assertEquals(entries, actual);
	}

	/**
	 * <h1>Opening a text file in the repository at a certain commit.</h1>
	 *
	 * Given that:
	 * <ol>
	 *   <li>I am successfully logged in.</li>
	 *   <li>I have a project.</li>
	 *   <li>There is a commit in the project.</li>
	 * </ol>
	 * When:
	 * <ol>
	 *   <li>I click a file in the folder view.</li>
	 * </ol>
	 * Then:
	 * <ol>
	 *   <li>I am redirected to the file page.</li>
	 *   <li>The contents in the file match the contents in the repository.</li>
	 * </ol>
	 */
	@Test
	public void testOpenFile() {
        TextFileView view = getFolderView()
				.getDirectoryElements()
				.get(1).click();

		assertEquals(TEXT_FILE_NAME, view.getFilename());
		assertThat(view.getPath(), containsString(REPO_NAME));
		assertEquals(commit.getAuthor(), view.getAuthorHeader());
		assertEquals(commit.getTitle(), view.getMessageHeader());
		assertEquals(TEXT_FILE_CONTENTS, view.getContent());
	}

}
