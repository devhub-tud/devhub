package nl.tudelft.ewi.devhub.server.web.resources.repository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LineWarning;
import nl.tudelft.ewi.devhub.server.util.Highlight;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.models.CommentResponse;
import nl.tudelft.ewi.devhub.server.web.resources.Resource;
import nl.tudelft.ewi.devhub.server.web.resources.views.WarningResolver;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.models.BlameModel;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CommitSubList;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.EntryType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;

import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public abstract class AbstractProjectResource extends Resource {

	private static final int PAGE_SIZE = 25;

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
							final Warnings warnings) {

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
	}

	protected abstract RepositoryEntity getRepositoryEntity();

	protected Map<String, Object> getBaseParameters() {
		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("repositoryEntity", getRepositoryEntity());
		return parameters;
	}

	@GET
	@Transactional
	public Response showProjectOverview(@Context HttpServletRequest request,
										@QueryParam("fatal") String fatal) throws IOException, ApiError {

		return showBranchOverview(request, "master", 1, fatal);
	}

	@GET
	@Path("/branch/{branchName}")
	@Transactional
	public Response showBranchOverview(@Context HttpServletRequest request,
									   @PathParam("branchName") String branchName,
									   @QueryParam("page") @DefaultValue("1") int page,
									   @QueryParam("fatal") String fatal) throws IOException, ApiError {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repository = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());

		Map<String, Object> parameters = getBaseParameters();
		parameters.put("repository", repository.getRepositoryModel());

		try {
			BranchApi branchApi = repository.getBranch(branchName);
			BranchModel branch = branchApi.get();
			CommitSubList commits = branchApi.retrieveCommitsInBranch((page - 1) * PAGE_SIZE, PAGE_SIZE);

			parameters.put("commits", commits);
			parameters.put("branch", branch);
			parameters.put("pagination", new Pagination(page, commits.getTotal()));

			Collection<String> commitIds = getCommitIds(commits);
			parameters.put("warnings", warnings.commitsWithWarningsFor(repositoryEntity, commitIds));
			parameters.put("comments", comments.commentsFor(repositoryEntity, commitIds));
			parameters.put("builds", buildResults.findBuildResults(repositoryEntity, commitIds));

			PullRequest pullRequest = pullRequests.findOpenPullRequest(repositoryEntity, branch.getName());
			if(pullRequest != null) {
				parameters.put("pullRequest", pullRequest);
			}
		}
		catch (NotFoundException e) {}

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-view.ftl", locales, parameters));
	}

	protected List<String> getCommitIds(CommitSubList commits) {
		return commits.getCommits().stream()
				.map(CommitModel::getCommit)
				.collect(Collectors.toList());
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
		CommitComment comment = new CommitComment();
		comment.setContent(message);
		comment.setCommit(commits.ensureExists(repositoryEntity, linkCommitId));
		comment.setUser(currentUser);

		if(sourceCommitId != null) {
			// In-line comment
			Source source = new Source();
			source.setSourceCommit(commits.ensureExists(repositoryEntity, sourceCommitId));
			source.setSourceFilePath(sourceFileName);
			source.setSourceLineNumber(sourceLineNumber);
			comment.setSource(source);
		}

		comments.persist(comment);
		commentMailer.sendCommentMail(comment, redirect);

		CommentResponse response = new CommentResponse();
		response.setContent(message);
		response.setDate(comment.getTimestamp().toString());
		response.setName(currentUser.getName());
		response.setCommentId(comment.getCommentId());

		return response;
    }

	@GET
	@Path("/commits/{commitId}")
	public Response showCommitOverview(@Context HttpServletRequest request) {
		return redirect(request.getPathInfo() + "/diff");
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
		return getTree(request, commitId, "");
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
		entries.putAll(commitApi.showTree(path));
		
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

		String[] contents = commitApi.showTextFile(path).split("\\r?\\n");
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
		List<LineWarning> lineWarnings = warnings.getLineWarningsFor(repositoryEntity, blameCommits);
		parameters.put("lineWarnings", new WarningResolver(lineWarnings));

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-file-view.ftl", locales, parameters));
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
	
}