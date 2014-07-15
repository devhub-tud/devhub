package nl.tudelft.ewi.devhub.webtests;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.containsString;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.FolderView;
import nl.tudelft.ewi.devhub.webtests.views.TextFileView;
import nl.tudelft.ewi.git.client.GitServerClientMock;
import nl.tudelft.ewi.git.client.RepositoriesMock;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.MockedRepositoryModel;
import nl.tudelft.ewi.git.models.UserModel;

public class FolderTest extends WebTest {
	
	public static final String COMMIT_ID = "6f69819c39b87566a65a2a005a6553831f6d7e7c";
	public static final String COMMIT_MESSAGE = "Initial commit";
	public static final String REPO_NAME = "group-1/";
	public static final String FOLDER_NAME = "SubFolder/";
	public static final String TEXT_FILE_NAME = "File.txt";
	
	private static GitServerClientMock gitServerClient;
	private static MockedRepositoryModel repository;
	private static UserModel user;
	private static CommitModel commit;
	
	
	@BeforeClass
	public static void setUpRepository() throws Exception {
		gitServerClient = getGitServerClient();
		user = gitServerClient.users().ensureExists(NET_ID);
		repository = gitServerClient.repositories().retrieve("courses/ti1705/group-1");
		commit = createInitialCommit(repository);
		gitServerClient.repositories().setDirectoryEntries(
				ImmutableMap.<String, EntryType> of(FOLDER_NAME,
						EntryType.FOLDER, TEXT_FILE_NAME, EntryType.TEXT));
	}
	
	private static CommitModel createInitialCommit(MockedRepositoryModel repository) {
		CommitModel commit = new CommitModel();
		commit.setAuthor(user.getName());
		commit.setCommit(COMMIT_ID);
		commit.setParents(new String[] {});
		commit.setTime(System.currentTimeMillis());
		commit.setMessage(COMMIT_MESSAGE);
		repository.addCommit(commit);

		BranchModel branch = new BranchModel();
		branch.setCommit(COMMIT_ID);
		branch.setName("refs/remotes/origin/master");
		repository.addBranch(branch);
		
		return commit;
	}
	
	private FolderView getFolderView() {
		FolderView view = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toProjectsView()
				.listMyProjects()
				.get(0).click()
				.listCommits()
				.get(0).click()
				.viewFiles();
		
		assertEquals(commit.getAuthor(), view.getAuthorHeader());
		assertEquals(commit.getMessage(), view.getMessageHeader());
		
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
		Map<String, EntryType> expected = gitServerClient.repositories().listDirectoryEntries(repository, COMMIT_ID, "");
		Map<String, EntryType> actual = view.getDirectoryEntries();
		assertEquals(expected, actual);
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
		assertEquals(commit.getMessage(), view.getMessageHeader());
		assertEquals(RepositoriesMock.DEFAULT_FILE_CONTENTS, view.getContent());
	}
	
}
