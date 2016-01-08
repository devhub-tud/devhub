package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.DiffView;
import nl.tudelft.ewi.devhub.webtests.views.ProjectView;
import nl.tudelft.ewi.devhub.webtests.views.ProjectView.Commit;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;

import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ProjectTest extends WebTest {

	@Inject Users users;
	@Inject Groups groups;
	@Inject RepositoriesApi repositoriesApi;

	private User user;
	private Group group;
	private GroupRepository groupRepository;
	private RepositoryApi repositoryApi;
	private BranchApi masterApi;
	private CommitApi commitApi;
	private DetailedCommitModel commitModel;

	@Before
	public void prepareInitialCommit() {
		user = users.findByNetId(NET_ID);
		group = groups.listFor(user).get(0);
		groupRepository = group.getRepository();
		repositoryApi = repositoriesApi.getRepository(groupRepository.getRepositoryName());
		masterApi = repositoryApi.getBranch("master");
		commitApi = masterApi.getCommit();
		commitModel = commitApi.get();
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
		ProjectView view = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click();

		List<Commit> commits = view.listCommits();
		List<CommitModel> expected = masterApi.retrieveCommitsInBranch().getCommits();
		assertEquals(expected.size(), commits.size());

		for(int i = 0, s = expected.size(); i < s; i++) {
			Commit commit = commits.get(i);
			CommitModel expectedModel = expected.get(i);
			String expectedMessage = expectedModel.getMessage();

			assertEquals(expectedModel.getAuthor(), commit.getAuthor());
			assertTrue(expectedMessage.startsWith(commit.getMessage()));
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
	@Ignore("No easy way to produce empty diff right now")
	public void testViewCommitDiffEmpty() {
		DiffView view = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click()
				.listCommits()
				.get(0).click();

		List<DiffView.DiffElement> list = view.listDiffs();
		assertEquals(commitModel.getAuthor(), view.getAuthorHeader());
		assertEquals(commitModel.getMessage(), view.getMessageHeader());
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

		DiffBlameModel diffBlameModel = commitApi.diffBlame();

		DiffView view = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click()
				.listCommits()
				.get(0).click();

		assertEquals(commitModel.getAuthor(), view.getAuthorHeader());
		assertEquals(commitModel.getMessage(), view.getMessageHeader());

		List<DiffView.DiffElement> list = view.listDiffs();
		DiffView.DiffElement result = list.get(0);
		result.assertEqualTo(diffBlameModel.getDiffs().get(0));
	}

}
