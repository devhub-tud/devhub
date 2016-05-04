package nl.tudelft.ewi.devhub.webtests;

import com.google.common.io.Files;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.PullRequestView;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;
import nl.tudelft.ewi.gitolite.repositories.Repository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
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
import static org.junit.Assert.assertTrue;

public class ProjectPullTest extends WebTest {

	private final String BRANCH_NAME = "my-super-branch";
	
	@Inject Users users;
	@Inject Groups groups;
	@Inject RepositoriesApi repositoriesApi;
	@Inject RepositoriesManager repositoriesManager;

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
		Files.write("Initial content".getBytes(), new File(temporaryFolder.getRoot(), "my-file.txt"));
		git.add().addFilepattern("my-file.txt").call();
		git.commit().setMessage("Adding my-file.txt").setAuthor(user.getName(), user.getEmail()).call();
		
		git.push().call();
	}

	@Test
	public void testCreatePullRequest() {
		
		// Create pull request for commit
		PullRequestView view = openLoginScreen()
			.login(NET_ID, PASSWORD)
			.toCoursesView()
			.listMyProjects()
			.get(0).click()
			.listBranches()
			.get(1).click()
			.openCreatePullRequestView();
		
	}

}
