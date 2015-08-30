package nl.tudelft.ewi.devhub.server.web.resources.repository;

import lombok.extern.slf4j.Slf4j;

import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.PullRequestBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.backend.mail.PullRequestMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequestComments;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.comments.PullRequestComment;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LineWarning;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.models.CommentResponse;
import nl.tudelft.ewi.devhub.server.web.models.DeleteBranchResponse;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.devhub.server.web.models.PullCloseResponse;
import nl.tudelft.ewi.devhub.server.web.resources.HooksResource;
import nl.tudelft.ewi.devhub.server.web.resources.Resource;
import nl.tudelft.ewi.devhub.server.web.resources.views.WarningResolver;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.client.Commit;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.MergeResponse;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;

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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public abstract class AbstractProjectPullResource extends Resource {

	protected final TemplateEngine templateEngine;
	protected final User currentUser;
	protected final BuildResults buildResults;
	protected final PullRequests pullRequests;
	protected final CommentBackend commentBackend;
	protected final PullRequestBackend pullRequestBackend;
	protected final GitServerClient gitClient;
	protected final CommentMailer commentMailer;
	protected final PullRequestComments pullRequestComments;
	protected final PullRequestMailer pullRequestMailer;
	protected final HooksResource hooksResource;
	protected final Warnings warnings;

    protected AbstractProjectPullResource(final TemplateEngine templateEngine,
								final @Named("current.user") User currentUser,
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

	protected abstract RepositoryEntity getRepositoryEntity();

	protected Map<String, Object> getBaseParameters() {
		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("repositoryEntity", getRepositoryEntity());
		return parameters;
	}

	@POST
    @Path("/pull")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createPullRequest(@Context HttpServletRequest request,
                                      @FormParam("branchName") String branchName)
            throws ApiError, IOException, GitClientException {

        Preconditions.checkNotNull(branchName);

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		if(pullRequests.findOpenPullRequest(repositoryEntity, branchName) != null) {
            throw new IllegalArgumentException("There already is an open pull request for " + branchName);
        }

        Repository repository = gitClient.repositories().retrieve(repositoryEntity.getRepositoryName());
        PullRequest pullRequest = new PullRequest();
        pullRequest.setBranchName(branchName);
        pullRequest.setRepository(repositoryEntity);
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
		RepositoryEntity repositoryEntity = getRepositoryEntity();
        Repository repository = gitClient.repositories().retrieve(repositoryEntity.getRepositoryName());

        List<PullRequest> openPullRequests = pullRequests.findOpenPullRequests(repositoryEntity);
        List<PullRequest> closedPullReqeusts = pullRequests.findClosedPullRequests(repositoryEntity);

        Map<String, Object> parameters = getBaseParameters();
        parameters.put("repository", repository);
        parameters.put("openPullRequests", openPullRequests);
        parameters.put("closedPullRequests", closedPullReqeusts);

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("courses/assignments/group-pulls.ftl", locales, parameters));
    }

    @GET
    @Transactional
    @Path("/pull/{pullId}")
    public Response getPullRequest(@Context HttpServletRequest request,
                                   @PathParam("pullId") long pullId)
            throws ApiError, IOException, GitClientException {

        RepositoryEntity repositoryEntity = getRepositoryEntity();
        PullRequest pullRequest = pullRequests.findById(repositoryEntity, pullId);
        Repository repository = gitClient.repositories().retrieve(repositoryEntity.getRepositoryName());
        pullRequestBackend.updatePullRequest(repository, pullRequest);

        String destinationId = pullRequest.getDestination().getCommitId();
        String mergeBaseId = pullRequest.getMergeBase().getCommitId();
        Commit commit = repository.retrieveCommit(destinationId);

        DiffBlameModel diffBlameModel = repository.retrieveCommit(destinationId).diffBlame(mergeBaseId);
        List<String> commitIds = Lists.transform(diffBlameModel.getCommits(), CommitModel::getCommit);
        PullRequestBackend.EventResolver resolver = pullRequestBackend.getEventResolver(pullRequest, diffBlameModel, repositoryEntity);

        Map<String, Object> parameters = getBaseParameters();
        parameters.put("commit", commit);
        parameters.put("pullRequest", pullRequest);
        parameters.put("events", resolver.getEvents());
        parameters.put("repository", repository);
        parameters.put("builds", buildResults.findBuildResults(repositoryEntity, commitIds));

        List<LineWarning> lineWarnings = warnings.getLineWarningsFor(repositoryEntity, commitIds);
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

		RepositoryEntity repositoryEntity = getRepositoryEntity();
        PullRequest pullRequest = pullRequests.findById(repositoryEntity, pullId);
        PullRequestComment comment = new PullRequestComment();

        comment.setContent(content);
        comment.setPullRequest(pullRequest);
        comment.setUser(currentUser);
        pullRequestComments.persist(comment);

        CommentResponse response = new CommentResponse();
        response.setContent(content);
        response.setName(currentUser.getName());
        response.setDate(comment.getTimestamp().toString());
        response.setCommentId(comment.getCommentId());

        String redirect = pullRequest.getURI().toASCIIString();
        commentMailer.sendCommentMail(comment, redirect);

        return response;
    }

    @GET
    @Transactional
    @Path("/pull/{pullId}/diff")
    public Response getPullRequestDiff(@Context HttpServletRequest request,
                                       @PathParam("pullId") long pullId)
            throws ApiError, IOException, GitClientException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
        PullRequest pullRequest = pullRequests.findById(repositoryEntity, pullId);
        Repository repository = gitClient.repositories().retrieve(repositoryEntity.getRepositoryName());
        pullRequestBackend.updatePullRequest(repository, pullRequest);
        DiffBlameModel diffBlameModel = getDiffBlameModelForPull(pullRequest, repository);
        Commit commit = repository.retrieveCommit(pullRequest.getDestination().getCommitId());
        List<String> commitIds = Lists.transform(diffBlameModel.getCommits(), CommitModel::getCommit);

        Map<String, Object> parameters = getBaseParameters();
        parameters.put("commit", commit);
        parameters.put("commentChecker", commentBackend.getCommentChecker(commitIds));
        parameters.put("pullRequest", pullRequest);
        parameters.put("repository", repository);
        parameters.put("diffViewModel", diffBlameModel);
        parameters.put("builds", buildResults.findBuildResults(repositoryEntity, commitIds));

        List<LineWarning> lineWarnings = warnings.getLineWarningsFor(repositoryEntity, commitIds);
        parameters.put("lineWarnings", new WarningResolver(lineWarnings));

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("project-pull-diff-view.ftl", locales, parameters));
    }

    private static DiffBlameModel getDiffBlameModelForPull(PullRequest pullRequest, Repository repository) throws GitClientException {
        String destinationId = pullRequest.getDestination().getCommitId();
        String mergeBaseId = pullRequest.getMergeBase().getCommitId();
        return repository.retrieveCommit(destinationId).diffBlame(mergeBaseId);
    }

    @POST
    @Path("/pull/{pullId}/close")
    @Produces(MediaType.APPLICATION_JSON)
    public PullCloseResponse closePullRequest(@Context HttpServletRequest request,
                                              @PathParam("pullId") long pullId) {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
        PullRequest pullRequest = pullRequests.findById(repositoryEntity, pullId);
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

		RepositoryEntity repositoryEntity = getRepositoryEntity();
        PullRequest pullRequest = pullRequests.findById(repositoryEntity, pullId);
        Repository repository = gitClient.repositories().retrieve(repositoryEntity.getRepositoryName());

        if(pullRequest.isOpen()) {
            throw new IllegalStateException("Cannot remove branch if pull request is still open");
        }

        DeleteBranchResponse response = new DeleteBranchResponse();
        String branchName = pullRequest.getBranchName();

        if(!pullRequests.openPullRequestExists(repositoryEntity, branchName)) {
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

		RepositoryEntity repositoryEntity = getRepositoryEntity();
        PullRequest pullRequest = pullRequests.findById(repositoryEntity, pullId);
        Repository repository = gitClient.repositories().retrieve(repositoryEntity.getRepositoryName());
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
            hooksResource.onGitPush(request, new GitPush(repositoryEntity.getRepositoryName()));
        }

        return response;
    }

}
