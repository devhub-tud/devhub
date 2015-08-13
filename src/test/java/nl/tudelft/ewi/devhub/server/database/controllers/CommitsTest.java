package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.AbstractModule;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Repository;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(JukitoRunner.class)
@UseModules({TestDatabaseModule.class, CommitsTest.CommitsTestModule.class})
public class CommitsTest {

	public static class CommitsTestModule extends AbstractModule {

		@Override
		@SneakyThrows
		protected void configure() {
			Repositories repositories = Mockito.mock(Repositories.class);
			Repository repository = Mockito.mock(Repository.class);
			nl.tudelft.ewi.git.client.Commit commit = Mockito.mock(nl.tudelft.ewi.git.client.Commit.class);

			bind(Repositories.class).toInstance(repositories);
			Mockito.when(repositories.retrieve(Mockito.anyString())).thenReturn(repository);
			Mockito.when(repository.retrieveCommit(Mockito.anyString())).thenReturn(commit);
			Mockito.when(commit.getParents()).thenReturn(new String[] {});
		}

	}
	
	@Inject
	private Random random;
	
	@Inject
	private Groups groups;
	
	@Inject
	private Courses courses;

	@Inject
	private Users users;

	@Inject
	private EntityManager entityManager;

	@Inject
	private Repositories repositories;
	@Inject
	private Commits commits;
	
	@Test
	public void testEnsureCommitInRepository() {
		Group group = createGroup();
		Commit commit = createCommit(group);
		assertEquals(group, commit.getRepository());
	}
	
	@Test
	public void testEnsureCommentInCommit() {
		Group group = createGroup();
		Commit commit = createCommit(group);
		CommitComment expected = createCommitComment(commit);
		List<CommitComment> comments = commit.getComments();
		assertEquals("Expected size 1 for list of comments", 1, comments.size());
		
		CommitComment actual = comments.get(0);
		assertEquals(expected.getCommit(), actual.getCommit());
		assertEquals(expected.getContent(), actual.getContent());
		//assertEquals(expected.getOldLineNumber(), actual.getOldLineNumber());
		//assertEquals(expected.getOldFilePath(), actual.getOldFilePath());
		//assertEquals(expected.getNewLineNumber(), actual.getNewLineNumber());
		//assertEquals(expected.getNewFilePath(), actual.getNewFilePath());
		assertEquals(expected.getTime(), actual.getTime());
		assertEquals(expected.getUser(), actual.getUser());
	}
	
	protected CommitComment createCommitComment(Commit commit) {
		CommitComment comment = new CommitComment();
		comment.setCommit(commit);
		comment.setContent("This is a comment");
		//comment.setOldFilePath("dev/null");
		//comment.setOldLineNumber(null);
		//comment.setNewFilePath(".gitignore");
		//comment.setNewLineNumber(1);
		comment.setTime(new Date());
		comment.setUser(student1());
		commit.getComments().add(comment);
		commits.merge(commit);
		return comment;
	}
	
	protected Commit createCommit(Group repository) {
		return commits.ensureExists(repository, UUID.randomUUID().toString());
	}
	
	protected Group createGroup() {
		Group group = new Group();
		CourseEdition course = getTestCourse();
		group.setGroupNumber(random.nextLong());
		group.setCourse(course);
		group.setRepositoryName(String.format("courses/%s/group-%s", group.getGroupNumber(), course.getName()));
		return groups.persist(group);
	}
	
	protected CourseEdition getTestCourse() {
		return courses.find("TI1705");
	}
	
	protected User student1() {
		return users.find(1);
	}
	
}
