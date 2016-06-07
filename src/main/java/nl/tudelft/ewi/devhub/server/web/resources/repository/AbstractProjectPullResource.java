package nl.tudelft.ewi.devhub.server.web.resources.repository;

import com.vdurmont.emoji.EmojiParser;
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
import nl.tudelft.ewi.devhub.server.util.MarkDownParser;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.models.CommentResponse;
import nl.tudelft.ewi.devhub.server.web.models.DeleteBranchResponse;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.devhub.server.web.models.PullCloseResponse;
import nl.tudelft.ewi.devhub.server.web.resources.HooksResource;
import nl.tudelft.ewi.devhub.server.web.resources.Resource;
import nl.tudelft.ewi.devhub.server.web.resources.views.WarningResolver;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.MergeResponse;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
	protected final RepositoriesApi repositoriesApi;
	protected final CommentMailer commentMailer;
	protected final PullRequestComments pullRequestComments;
	protected final PullRequestMailer pullRequestMailer;
	protected final HooksResource hooksResource;
	protected final Warnings warnings;
	protected final MarkDownParser markDownParser;

	protected AbstractProjectPullResource(final TemplateEngine templateEngine,
	                                      final @Named("current.user") User currentUser,
	                                      final CommentBackend commentBackend,
	                                      final BuildResults buildResults,
	                                      final PullRequests pullRequests,
	                                      final PullRequestBackend pullRequestBackend,
	                                      final RepositoriesApi repositoriesApi,
	                                      final CommentMailer commentMailer,
	                                      final PullRequestMailer pullRequestMailer,
	                                      final PullRequestComments pullRequestComments,
	                                      final HooksResource hooksResource,
	                                      final Warnings warnings,
										  final MarkDownParser markDownParser) {

		this.templateEngine = templateEngine;
		this.currentUser = currentUser;
		this.commentBackend = commentBackend;
		this.buildResults = buildResults;
		this.pullRequests = pullRequests;
		this.pullRequestBackend = pullRequestBackend;
		this.repositoriesApi = repositoriesApi;
		this.commentMailer = commentMailer;
		this.pullRequestComments = pullRequestComments;
		this.pullRequestMailer = pullRequestMailer;
		this.hooksResource = hooksResource;
		this.warnings = warnings;
		this.markDownParser = markDownParser;
	}

	protected abstract RepositoryEntity getRepositoryEntity();

	protected RepositoryApi getRepositoryApi(RepositoryEntity repositoryEntity) {
		return repositoriesApi.getRepository(repositoryEntity.getRepositoryName());
	}

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
		throws ApiError, IOException {

		Preconditions.checkNotNull(branchName);

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		if (pullRequests.findOpenPullRequest(repositoryEntity, branchName).isPresent()) {
			throw new IllegalArgumentException("There already is an open pull request for " + branchName);
		}

		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);

		PullRequest pullRequest = new PullRequest();
		pullRequest.setBranchName(branchName);
		pullRequest.setRepository(repositoryEntity);
		pullRequest.setOpen(true);

		pullRequestBackend.createPullRequest(repositoryApi, pullRequest);
		pullRequestMailer.sendReviewMail(pullRequest);

		String uri = request.getRequestURI() + "/" + pullRequest.getIssueId();
		return Response.seeOther(URI.create(uri)).build();
	}

	@GET
	@Transactional
	@Path("/pulls")
	public Response getPullRequests(@Context HttpServletRequest request) throws IOException {
		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);
		RepositoryModel repository = repositoryApi.getRepositoryModel();

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
		throws ApiError, IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);
		RepositoryModel repository = repositoryApi.getRepositoryModel();

		PullRequest pullRequest = pullRequests.findById(repositoryEntity, pullId);
		pullRequestBackend.updatePullRequest(repositoryApi, pullRequest);

		String destinationId = pullRequest.getDestination().getCommitId();
		String mergeBaseId = pullRequest.getMergeBase().getCommitId();

		CommitApi commitApi = repositoryApi.getCommit(destinationId);
		CommitModel commit = commitApi.get();

		DiffBlameModel diffBlameModel = commitApi.diffBlame(mergeBaseId);
		List<String> commitIds = Lists.transform(diffBlameModel.getCommits(), CommitModel::getCommit);
		PullRequestBackend.EventResolver resolver = pullRequestBackend.getEventResolver(pullRequest, diffBlameModel, repositoryEntity);

		Map<String, Object> parameters = getBaseParameters();
		parameters.put("commit", commit);
		parameters.put("pullRequest", pullRequest);
		parameters.put("events", resolver.getEvents());
		parameters.put("repository", repository);
		parameters.put("builds", buildResults.findBuildResults(repositoryEntity, commitIds));

		List<LineWarning> lineWarnings = warnings.getLineWarningsFor(repositoryEntity, destinationId);
		parameters.put("lineWarnings", new WarningResolver(lineWarnings));

		try {
			BranchModel branch = repositoryApi.getBranch(pullRequest.getBranchName()).get();
			parameters.put("branch", branch);
		} catch (NotFoundException e) {
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

		String contentWithEmojis = EmojiParser.parseToUnicode(content);
        response.setFormattedContent(markDownParser.markdownToHtml(contentWithEmojis));

		String redirect = pullRequest.getURI().toASCIIString();
		commentMailer.sendCommentMail(comment, redirect);

		return response;
	}

	@GET
	@Transactional
	@Path("/pull/{pullId}/diff")
	public Response getPullRequestDiff(@Context HttpServletRequest request,
	                                   @PathParam("pullId") long pullId)
		throws ApiError, IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);
		RepositoryModel repository = repositoryApi.getRepositoryModel();

		PullRequest pullRequest = pullRequests.findById(repositoryEntity, pullId);
		pullRequestBackend.updatePullRequest(repositoryApi, pullRequest);

		DiffBlameModel diffBlameModel = getDiffBlameModelForPull(pullRequest, repositoryApi);
		CommitModel commit = repositoryApi.getCommit(pullRequest.getDestination().getCommitId()).get();
		List<String> commitIds = Lists.transform(diffBlameModel.getCommits(), CommitModel::getCommit);

		Map<String, Object> parameters = getBaseParameters();
		parameters.put("commit", commit);
		parameters.put("commentChecker", commentBackend.getCommentChecker(repositoryEntity, commitIds));
		parameters.put("pullRequest", pullRequest);
		parameters.put("repository", repository);
		parameters.put("diffViewModel", diffBlameModel);
		parameters.put("builds", buildResults.findBuildResults(repositoryEntity, commitIds));

		List<LineWarning> lineWarnings = warnings.getLineWarningsFor(repositoryEntity, pullRequest.getDestination().getCommitId());
		parameters.put("lineWarnings", new WarningResolver(lineWarnings));

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-pull-diff-view.ftl", locales, parameters));
	}

	private static DiffBlameModel getDiffBlameModelForPull(PullRequest pullRequest, RepositoryApi repository) {
		String destinationId = pullRequest.getDestination().getCommitId();
		String mergeBaseId = pullRequest.getMergeBase().getCommitId();
		return repository.getCommit(destinationId).diffBlame(mergeBaseId);
	}

	@POST
	@Path("/pull/{pullId}/close")
	@Produces(MediaType.APPLICATION_JSON)
	public PullCloseResponse closePullRequest(@Context HttpServletRequest request,
	                                          @PathParam("pullId") long pullId) {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		PullRequest pullRequest = pullRequests.findById(repositoryEntity, pullId);
		pullRequest.setOpen(false);
		pullRequest.setClosed(new Date());
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
	                                         @PathParam("pullId") long pullId) {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);

		PullRequest pullRequest = pullRequests.findById(repositoryEntity, pullId);

		if (pullRequest.isOpen()) {
			throw new IllegalStateException("Cannot remove branch if pull request is still open");
		}

		DeleteBranchResponse response = new DeleteBranchResponse();
		String branchName = pullRequest.getBranchName();

		if (!pullRequests.openPullRequestExists(repositoryEntity, branchName)) {
			repositoryApi.getBranch(branchName).deleteBranch();
			log.debug("Deleted branch {}", pullRequest.getBranchName());
			response.setClosed(true);
		}

		return response;
	}

	@POST
	@Path("/pull/{pullId}/merge")
	@Produces(MediaType.APPLICATION_JSON)
	public MergeResponse mergePullRequest(@PathParam("pullId") long pullId) throws IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);

		PullRequest pullRequest = pullRequests.findById(repositoryEntity, pullId);
		pullRequestBackend.updatePullRequest(repositoryApi, pullRequest);

		BranchApi branchApi = repositoryApi.getBranch(pullRequest.getBranchName());
		BranchModel branch = branchApi.get();

		String message = String.format("Merge pull request #%s from %s", pullRequest.getIssueId(), branch.getSimpleName());
		MergeResponse response = branchApi.merge(message, currentUser.getName(), currentUser.getEmail());
		log.debug("Merged pull request {}", pullRequest);

		if (response.isSuccess()) {
			pullRequest.setOpen(false);
			pullRequest.setMerged(true);
			pullRequest.setClosed(new Date());
			pullRequests.merge(pullRequest);
			// Currently the git server fails to correctly trigger the push hook
			// Therefore, we invoke the githook manually
			// See: https://github.com/devhub-tud/devhub/issues/140
			hooksResource.onGitPush(new GitPush(repositoryEntity.getRepositoryName()));
		}

		return response;
	}

}
