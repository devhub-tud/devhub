package nl.tudelft.ewi.devhub.server.database.controllers;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;

import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class CommitsTest {
	
	@Inject
	private Random random;
	
	@Inject
	private Groups groups;
	
	@Inject
	private Courses courses;
	
	@Inject
	private Commits commits;
	
	@Inject
	private Users users;
	
	
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
		assertEquals(expected.getOldLineNumber(), actual.getOldLineNumber());
		assertEquals(expected.getOldFilePath(), actual.getOldFilePath());
		assertEquals(expected.getNewLineNumber(), actual.getNewLineNumber());
		assertEquals(expected.getNewFilePath(), actual.getNewFilePath());
		assertEquals(expected.getTime(), actual.getTime());
		assertEquals(expected.getUser(), actual.getUser());
	}
	
	protected CommitComment createCommitComment(Commit commit) {
		CommitComment comment = new CommitComment();
		comment.setCommit(commit);
		comment.setContent("This is a comment");
		comment.setOldFilePath("dev/null");
		comment.setOldLineNumber(null);
		comment.setNewFilePath(".gitignore");
		comment.setNewLineNumber(1);
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
		Course course = getTestCourse();
		group.setGroupNumber(random.nextLong());
		group.setCourse(course);
		group.setRepositoryName(String.format("courses/%s/group-%s", group.getGroupNumber(), course.getName()));
		return groups.persist(group);
	}
	
	protected Course getTestCourse() {
		return courses.find("TI1705");
	}
	
	protected User student1() {
		return users.find(1);
	}
	
}
