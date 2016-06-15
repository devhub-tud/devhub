package nl.tudelft.ewi.devhub.server.web.resources.repository;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequestComments;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.entities.*;
import nl.tudelft.ewi.devhub.server.database.entities.comments.PullRequestComment;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.devhub.server.util.MarkDownParser;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.models.CommentResponse;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.web.CucumberModule;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRule;
import org.pegdown.PegDownProcessor;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(JukitoRunner.class)
@UseModules(CucumberModule.class)
public class AbstractProjectPullResourceTest {

    private static final String REPOSITORY_NAME = "JohnCena";
    private static final String PULL_URI = "blah.com/pull";
    private static final long PULL_ID = 1;

    @Mock TemplateEngine templateEngine;
    @Mock HttpServletRequest request;
    @Mock PullRequestComment pullRequestComment;
    @Mock CommentMailer commentMailer;
    @Mock User currentUser;
    @Mock PullRequests pullRequests;
    @Mock PullRequest pullRequest;
    @Mock PullRequestComments pullRequestComments;

    @Inject private RepositoriesApi repositoriesApi;

    @Rule public MockitoJUnitRule mockitoJUnitRule = new MockitoJUnitRule(this);

    private ProjectPullResource projectPullResource;
    private Date commentDate = new Date(150);

    @Before
    public void setUp() throws Throwable {
        Group group = new Group();
        GroupRepository groupRepository = new GroupRepository();
        groupRepository.setRepositoryName(REPOSITORY_NAME);
        group.setRepository(groupRepository);

        when(pullRequests.findById(anyObject(), eq(PULL_ID))).thenReturn(pullRequest);

        projectPullResource = spy(new ProjectPullResource(templateEngine, currentUser, group, null,
                null, pullRequests, null, repositoriesApi, commentMailer, null,
                pullRequestComments, null, null, new MarkDownParser(new PegDownProcessor()), null));

        when(pullRequestComment.getTimestamp()).thenReturn(commentDate);
        when(currentUser.getName()).thenReturn(REPOSITORY_NAME);
        when(pullRequest.getURI()).thenReturn(new URI(PULL_URI));
        doReturn(pullRequestComment).when(projectPullResource).pullRequestCommentFactory(anyString(),
                anyObject());

        when(request.getLocales()).thenReturn(new Vector<Locale>().elements());

    }

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

        CommentResponse resp = projectPullResource.commentOnPullRequest(request, PULL_ID, message);
        assertEquals(expected, resp);

        expected.setContent(wrongMessage);
        expected.setFormattedContent(wrongFormattedMessage);
        resp = projectPullResource.commentOnPullRequest(request, PULL_ID, wrongMessage);
        assertEquals(expected, resp);
    }

}
