package nl.tudelft.ewi.devhub.webtests;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.UserModel;

public class FolderTest extends WebTest {
	
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
	
	@Test
	public void testFileExplorer() throws InterruptedException {
		/* FolderView view = */ openLoginScreen().login(NET_ID, PASSWORD)
				.toProjectsView().listMyProjects().get(0).click()
				.listCommits().get(0).click().viewFiles();
		Thread.sleep(10000000);
	}
	
}
