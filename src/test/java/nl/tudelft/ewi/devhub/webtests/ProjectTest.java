package nl.tudelft.ewi.devhub.webtests;

import java.util.Date;

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
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.CreateRepositoryModelFactory;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;

import org.junit.After;
import org.junit.Before;
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
	public static final String REPOSITORY_URL = String.format("shh://localhost:2222/%s", REPOSITORY_NAME);
	
	private static GitServerClient gitServerClient;
	
	
	@Before
	public void setUpRepository() {
		gitServerClient = getGitServerClient();
		
		User user = fetchUser();
		Course course = fetchOrCreateCourse();
		Group group = fetchOrCreateGroup(course, GROUP_NUMBER);
		createGroupMembershipIfNotExists(group, user);
		
		DetailedRepositoryModel repository = createRepositoryIfNotExists(group);
		CommitModel initialCommit = createInitialCommit(repository, user);
	}
	
	private User fetchUser() {
		return getServer().getInstance(Users.class).findByNetId(NET_ID);
	}
	
	private Course fetchOrCreateCourse() {
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
	
	private Group fetchOrCreateGroup(Course course, long groupNumber) {
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
		groupModel.setName(group.getGroupName());
		gitServerClient.groups().create(groupModel);		
		
		return group;
	}
	
	private void createGroupMembershipIfNotExists(Group group, User user) {
		if(!getServer().getInstance(GroupMemberships.class).listParticipating(user).contains(group)) {
			GroupMembership groupMembership = new GroupMembership();
			groupMembership.setUser(user);
			groupMembership.setGroup(group);
			getServer().getInstance(GroupMemberships.class).persist(groupMembership);
		}
	}
	
	private DetailedRepositoryModel createRepositoryIfNotExists(Group group) {
		CreateRepositoryModel repositoryModel = CreateRepositoryModelFactory.create(TEMPLATE_REPOSITORY, REPOSITORY_NAME, REPOSITORY_URL);
		repositoryModel.setPermissions(ImmutableMap.<String, Level> of(group.getGroupName(),
				Level.READ_WRITE));
		gitServerClient.repositories().create(repositoryModel);
		return gitServerClient.repositories().retrieve(repositoryModel);
	}
	
	private CommitModel createInitialCommit(DetailedRepositoryModel repository, User user) {
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
//		ProjectView view = 
			openLoginScreen().login(NET_ID, PASSWORD)
				.toProjectsView().listMyProjects().get(0).click();
	}
	
	@After
	public void clearRepository() {
//		GitServerClient gitServerClient = getGitServerClient();
//		gitServerClient.repositories().delete(
//				gitServerClient.repositories().retrieve(REPOSITORY_NAME));
//		gitServerClient.groups().delete(
//				gitServerClient.groups().retrieve(group));
	}
	
}
