package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.PullRequestBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.backend.mail.PullRequestMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequestComments;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequestComment;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LineWarning;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.models.CommentResponse;
import nl.tudelft.ewi.devhub.server.web.models.DeleteBranchResponse;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.devhub.server.web.models.PullCloseResponse;
import nl.tudelft.ewi.devhub.server.web.resources.views.WarningResolver;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.client.Commit;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.MergeResponse;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@RequestScoped
@Path("courses/{courseCode}/groups/{groupNumber}")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class ProjectPullResource extends Resource {

    private final TemplateEngine templateEngine;
    private final User currentUser;
    private final BuildResults buildResults;
    private final Group group;
    private final PullRequests pullRequests;
    private final CommentBackend commentBackend;
    private final PullRequestBackend pullRequestBackend;
    private final GitServerClient gitClient;
    private final CommentMailer commentMailer;
    private final PullRequestComments pullRequestComments;
    private final PullRequestMailer pullRequestMailer;
    private final HooksResource hooksResource;
    private final Warnings warnings;

    @Inject
    ProjectPullResource(final TemplateEngine templateEngine,
                    final @Named("current.user") User currentUser,
                    final @Named("current.group") Group group,
                    final CommentBackend commentBackend,
                    final BuildResults buildResults,
                    final PullRequests pullRequests,
                    final PullRequestBackend pullRequestBackend,
                    final GitServerClient gitClient,
                    final CommentMailer commentMailer,
                    final PullRequestMailer pullRequestMailer,
                    final PullRequestComments pullRequestComments,
                    final HooksResource hooksResource,
                    final Warnings warnings) {

        this.templateEngine = templateEngine;
        this.group = group;
        this.currentUser = currentUser;
        this.commentBackend = commentBackend;
        this.buildResults = buildResults;
        this.pullRequests = pullRequests;
        this.pullRequestBackend = pullRequestBackend;
        this.gitClient = gitClient;
        this.commentMailer = commentMailer;
        this.pullRequestComments = pullRequestComments;
        this.pullRequestMailer = pullRequestMailer;
        this.hooksResource = hooksResource;
        this.warnings = warnings;
    }

    @POST
    @Path("/pull")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createPullRequest(@Context HttpServletRequest request,
                                      @FormParam("branchName") String branchName)
            throws ApiError, IOException, GitClientException {

        Preconditions.checkNotNull(branchName);

        if(pullRequests.findOpenPullRequest(group, branchName) != null) {
            throw new IllegalArgumentException("There already is an open pull request for " + branchName);
        }

        Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());
        PullRequest pullRequest = new PullRequest();
        pullRequest.setBranchName(branchName);
        pullRequest.setGroup(group);
        pullRequest.setOpen(true);
        pullRequestBackend.createPullRequest(repository, pullRequest);
        pullRequestMailer.sendReviewMail(pullRequest);

        String uri = request.getRequestURI() + "/" + pullRequest.getIssueId();
        return Response.seeOther(URI.create(uri)).build();
    }

    @GET
    @Transactional
    @Path("/pulls")
    public Response getPullRequests(@Context HttpServletRequest request) throws IOException, GitClientException {
        Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());

        List<PullRequest> openPullRequests = pullRequests.findOpenPullRequests(group);
        List<PullRequest> closedPullReqeusts = pullRequests.findClosedPullRequests(group);

        Collection<String> commitIds = Stream.concat(openPullRequests.stream(), closedPullReqeusts.stream())
            .map(PullRequest::getDestination)
            .collect(Collectors.toSet());

        Map<String, Object> parameters = Maps.newLinkedHashMap();
        parameters.put("user", currentUser);
        parameters.put("group", group);
        parameters.put("course", group.getCourse());
        parameters.put("repository", repository);
        parameters.put("openPullRequests", pullRequests.findOpenPullRequests(group));
        parameters.put("closedPullRequests", pullRequests.findClosedPullRequests(group));
        parameters.put("builds", buildResults.findBuildResults(group, commitIds));

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("courses/assignments/group-pulls.ftl", locales, parameters));
    }

    @GET
    @Transactional
    @Path("/pull/{pullId}")
    public Response getPullRequest(@Context HttpServletRequest request,
                                   @PathParam("pullId") long pullId)
            throws ApiError, IOException, GitClientException {

        PullRequest pullRequest = pullRequests.findById(group, pullId);
        Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());
        pullRequestBackend.updatePullRequest(repository, pullRequest);
        Commit commit = repository.retrieveCommit(pullRequest.getDestination());

        val destinationId = pullRequest.getDestination();
        val mergeBaseId = pullRequest.getMergeBase();
        val diffBlameModel = repository.retrieveCommit(destinationId).diffBlame(mergeBaseId);
        val commitIds = Lists.transform(diffBlameModel.getCommits(), CommitModel::getCommit);
        val resolver = pullRequestBackend.getEventResolver(pullRequest, diffBlameModel, group);

        Map<String, Object> parameters = Maps.newLinkedHashMap();
        parameters.put("user", currentUser);
        parameters.put("group", group);
        parameters.put("commit", commit);
        parameters.put("pullRequest", pullRequest);
        parameters.put("events", resolver.getEvents());
        parameters.put("repository", repository);
        parameters.put("builds", buildResults.findBuildResults(group, commitIds));

        List<LineWarning> lineWarnings = warnings.getLineWarningsFor(group, commitIds);
        parameters.put("lineWarnings", new WarningResolver(lineWarnings));

        try {
            nl.tudelft.ewi.git.client.Branch branch = repository.retrieveBranch(pullRequest.getBranchName());
            parameters.put("branch", branch);
        }
        catch (NotFoundException e) {
            // Branch has been removed.
        }

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("project-pull.ftl", locales, parameters));
    }

    @POST
    @Transactional
    @Path("/pull/{pullId}/comment")
    @Produces(MediaType.APPLICATION_JSON)
    public CommentResponse commentOnPullRequest(@Context HttpServletRequest request,
                                                @PathParam("pullId") long pullId,
                                                String content) {

        PullRequest pullRequest = pullRequests.findById(group, pullId);
        PullRequestComment comment = new PullRequestComment();

        comment.setContent(content);
        comment.setPullRequest(pullRequest);
        comment.setUser(currentUser);
        comment.setTime(new Date());
        pullRequestComments.persist(comment);

        CommentResponse response = new CommentResponse();
        response.setContent(content);
        response.setName(currentUser.getName());
        response.setDate(comment.getTime().toString());
        response.setCommentId(comment.getCommentId());

        String redirect = String.format("/courses/%s/groups/%d/pull/%d",
                group.getCourse().getCode(),
                group.getGroupNumber(),
                pullId);
        commentMailer.sendCommentMail(comment, redirect);

        return response;
    }

    @GET
    @Transactional
    @Path("/pull/{pullId}/diff")
    public Response getPullRequestDiff(@Context HttpServletRequest request,
                                       @PathParam("pullId") long pullId)
            throws ApiError, IOException, GitClientException {

        PullRequest pullRequest = pullRequests.findById(group, pullId);
        Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());
        pullRequestBackend.updatePullRequest(repository, pullRequest);
        DiffBlameModel diffBlameModel = getDiffBlameModelForPull(pullRequest, repository);
        Commit commit = repository.retrieveCommit(pullRequest.getDestination());
        List<String> commitIds = Lists.transform(diffBlameModel.getCommits(), CommitModel::getCommit);

        Map<String, Object> parameters = Maps.newLinkedHashMap();
        parameters.put("user", currentUser);
        parameters.put("group", group);
        parameters.put("commit", commit);
        parameters.put("commentChecker", commentBackend.getCommentChecker(commitIds));
        parameters.put("pullRequest", pullRequest);
        parameters.put("repository", repository);
        parameters.put("diffViewModel", diffBlameModel);
        parameters.put("builds", buildResults.findBuildResults(group, commitIds));

        List<LineWarning> lineWarnings = warnings.getLineWarningsFor(group, commitIds);
        parameters.put("lineWarnings", new WarningResolver(lineWarnings));

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("project-pull-diff-view.ftl", locales, parameters));
    }

    private static DiffBlameModel getDiffBlameModelForPull(PullRequest pullRequest, Repository repository) throws GitClientException {
        String destinationId = pullRequest.getDestination();
        String mergeBaseId = pullRequest.getMergeBase();
        return repository.retrieveCommit(destinationId).diffBlame(mergeBaseId);
    }

    @POST
    @Path("/pull/{pullId}/close")
    @Produces(MediaType.APPLICATION_JSON)
    public PullCloseResponse closePullRequest(@Context HttpServletRequest request,
                                              @PathParam("pullId") long pullId) {

        PullRequest pullRequest = pullRequests.findById(group, pullId);
        pullRequest.setOpen(false);
        log.debug("Closing pull request {}", pullRequest);
        pullRequests.merge(pullRequest);

        PullCloseResponse response = new PullCloseResponse();
        response.setClosed(true);
        return response;
    }

    @POST
    @Path("/pull/{pullId}/delete-branch")
    @Produces(MediaType.APPLICATION_JSON)
    public DeleteBranchResponse deleteBranch(@Context HttpServletRequest request,
                                             @PathParam("pullId") long pullId)
            throws GitClientException {

        PullRequest pullRequest = pullRequests.findById(group, pullId);
        Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());

        if(pullRequest.isOpen()) {
            throw new IllegalStateException("Cannot remove branch if pull request is still open");
        }

        DeleteBranchResponse response = new DeleteBranchResponse();
        String branchName = pullRequest.getBranchName();

        if(!pullRequests.openPullRequestExists(group, branchName)) {
            nl.tudelft.ewi.git.client.Branch branch = repository.retrieveBranch(branchName);
            branch.delete();
            log.debug("Deleted branch {}", pullRequest.getBranchName());
            response.setClosed(true);
        }

        return response;
    }

    @POST
    @Path("/pull/{pullId}/merge")
    @Produces(MediaType.APPLICATION_JSON)
    public MergeResponse mergePullRequest(@Context HttpServletRequest request,
                                          @PathParam("pullId") long pullId) throws GitClientException, IOException {

        PullRequest pullRequest = pullRequests.findById(group, pullId);
        Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());
        pullRequestBackend.updatePullRequest(repository, pullRequest);

        nl.tudelft.ewi.git.client.Branch branch = repository.retrieveBranch(pullRequest.getBranchName());
        String message = String.format("Merge pull request #%s from %s", pullRequest.getIssueId(), branch.getSimpleName());
        MergeResponse response = branch.merge(message, currentUser.getName(), currentUser.getEmail());
        log.debug("Merged pull request {}", pullRequest);

        if(response.isSuccess()) {
            pullRequest.setOpen(false);
            pullRequest.setMerged(true);
            pullRequests.merge(pullRequest);
            // Currently the git server fails to correctly trigger the push hook
            // Therefore, we invoke the githook manually
            // See: https://github.com/devhub-tud/devhub/issues/140
            hooksResource.onGitPush(request, new GitPush(group.getRepositoryName()));
        }

        return response;
    }

}
