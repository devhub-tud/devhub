package nl.tudelft.ewi.devhub.webtests;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.GroupMemberships;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupMembership;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.DiffView;
import nl.tudelft.ewi.devhub.webtests.views.DiffView.DiffElement;
import nl.tudelft.ewi.devhub.webtests.views.ProjectView;
import nl.tudelft.ewi.devhub.webtests.views.ProjectView.Commit;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.RepositoriesMock;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.CreateRepositoryModelFactory;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.DiffModel.Type;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class ProjectTest extends WebTest {
	
	public static final String COURSE_CODE = "TI1705";
	public static final String COURSE_NAME = "Software Quality & Testing";
	public static final int GROUP_NUMBER = 1;
	public static final String TEMPLATE_REPOSITORY = "TEMPLATE_REPOSITORY";
	public static final String COMMIT_ID = "6f69819c39b87566a65a2a005a6553831f6d7e7c";
	public static final String COMMIT_MESSAGE = "Initial commit";
	public static final String REPOSITORY_NAME = String.format("courses/%s/group-%s", COURSE_CODE, GROUP_NUMBER);
	public static final String REPOSITORY_URL = String.format("ssh://git@localhost:2222/%s", REPOSITORY_NAME);
	
	private static GitServerClient gitServerClient;
	private static User user;
	private static DetailedRepositoryModel repository;
	private static String GROUP_NAME;
	
	@BeforeClass
	public static void setUpRepository() {
		gitServerClient = getGitServerClient();
		// Create a course and group to test against 
		user = fetchUser();
		Course course = fetchOrCreateCourse();
		Group group = fetchOrCreateGroup(course, GROUP_NUMBER);
		createGroupMembershipIfNotExists(group, user);
		repository = createRepositoryIfNotExists(group);
	}
	
	private static User fetchUser() {
		return getServer().getInstance(Users.class).findByNetId(NET_ID);
	}
	
	private static Course fetchOrCreateCourse() {
		Course course;
		try {
			course = getServer().getInstance(Courses.class).find(COURSE_CODE);
		} catch (EntityNotFoundException exception) {
			course = new Course();
			course.setCode(COURSE_CODE);
			course.setName(COURSE_NAME);
			course.setStart(new Date());
			getServer().getInstance(Courses.class).persist(course);			
		}
		return course;
	}
	
	private static Group fetchOrCreateGroup(Course course, long groupNumber) {
		Group group;
		
		try {
			group = getServer().getInstance(Groups.class).find(course, groupNumber);
		} catch (EntityNotFoundException exception) {
			group = new Group();
			group.setGroupNumber(groupNumber);
			group.setRepositoryName(REPOSITORY_NAME);
			group.setCourse(course);
			getServer().getInstance(Groups.class).persist(group);
		}
		
		GroupModel groupModel = new GroupModel();
		GROUP_NAME = group.getGroupName();
		groupModel.setName(GROUP_NAME);
		gitServerClient.groups().create(groupModel);		
		
		return group;
	}
	
	private static void createGroupMembershipIfNotExists(Group group, User user) {
		if(!getServer().getInstance(GroupMemberships.class).listParticipating(user).contains(group)) {
			GroupMembership groupMembership = new GroupMembership();
			groupMembership.setUser(user);
			groupMembership.setGroup(group);
			getServer().getInstance(GroupMemberships.class).persist(groupMembership);
		}
	}
	
	private static DetailedRepositoryModel createRepositoryIfNotExists(Group group) {
		CreateRepositoryModel repositoryModel = CreateRepositoryModelFactory.create(TEMPLATE_REPOSITORY, REPOSITORY_NAME, REPOSITORY_URL);
		repositoryModel.setPermissions(ImmutableMap.<String, Level> of(group.getGroupName(),
				Level.READ_WRITE));
		gitServerClient.repositories().create(repositoryModel);
		return gitServerClient.repositories().retrieve(repositoryModel);
	}
	
	private static CommitModel createInitialCommit(DetailedRepositoryModel repository, User user) {
		CommitModel commit = new CommitModel();
		commit.setAuthor(gitServerClient.users().retrieve(user.getNetId()).getName());
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
		createInitialCommit(repository, user);
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
		createInitialCommit(repository, user);
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
		model.setRaw(new String[] { "Line 1", "Line 2", "Line 3"});
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
		
//		assertArrayEquals(model.getRaw(), result.getRaw());
	}
	
	@AfterClass
	public static void clearRepository() {
		GitServerClient gitServerClient = getGitServerClient();
		gitServerClient.repositories().delete(
				gitServerClient.repositories().retrieve(REPOSITORY_NAME));
		gitServerClient.groups().delete(
				gitServerClient.groups().retrieve(GROUP_NAME));
	}
	
}
