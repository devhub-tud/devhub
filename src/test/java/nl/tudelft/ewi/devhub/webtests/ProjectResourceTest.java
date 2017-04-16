package nl.tudelft.ewi.devhub.webtests;

import com.google.common.io.Files;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.CommitsView;
import nl.tudelft.ewi.devhub.webtests.views.DeleteAheadBranchView;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;
import nl.tudelft.ewi.gitolite.repositories.Repository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.transport.URIish;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(MockitoJUnitRunner.class)
public class ProjectResourceTest extends WebTest {

    // Commit constants
    private static final String BRANCH_NAME = "someBranch";
    private static final String COMMIT_MESSAGE = "Adding my-file.txt";
    private static final String FILE_NAME = "my-file.txt";
    private static final String FILE_CONTENT_BRANCH = "Initial content on branch";

    @Inject Users users;
    @Inject Groups groups;
    @Inject RepositoriesApi repositoriesApi;
    @Inject RepositoriesManager repositoriesManager;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private User user;
    private Group group;
    private GroupRepository groupRepository;
    private RepositoryApi repositoryApi;
    private BranchApi masterApi;
    private CommitApi commitApi;
    private DetailedCommitModel commitModel;
    private Git git;

    @Before
    public void setup() throws Throwable {
        prepareInitialCommit();
        createBranch(BRANCH_NAME);
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

    public void createBranch(String branchName) throws Throwable {
        Repository repository = repositoriesManager.getRepository(new URI(groupRepository.getRepositoryName() + ".git/"));

        git = Git.init().setBare(false).setDirectory(temporaryFolder.getRoot()).call();

        RemoteAddCommand remoteAddCommand = git.remoteAdd();
        remoteAddCommand.setName("origin");
        remoteAddCommand.setUri(new URIish(repository.getPath().toString()));
        remoteAddCommand.call();

        git.pull().call();

        git.checkout()
                .setCreateBranch(true)
                .setName(branchName)
                .setForce(true)
                .call();

        Files.write(FILE_CONTENT_BRANCH.getBytes(), new File(temporaryFolder.getRoot(), FILE_NAME));
        git.add().addFilepattern(FILE_NAME).call();

        git.commit()
                .setMessage(COMMIT_MESSAGE)
                .setAuthor(user.getName(), user.getEmail())
                .setCommitter(user.getName(), user.getEmail())
                .call();

        git.push()
                .setForce(true)
                .call();
    }

    @Test
    public void testDeleteAheadBranch() throws Throwable {

        // Assert branch is visible
        CommitsView.Branch newBranch = openLoginScreen()
                .login(NET_ID, PASSWORD)
                .toCoursesView()
                .listMyProjects()
                .get(0).click()
                .listBranches()
                .get(1);

        DeleteAheadBranchView deleteAheadBranchView = newBranch.click()
                .deleteBranch()
                .typeBranchName(BRANCH_NAME)
                .clickRemoveAheadBranch();

        assertEquals("master", deleteAheadBranchView.listBranches().get(0).getName());
        assertEquals(1, deleteAheadBranchView.listBranches().size());
        assertNotEquals(COMMIT_MESSAGE, deleteAheadBranchView.listCommits().get(0).getMessage());
    }

    @Test
    public void testDeleteAheadBranchWrongName() throws Throwable {

        // Assert branch is visible
        CommitsView.Branch newBranch = openLoginScreen()
                .login(NET_ID, PASSWORD)
                .toCoursesView()
                .listMyProjects()
                .get(0).click()
                .listBranches()
                .get(1);

        DeleteAheadBranchView deleteAheadBranchView = newBranch.click()
                .deleteBranch()
                .typeBranchName("something wrong")
                .clickRemoveAheadBranch();

        assertEquals("master", deleteAheadBranchView.listBranches().get(0).getName());
        assertEquals(BRANCH_NAME, deleteAheadBranchView.listBranches().get(1).getName());
        assertEquals(2, deleteAheadBranchView.listBranches().size());

        deleteAheadBranchView.typeBranchName(BRANCH_NAME)
                .clickRemoveAheadBranch();

        assertEquals("master", deleteAheadBranchView.listBranches().get(0).getName());
        assertEquals(1, deleteAheadBranchView.listBranches().size());
        assertNotEquals(COMMIT_MESSAGE, deleteAheadBranchView.listCommits().get(0).getMessage());
    }
}
