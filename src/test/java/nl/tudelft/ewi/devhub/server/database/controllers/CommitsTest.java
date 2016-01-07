package nl.tudelft.ewi.devhub.server.database.controllers;

import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.PersistedBackendTest;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;

import com.google.inject.AbstractModule;

import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.BeforeClass;
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

	private static RepositoriesApi repositories = Mockito.mock(RepositoriesApi.class);
	private static RepositoryApi repository = Mockito.mock(RepositoryApi.class);
	private static  CommitApi commitApi = Mockito.mock(CommitApi.class);
	private static  DetailedCommitModel commit = Mockito.mock(DetailedCommitModel.class);

	@BeforeClass
	private static void before() {

		Mockito.when(repositories.getRepository(Mockito.anyString())).thenReturn(repository);
		Mockito.when(repository.getCommit(Mockito.anyString())).thenReturn(commitApi);
		Mockito.when(commitApi.get()).thenReturn(commit);
		Mockito.when(commit.getParents()).thenReturn(new String[] {});
	}

	public static class CommitsTestModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(RepositoriesApi.class).toInstance(repositories);
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
