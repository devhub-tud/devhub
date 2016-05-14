package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.util.FlattenFolderTree;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.FolderView;
import nl.tudelft.ewi.devhub.webtests.views.TextFileInCommitView;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.EntryType;

import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class FolderTest extends WebTest {

	public static final String REPO_NAME = "group-1/";

	@Inject Users users;
	@Inject Groups groups;
	@Inject RepositoriesApi repositoriesApi;

	User user;
	Group group;
	GroupRepository groupRepository;
	RepositoryApi repositoryApi;
	BranchApi masterApi;
	CommitApi commitApi;
	FlattenFolderTree flattenFolderTree;
	DetailedCommitModel commitModel;

	@Before
	public void prepareInitialCommit() {
		user = users.findByNetId(NET_ID);
		group = groups.listFor(user).get(0);
		groupRepository = group.getRepository();
		repositoryApi = repositoriesApi.getRepository(groupRepository.getRepositoryName());
		masterApi = repositoryApi.getBranch("master");
		commitApi = masterApi.getCommit();
		flattenFolderTree = new FlattenFolderTree(commitApi);
		commitModel = commitApi.get();
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

		assertEquals(commitModel.getAuthor(), view.getAuthorHeader());
		assertEquals(commitModel.getMessage(), view.getMessageHeader());

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
		Map<String, EntryType> expected = flattenFolderTree.resolveEntries();
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
	public void testOpenFile() throws IOException {

		FolderView folderView = getFolderView();

		String fileName = null;
		String contents = null;
		int i = 0;

		for (Entry<String, EntryType> entry : folderView.getDirectoryEntries().entrySet()) {
			if (entry.getValue().equals(EntryType.TEXT)) {
				fileName = entry.getKey();
				contents = commitApi.showTextFile(fileName);
				break;
			}
			i++;
		}

		assertNotNull(fileName);
		assertNotNull(contents);

		TextFileInCommitView view = folderView
				.getDirectoryElements()
				.get(i).click();

		assertThat(view.getPath(), containsString(REPO_NAME));
		assertEquals(commitModel.getAuthor(), view.getAuthorHeader());
		assertEquals(commitModel.getMessage(), view.getMessageHeader());

		assertEquals(fileName, view.getFilename());
		assertEquals(contents.trim(), view.getContent().trim());
	}

}
