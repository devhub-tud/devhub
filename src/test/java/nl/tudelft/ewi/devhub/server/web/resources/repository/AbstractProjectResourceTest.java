package nl.tudelft.ewi.devhub.server.web.resources.repository;

import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.AsyncEventBus;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.val;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.entities.*;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;
import nl.tudelft.ewi.devhub.server.util.MarkDownParser;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.models.CommentResponse;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.web.CloneStepDefinitions;
import nl.tudelft.ewi.git.web.CucumberModule;
import nl.tudelft.ewi.git.web.MergeStepDefinitions;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pegdown.PegDownProcessor;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(JukitoRunner.class)
@UseModules(CucumberModule.class)
public class AbstractProjectResourceTest {

    private static final String REPOSITORY_NAME = "JohnCena";
    private static final String BRANCH_NAME = "behindBranch";
    private static final String COMMIT_ID = "1";

    @Mock TemplateEngine templateEngine;
    @Mock HttpServletRequest request;
    @Mock Commits commits;
    @Mock Commit commit;
    @Mock CommitComments commitComments;
    @Mock CommitComment commitComment;
    @Mock CommentMailer commentMailer;
    @Mock CommentBackend commentBackend;
    @Mock User currentUser;

    @Captor ArgumentCaptor<Map<String, Object>> argumentCaptor;

    @Inject private RepositoriesApi repositoriesApi;
    @Inject private Injector injector;

    private CloneStepDefinitions cloneStepDefinitions;
    private MergeStepDefinitions mergeStepDefinitions;
    private DetailedRepositoryModel detailedRepositoryModel;
    private ProjectResource projectResource;
    private Date commentDate = new Date(150);

    @Before
    public void setUp() throws Throwable {
        MockitoAnnotations.initMocks(this);
        cloneStepDefinitions = new CloneStepDefinitions();
        injector.injectMembers(cloneStepDefinitions);

        val crm = new CreateRepositoryModel();
        crm.setTemplateRepository("https://github.com/SERG-Delft/jpacman-template.git");
        crm.setName(REPOSITORY_NAME);
        crm.setPermissions(ImmutableMap.of("me", RepositoryModel.Level.ADMIN));
        detailedRepositoryModel = repositoriesApi.createRepository(crm);

        cloneStepDefinitions.iCloneRepository(REPOSITORY_NAME);

        Group group = new Group();
        GroupRepository groupRepository = new GroupRepository();
        groupRepository.setRepositoryName(REPOSITORY_NAME);
        group.setRepository(groupRepository);

        when(commits.ensureExists(any(), any())).thenReturn(commit);

        projectResource = spy(new ProjectResource(templateEngine, currentUser, group, commentBackend, null,
                null, repositoriesApi, null, commitComments, commentMailer, commits, null, null,
                null, null, null, new MarkDownParser(new PegDownProcessor()), new AsyncEventBus(Executors.newCachedThreadPool())));

        when(commitComment.getTimestamp()).thenReturn(commentDate);
        when(currentUser.getName()).thenReturn(REPOSITORY_NAME);
        doReturn(commitComment).when(projectResource).commitCommentFactory(anyString(), any(),
                eq(COMMIT_ID));

        when(request.getLocales()).thenReturn(new Vector<Locale>().elements());

        mergeStepDefinitions = new MergeStepDefinitions();
        injector.injectMembers(mergeStepDefinitions);

    }

    /*
     * TODO: Implement this after next seminar, git-server needs an update.
    @Test
    public void testDeleteBehindBranch() throws Throwable {
        cloneStepDefinitions.isAheadOf(BRANCH_NAME, "master");

        Response response = projectResource.deleteBehindBranch(request, BRANCH_NAME, "");
        verify(templateEngine).process(anyString(), any(), argumentCaptor.capture());
        System.out.println("\n\n\n\n\n=======1:\n" + argumentCaptor.getValue());

    }*/

    @Test
    public void testEmojiParsing() throws IOException, ApiError {
        String message = ":grinning:";
        String wrongMessage = ":blah :grinning";
        String formattedMessage = "<p>\uD83D\uDE00</p>";
        String wrongFormattedMessage = "<p>:blah :grinning</p>";

        CommentResponse expected = new CommentResponse();
        expected.setName(REPOSITORY_NAME);
        expected.setDate(commentDate.toString());
        expected.setContent(message);
        expected.setFormattedContent(formattedMessage);

        CommentResponse resp = projectResource.commentOnPull(request, COMMIT_ID, message, null,
                null, null, null);
        assertEquals(expected, resp);

        expected.setContent(wrongMessage);
        expected.setFormattedContent(wrongFormattedMessage);
        resp = projectResource.commentOnPull(request, COMMIT_ID, wrongMessage, null, null, null,
                null);
        assertEquals(expected, resp);
    }

}
