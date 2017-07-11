package nl.tudelft.ewi.devhub.server.web.resources.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.SessionScoped;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.*;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LineWarning;
import nl.tudelft.ewi.devhub.server.events.CreateCommitEvent;
import nl.tudelft.ewi.devhub.server.util.FlattenFolderTree;
import nl.tudelft.ewi.devhub.server.util.Highlight;
import nl.tudelft.ewi.devhub.server.util.MarkDownParser;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.models.CommentResponse;
import nl.tudelft.ewi.devhub.server.web.resources.Resource;
import nl.tudelft.ewi.devhub.server.web.resources.views.WarningResolver;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.models.*;
import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.resteasy.annotations.cache.Cache;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

@Slf4j
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public abstract class AbstractProjectResource<RepoType extends RepositoryEntity> extends Resource {

	private static final int PAGE_SIZE = 25;
	private final static int MIN_GROUP_SIZE = 1;
	private final static int MAX_GROUP_SIZE = 20;
	public static final String MASTER_BRANCH_NAME = "master";

	@Data
	@SessionScoped
	public static class EditContributorsState {
		private final Map<String, Collection<User>> selectedUsers = Maps.newHashMap();
	}

	protected final TemplateEngine templateEngine;
	protected final User currentUser;
	protected final BuildResults buildResults;
	protected final PullRequests pullRequests;
	protected final CommentBackend commentBackend;
	protected final BuildsBackend buildBackend;
	protected final RepositoriesApi repositoriesApi;
	protected final CommitComments comments;
	protected final CommentMailer commentMailer;
	protected final Commits commits;
	protected final Warnings warnings;
	protected final Controller<? super RepoType> repositoriesController;
	protected final EditContributorsState editContributorsState;
	protected final Users users;
	protected MarkDownParser markDownParser;
	protected final EventBus asyncEventBus;

	/**
	 * Used to display different types of alerts on a branch view.
	 */
	public enum DeletionStatus {
		/**
		 * Deletion of a branch was successful.
		 */
		SUCCESS,
		/**
		 * Prompt the user for confirmation before deleting the branch. This is
		 * used on branches that are ahead of master to prevent accidental deletion. The
		 * confirmation requires the user to type in the branch name.
		 */
		CONFIRM,
		/**
		 * The user entered the confirmation wrong. Tell him and prompt
		 * again for confirmation.
		 */
		CONFIRM_AGAIN,
		/**
		 * Something has gone wrong wile attempting to delete the branch. Either
		 * one of the parameters did not get sent correctly, or the user attempted to remove the
		 * master branch.
		 */
		ERROR

	}

	protected AbstractProjectResource(final TemplateEngine templateEngine,
							final @Named("current.user") User currentUser,
							final CommentBackend commentBackend,
							final BuildResults buildResults,
							final PullRequests pullRequests,
							final RepositoriesApi repositoriesApi,
							final BuildsBackend buildBackend,
							final CommitComments comments,
							final CommentMailer commentMailer,
							final Commits commits,
							final Warnings warnings,
						  	final Controller<? super RepoType> repositoriesController,
						  	final EditContributorsState editContributorsState,
						  	final Users users,
						  	final MarkDownParser markDownParser,
	                        final EventBus asyncEventBus) {

		this.templateEngine = templateEngine;
		this.currentUser = currentUser;
		this.commentBackend = commentBackend;
		this.buildResults = buildResults;
		this.pullRequests = pullRequests;
		this.buildBackend = buildBackend;
		this.repositoriesApi = repositoriesApi;
		this.comments = comments;
		this.commentMailer = commentMailer;
		this.commits = commits;
		this.warnings = warnings;
		this.repositoriesController = repositoriesController;
		this.editContributorsState = editContributorsState;
		this.users = users;
		this.markDownParser = markDownParser;
		this.asyncEventBus = asyncEventBus;
	}

	protected abstract RepoType getRepositoryEntity();

	protected Map<String, Object> getBaseParameters() {
		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("repositoryEntity", getRepositoryEntity());
		return parameters;
	}

	/**
	 * Generates the parameters for the branch overview.
	 * @param branchName Name of the branch
	 * @param page Page number to display
     * @return A map containing the response parameters.
     */
	protected Map<String, Object> getBranchOverviewParameters(String branchName, int page) {
        RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());
		Map<String, Object> parameters = getBaseParameters();
		parameters.put("repository", repositoryApi.getRepositoryModel());

		try {
			BranchApi branchApi = repositoryApi.getBranch(branchName);
			BranchModel branch = branchApi.get();
			CommitSubList commits = branchApi.retrieveCommitsInBranch((page - 1) * PAGE_SIZE, PAGE_SIZE);

			Collection<String> commitIds = getCommitIds(commits);
			List<Commit> commitEntities = commitIds.stream()
				.map(commitId -> this.commits.ensureExists(repositoryEntity, commitId))
				.collect(Collectors.toList());
			Map<String, Commit> commitEntitiesByCommitId = Maps.uniqueIndex(commitEntities, Commit::getCommitId);

			parameters.put("commits", commits);
			parameters.put("branch", branch);
			parameters.put("commitEntities", commitEntitiesByCommitId);
			parameters.put("pagination", new Pagination(page, commits.getTotal()));

			parameters.put("warnings", warnings.commitsWithWarningsFor(repositoryEntity, commitIds));
			parameters.put("comments", comments.commentsFor(repositoryEntity, commitIds));
			parameters.put("builds", buildResults.findBuildResults(repositoryEntity, commitIds));

			pullRequests.findOpenPullRequest(repositoryEntity, branch.getName()).ifPresent(pullRequest ->
					parameters.put("pullRequest", pullRequest));
		}
		catch (NotFoundException e) {
			if (branchName.equals("master")) {
				// Swallow exception for master, so an overview page can be generated for bare empty repositories
				log.debug("Master branch is empty for {}", repositoryEntity);
			} else throw e;
		}

		return parameters;
	}

	public CommitComment commitCommentFactory(String message, RepositoryEntity repositoryEntity, String linkCommitId) {
		CommitComment comment = new CommitComment();
		comment.setContent(message);
		comment.setCommit(commits.ensureExists(repositoryEntity, linkCommitId));
		comment.setUser(currentUser);
		return comment;
	}

	@GET
	@Transactional
	public Response showProjectOverview(@Context HttpServletRequest request,
										@QueryParam("fatal") String fatal,
										@QueryParam("deletionStatus") DeletionStatus deletionStatus,
										@QueryParam("deletedBranch") String deletedBranch) throws IOException, ApiError {

		return showBranchOverview(request, MASTER_BRANCH_NAME, 1, fatal, deletionStatus, deletedBranch);
	}

	@GET
	@Cache(noStore = true)
	@Path("/branch/{branchName}")
	@Transactional
	public Response showBranchOverview(@Context HttpServletRequest request,
									   @PathParam("branchName") String branchName,
									   @QueryParam("page") @DefaultValue("1") int page,
									   @QueryParam("fatal") String fatal,
	                                   @QueryParam("deletionStatus") DeletionStatus deletionStatus,
	                                   @QueryParam("deletedBranch") String deletedBranch) throws IOException, ApiError {

		Map<String, Object> parameters = getBranchOverviewParameters(branchName, page);

		if (deletionStatus != null) {
			parameters.put("deletionStatus", deletionStatus);
			if (deletedBranch != null) {
				parameters.put("deletedBranch", deletedBranch);
			}
		}

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-view.ftl", locales, parameters));		
		
	}

	protected List<String> getCommitIds(CommitSubList commits) {
		return Lists.transform(commits.getCommits(), CommitModel::getCommit);
	}

	@GET
	@Path("/contributors")
	@Transactional
	public Response showContributors(@Context HttpServletRequest request) throws IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repository = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());

		Map<String, Object> parameters = getBaseParameters();
		parameters.put("repository", repository.getRepositoryModel());

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-contributors.ftl", locales, parameters));
	}

	@GET
	@Path("/insights")
	@Transactional
	public Response showInsights(@Context HttpServletRequest request) throws IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repository = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());

		Map<String, Object> parameters = getBaseParameters();
		parameters.put("repository", repository.getRepositoryModel());

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-commit-graph.ftl", locales, parameters));
	}

	@GET
	@Path("/contributors/edit")
	@Transactional
	public Response editContributors(@Context HttpServletRequest request,
									 @QueryParam("error") String error,
									 @QueryParam("step") @DefaultValue("1") int step) throws IOException, URISyntaxException {

		editContributorsAllowedCheck();

		if (step == 1) {
			return editContributorsStep1(request, error);
		}
		else if (step == 2) {
			return editContributorsStep2(request, error);
		}
		throw new BadRequestException("Invalid value for step");
	}

	private Response editContributorsStep1(@Context HttpServletRequest request,
										   @QueryParam("error") String error) throws IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		Collection<User> members = this.editContributorsState.getSelectedUsers()
			.computeIfAbsent(repositoryEntity.getRepositoryName(), c -> repositoryEntity.getCollaborators());

		Map<String, Object> parameters = getBaseParameters();
		if (members != null && !members.isEmpty()) {
			parameters.put("members", members);
		}
		parameters.put("minGroupSize", getMinGroupSize());
		parameters.put("maxGroupSize", getMaxGroupSize());
		if (!isNullOrEmpty(error)) {
			parameters.put("error", error);
		}

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-setup-2.ftl", locales, parameters));
	}

	private Response editContributorsStep2(@Context HttpServletRequest request,
										   @QueryParam("error") String error) throws IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		Collection<User> members = this.editContributorsState.getSelectedUsers().get(repositoryEntity.getRepositoryName());

		Map<String, Object> parameters = getBaseParameters();
		parameters.put("members", members);

		if (!isNullOrEmpty(error)) {
			parameters.put("error", error);
		}

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-setup-3.ftl", locales, parameters));
	}

	@POST
	@Path("/contributors/edit")
	public Response processEditContributors(@Context HttpServletRequest request,
											@QueryParam("step") @DefaultValue("1") int step) throws IOException, URISyntaxException {
		try {
			editContributorsAllowedCheck();

			RepositoryEntity repositoryEntity = getRepositoryEntity();

			if (step == 1) {
				Collection<User> groupMembers = getGroupMembers(request);
				validateCollaborators(groupMembers);
				this.editContributorsState.getSelectedUsers().put(repositoryEntity.getRepositoryName(), groupMembers);
				return redirect(new URI(request.getRequestURI()).resolve("edit?step=2"));
			}

			Collection<User> members = this.editContributorsState.getSelectedUsers().get(repositoryEntity.getRepositoryName());
			updateCollaborators(members);
			this.editContributorsState.getSelectedUsers().remove(repositoryEntity.getRepositoryName());
			return redirect(new URI(request.getRequestURI()).resolve("../contributors"));
		}
		catch (ApiError e) {
			return redirect(new URI(request.getRequestURI()).resolve("edit?step=1&error=" + e.getResourceKey()));
		}
	}

    @POST
    @Transactional
    @Path("/comment")
    @ValidateRequest
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public CommentResponse commentOnPull(@Context HttpServletRequest request,
										 @NotEmpty @FormParam("link-commit") String linkCommitId,
										 @NotEmpty @FormParam("content") String message,
										 @NotEmpty @FormParam("redirect") String redirect,
										 @FormParam("source-commit") String sourceCommitId,
										 @FormParam("source-line-number") Integer sourceLineNumber,
										 @FormParam("source-file-name") String sourceFileName)
		throws IOException, ApiError {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		CommitComment comment = commitCommentFactory(message, repositoryEntity, linkCommitId);

		if(sourceCommitId != null) {
			// In-line comment
			Source source = new Source();
			source.setSourceCommit(commits.ensureExists(repositoryEntity, sourceCommitId));
			source.setSourceFilePath(sourceFileName);
			source.setSourceLineNumber(sourceLineNumber);
			comment.setSource(source);
		}

		commentBackend.post(comment);
		commentMailer.sendCommentMail(comment, redirect);

		CommentResponse response = new CommentResponse();
		response.setContent(message);
		response.setDate(comment.getTimestamp().toString());
		response.setName(currentUser.getName());
		response.setCommentId(comment.getCommentId());
		response.setFormattedContent(markDownParser.markdownToHtml(message));

		return response;
    }

	@GET
	@Path("/commits/{commitId}")
	public Response showCommitOverview(@Context HttpServletRequest request) {
		return redirect(URI.create(request.getRequestURI() + "/").resolve("diff"));
	}

	@GET
	@Path("/commits/{commitId}/build")
	@Transactional
	public Response showCommitBuild(@Context HttpServletRequest request,
									@PathParam("commitId") String commitId) throws IOException, ApiError {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repository = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());
		CommitApi commit = repository.getCommit(commitId);

		Map<String, Object> parameters = getBaseParameters();
		parameters.put("commit", commit.get());
		parameters.put("repository", repository.getRepositoryModel());

		try {
			parameters.put("buildResult", buildResults.find(repositoryEntity, commitId));
		}
		catch (EntityNotFoundException e) {
			log.debug("No build result for commit {}", commitId);
		}

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-commit-view.ftl", locales, parameters));
	}

    @GET
    @Path("/commits/{commitId}/rebuild")
    @Transactional
    public Response rebuildCommit(@Context HttpServletRequest request,
                                  @PathParam("commitId") String commitId)
			throws URISyntaxException, UnsupportedEncodingException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		nl.tudelft.ewi.devhub.server.database.entities.Commit commit = commits.ensureExists(repositoryEntity, commitId);
		buildBackend.rebuildCommit(commit);
        URI responseUri = new URI(request.getRequestURI()).resolve("./diff");
        return Response.seeOther(responseUri).build();
    }

	@GET
	@Path("/commits/{commitId}/diff")
	@Transactional
	public Response showCommitChanges(@Context HttpServletRequest request,
									  @PathParam("commitId") String commitId)
			throws IOException, ApiError {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repository = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());
		CommitApi commitApi = repository.getCommit(commitId);
		CommitModel commit = commitApi.get();
		DiffBlameModel diffBlameModel = commitApi.diffBlame();


		Map<String, Object> parameters = getBaseParameters();
		parameters.put("commit", commit);
		parameters.put("repository", repository.getRepositoryModel());
		parameters.put("diffViewModel", diffBlameModel);
		parameters.put("comments", comments.getCommentsFor(repositoryEntity, commitId));
		parameters.put("commentChecker", commentBackend.getCommentChecker(repositoryEntity, Lists.newArrayList(commitId)));

		try {
			parameters.put("buildResult", buildResults.find(repositoryEntity, commitId));
		}
		catch (EntityNotFoundException e) {
			log.debug("No build result for commit {}", commitId);
		}

		List<LineWarning> lineWarnings = warnings.getLineWarningsFor(repositoryEntity, commitId);
		parameters.put("warnings", warnings.getWarningsFor(repositoryEntity, commitId));
		parameters.put("lineWarnings", new WarningResolver(lineWarnings));

    		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-diff-view.ftl", locales, parameters));
	}

    @GET
	@Path("/commits/{commitId}/tree")
	@Transactional
	public Response getTree(@Context HttpServletRequest request,
							@PathParam("commitId") String commitId)
					throws ApiError, IOException {
		return getTree(request, commitId, CommitApi.EMPTY_PATH);
	}

	public static Comparator<String> FOLDER_TREE_COMPARATOR = (o1, o2) -> {
		if (o1.endsWith("/") && o2.endsWith("/")) {
			return o1.compareTo(o2);
		}
		else if (!o1.endsWith("/") && !o2.endsWith("/")) {
			return o1.compareTo(o2);
		}
		else if (o1.endsWith("/")) {
			return -1;
		}
		return 1;
	};

	@GET
	@Path("/commits/{commitId}/tree/{path:.+}")
	@Transactional
	public Response getTree(@Context HttpServletRequest request,
							@PathParam("commitId") String commitId,
							@PathParam("path") String path) throws ApiError, IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repository = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());
		Map<String, EntryType> entries = new TreeMap<>(FOLDER_TREE_COMPARATOR);

		CommitApi commitApi = repository.getCommit(commitId);
		CommitModel commit = commitApi.get();
		entries.putAll(new FlattenFolderTree(commitApi).resolveEntries(path));
		
		Map<String, Object> parameters = getBaseParameters();
		parameters.put("commit", commit);
		parameters.put("path", path);
		parameters.put("repository", repository.getRepositoryModel());
		parameters.put("entries", entries);

		try {
			parameters.put("buildResult", buildResults.find(repositoryEntity, commitId));
		}
		catch (EntityNotFoundException e) {
			log.debug("No build result for commit {}", commitId);
		}
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-folder-view.ftl", locales, parameters));
	}




	@GET
	@Path("/commits/{commitId}/raw/{path:.+}")
	@Transactional
	public Response getRawFile(@Context HttpServletRequest request,
							@PathParam("commitId") String commitId,
							@PathParam("path") String path) throws ApiError, IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repository = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());
		return Response.ok(repository.getCommit(commitId).showFile(path))
				.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
				.build();
	}

	@GET
	@Path("/commits/{commitId}/blob/{path:.+}")
	@Transactional
	public Response getBlob(@Context HttpServletRequest request,
                            @PathParam("commitId") String commitId,
                            @PathParam("path") String path) throws ApiError, IOException {

		String folderPath = "";
		String fileName = path;
		if (path.contains("/")) {
			folderPath = path.substring(0, path.lastIndexOf('/'));
			fileName = path.substring(path.lastIndexOf('/') + 1);
		}

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repository = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());
		CommitApi commitApi = repository.getCommit(commitId);
		CommitModel commit = commitApi.get();
		Map<String, EntryType> entries = commitApi.showTree(folderPath);

		EntryType type = entries.get(fileName);

		if (type == EntryType.BINARY) {
			return Response.ok(commitApi.showFile(path))
					.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
					.build();
		}

		String contents = commitApi.showTextFile(path);
		BlameModel blame = commitApi.blame(path);

		Map<String, Object> parameters  = getBaseParameters();
		parameters.put("commit", commit);
		parameters.put("blame", blame);
		parameters.put("path", path);
		parameters.put("contents", contents);
		parameters.put("highlight", Highlight.forFileName(path));
		parameters.put("repository", repository.getRepositoryModel());

		try {
			parameters.put("buildResult", buildResults.find(repositoryEntity, commitId));
		}
		catch (EntityNotFoundException e) {
			log.debug("No build result for commit {}", commitId);
		}

		Set<String> blameCommits = getCommitsForBlame(blame);
        parameters.put("comments", commentBackend.getCommentChecker(repositoryEntity, blameCommits));
		List<LineWarning> lineWarnings = warnings.getLineWarningsFor(repositoryEntity, commitId);
		parameters.put("lineWarnings", new WarningResolver(lineWarnings));

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-file-view.ftl", locales, parameters));
	}

	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Transactional
	public void deleteRepository() {
		RepoType repositoryEntity = getRepositoryEntity();
		log.info("Removing {} from git-server", repositoryEntity.getRepositoryName());
		repositoriesApi.getRepository(repositoryEntity.getRepositoryName()).deleteRepository();
		log.info("Removing {}", repositoryEntity);
		repositoriesController.delete(repositoryEntity);
	}

	@GET
	@Path("/settings")
	@Transactional
	public Response showSettings(@Context HttpServletRequest request) throws IOException, ApiError {
		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repository = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());

		Map<String, Object> parameters = getBaseParameters();
		parameters.put("repository", repository.getRepositoryModel());
		parameters.put("buildInstruction", repositoriesController.unproxy(repositoryEntity.getBuildInstruction()));

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project/settings.ftl", locales, parameters));
	}

	@POST
	@Path("/branch/delete")
	@Transactional
	public Response deleteBranch(@FormParam("branchName") @NotEmpty String branchName,
                                 @FormParam("branchNameConf") String branchNameConf,
                                 @QueryParam("fatal") String fatal)
			throws IOException, ApiError {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());

		BranchApi branchApi = repositoryApi.getBranch(branchName);
		BranchModel branchModel = branchApi.get();

		if (branchName.equals("refs/heads/master")) {
			return Response.seeOther(UriBuilder.fromUri(repositoryEntity.getURI().resolve("branch/").resolve(URLEncoder.encode(branchName)))
				.queryParam("deletionStatus", DeletionStatus.ERROR)
				.queryParam("deletedBranch", branchModel.getSimpleName()).build()).build();
		}



		if (branchModel.isAhead()) {
			if (isNullOrEmpty(branchNameConf)) {
				return Response.seeOther(UriBuilder.fromUri(repositoryEntity.getURI().resolve("branch/").resolve(URLEncoder.encode(branchName)))
					.queryParam("deletionStatus", DeletionStatus.CONFIRM)
					.queryParam("deletedBranch", branchModel.getSimpleName()).build()).build();
			}
			else if (! branchNameConf.equals(branchModel.getSimpleName())) {
				return Response.seeOther(UriBuilder.fromUri(repositoryEntity.getURI().resolve("branch/").resolve(URLEncoder.encode(branchName)))
					.queryParam("deletionStatus", DeletionStatus.CONFIRM_AGAIN)
					.queryParam("deletedBranch", branchModel.getSimpleName()).build()).build();
			}
		}

		branchApi.deleteBranch();

		return Response.seeOther(UriBuilder.fromUri(repositoryEntity.getURI().resolve("branch/").resolve(MASTER_BRANCH_NAME))
			.queryParam("deletionStatus", DeletionStatus.SUCCESS)
			.queryParam("deletedBranch", branchModel.getSimpleName()).build()).build();
	}

	@GET
    @Path("/branches/delete")
    @Transactional
    public Response deleteBehindBranchPageReload(@Context HttpServletRequest request) throws URISyntaxException {
        return Response.seeOther(new URI("/courses")).build();
    }

	/**
	 * Security check for updating the collaborators.
	 * @see AbstractProjectResource#editContributors(HttpServletRequest, String, int)
	 */
	protected void editContributorsAllowedCheck() throws ForbiddenException {};

	/**
	 * Validation hook for updating the collaborators.
	 * @see AbstractProjectResource#editContributors(HttpServletRequest, String, int)
	 */
	protected void validateCollaborators(Collection<User> groupMembers) throws ApiError {}

	/**
	 * Update hook for updating the collaborators.
	 * @see AbstractProjectResource#editContributors(HttpServletRequest, String, int)
	 */
	protected abstract void updateCollaborators(Collection<User> members);

	/**
	 * The maximal number of collaborators.
	 * @return maximal number of collaborators.
	 * @see AbstractProjectResource#editContributors(HttpServletRequest, String, int)
	 */
	protected int getMaxGroupSize() {
		return MAX_GROUP_SIZE;
	}

	/**
	 * The minimal number of collaborators.
	 * @return minimal number of collaborators.
	 * @see AbstractProjectResource#editContributors(HttpServletRequest, String, int)
	 */
	protected int getMinGroupSize() {
		return MIN_GROUP_SIZE;
	}

	private Collection<User> getGroupMembers(HttpServletRequest request) {
		String netId;
		int memberId = 1;
		Set<String> netIds = Sets.newHashSet();
		while (!isNullOrEmpty((netId = request.getParameter("member-" + memberId)))) {
			memberId++;
			netIds.add(netId);
		}

		Map<String, User> members = users.mapByNetIds(netIds);
		return members.values();
	}

	private Set<String> getCommitsForBlame(BlameModel blame) {
		return blame.getBlames().stream()
			.map(BlameModel.BlameBlock::getFromCommitId)
			.collect(Collectors.toSet());
	}
	
	
	@Data
	static public class Pagination {
				
		private final int page, total;
		
		public int getPageCount() {
			return (total + PAGE_SIZE - 1) / PAGE_SIZE;
		}
		
	}

	@POST
	@Deprecated
	@Transactional
	@Path("enhance-commits")
	@Consumes(MediaType.MEDIA_TYPE_WILDCARD)
	public void enhanceCommitsForRepository() {
		RepositoryEntity repositoryEntity = getRepositoryEntity();
		repositoryEntity.getCommits().stream()
			.map(commit -> {
				CreateCommitEvent createCommitEvent = new CreateCommitEvent();
				createCommitEvent.setRepositoryName(repositoryEntity.getRepositoryName());
				createCommitEvent.setCommitId(commit.getCommitId());
				return createCommitEvent;
			})
			.forEach(asyncEventBus::post);
	}


	/**
	 * Used for showing multiple graphs
	 * @return a list with all commits made
	 */
	private List<Commit> getAllCommits() {

		// get all commits
		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());
		Map<String, Object> parameters = getBaseParameters();
		parameters.put("repository", repositoryApi.getRepositoryModel());
		BranchApi branchApi = repositoryApi.getBranch("master");
		BranchModel branch = branchApi.get();
		CommitSubList commits = branchApi.retrieveCommitsInBranch();
		Collection<String> commitIds = getCommitIds(commits);
		List<Commit> commitEntities = commitIds.stream()
			.map(commitId -> this.commits.ensureExists(repositoryEntity, commitId))
			.collect(Collectors.toList());

		return commitEntities;
	}

	@GET
	@Path("magical-chart-data")
	@Produces(MediaType.APPLICATION_JSON)

	public List<List<Object>> getMagicalChartData() {

		List<Commit> commitEntities = getAllCommits();

		return getGoogleChartDataForCommits(commitEntities);
	}

	private static List<List<Object>> getGoogleChartDataForCommits(List<Commit> commitEntities) {

		Map<LocalDate, List<Commit>> commitsGroupedByDate = commitEntities.stream()
			.collect(Collectors.groupingBy((Commit commit) ->
				commit.getCommitTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));

		Map<LocalDate, Integer> numberOfCommitsByDate = Maps.transformValues(commitsGroupedByDate, List::size);

		SortedMap<LocalDate, Integer> numberOfCommitsByDateInOrder = new TreeMap<>(numberOfCommitsByDate);

		List<List<Object>> datesOrdered = numberOfCommitsByDateInOrder.entrySet().stream()
			.map((Map.Entry<LocalDate, Integer> entry) ->
				ImmutableList.<Object> of(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());

		ImmutableList.Builder<List<Object>> listBuilder = ImmutableList.builder();
		listBuilder.add(ImmutableList.of("Date", "Number of commits"));
		listBuilder.addAll(datesOrdered);
		return listBuilder.build();
	}

	@GET
	@Path("person-commit")
	@Produces(MediaType.APPLICATION_JSON)

	public Map<String, List<List<Object>>> getPersonCommit() {

		List<Commit> commitEntities = getAllCommits();

		Map<String, List<Commit>> commitsGroupedByAuthor = commitEntities.stream()
			.collect(Collectors.groupingBy((Commit commit) -> commit.getAuthor()));

		Map<String, List<List<Object>>> commitsGroupedByAuthorByDates =
			Maps.transformValues(commitsGroupedByAuthor, AbstractProjectResource::getGoogleChartDataForCommits);

		return commitsGroupedByAuthorByDates;

	}
}
