package nl.tudelft.ewi.devhub.server.database.controllers;

import lombok.Getter;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.backend.PersistedBackendTest;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Repository;

import com.google.inject.AbstractModule;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JukitoRunner.class)
@UseModules({TestDatabaseModule.class, CommitsTest.CommitsTestModule.class})
public class CommitsTest extends PersistedBackendTest {

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

	@Inject @Getter private Groups groups;
	@Inject @Getter private CourseEditions courses;
	@Inject @Getter private Users users;
	@Inject private Commits commits;

	private User user;
	private Group group;

	@Before
	public void setup() {
		user = createUser();
		group = createGroup(createCourseEdition(), user);
	}
	
	@Test
	public void testEnsureCommitInRepository() {
		Commit commit = createCommit(group.getRepository());
		assertEquals(group.getRepository(), commit.getRepository());
	}

	@Test
	public void testEnsureCommentInCommit() {
		Commit commit = createCommit(group.getRepository());
		CommitComment expected = createCommitComment(commit);
		List<CommitComment> comments = commit.getComments();
		assertEquals("Expected size 1 for list of comments", 1, comments.size());
		
		CommitComment actual = comments.get(0);
		assertEquals(expected.getCommit(), actual.getCommit());
		assertEquals(expected.getContent(), actual.getContent());

		assertEquals(expected.getSource(), actual.getSource());
		assertNotNull(actual.getTimestamp());
		assertEquals(expected.getUser(), actual.getUser());
	}
	
	protected CommitComment createCommitComment(Commit commit) {
		CommitComment comment = new CommitComment();
		comment.setCommit(commit);
		comment.setContent("This is a comment");

		Source source = new Source();
		source.setSourceCommit(commit);
		source.setSourceFilePath(".gitignore");
		source.setSourceLineNumber(1);

		comment.setSource(source);
		comment.setUser(user);
		commit.getComments().add(comment);
		commits.merge(commit);
		return comment;
	}
	
	protected Commit createCommit(RepositoryEntity repository) {
		return commits.ensureExists(repository, UUID.randomUUID().toString());
	}
	
}
