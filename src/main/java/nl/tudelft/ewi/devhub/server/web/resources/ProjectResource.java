package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.build.jaxrs.models.BuildRequest;
import nl.tudelft.ewi.build.jaxrs.models.GitSource;
import nl.tudelft.ewi.build.jaxrs.models.MavenBuildInstruction;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LineWarning;
import nl.tudelft.ewi.devhub.server.util.CommitChecker;
import nl.tudelft.ewi.devhub.server.util.Highlight;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.models.CommentResponse;
import nl.tudelft.ewi.devhub.server.web.resources.views.WarningResolver;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.client.Branch;
import nl.tudelft.ewi.git.client.Commit;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.BlameModel;
import nl.tudelft.ewi.git.models.CommitSubList;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.EntryType;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;

import javax.inject.Inject;
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
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@RequestScoped
@Path("courses/{courseCode}/groups/{groupNumber}")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class ProjectResource extends Resource {
	
	private static final int PAGE_SIZE = 25;

	private final TemplateEngine templateEngine;
	private final User currentUser;
	private final BuildResults buildResults;
	private final Group group;
	private final PullRequests pullRequests;
	private final CommentBackend commentBackend;
    private final GitServerClient client;
    private final BuildsBackend buildBackend;
    private final Config config;
	private final GitServerClient gitClient;
	private final CommitComments comments;
	private final CommentMailer commentMailer;
	private final Commits commits;
	private final Warnings warnings;

	@Inject
	ProjectResource(final TemplateEngine templateEngine,
			final @Named("current.user") User currentUser,
			final @Named("current.group") Group group,
			final CommentBackend commentBackend,
			final BuildResults buildResults,
			final PullRequests pullRequests,
            final GitServerClient client,
            final BuildsBackend buildBackend,
			final GitServerClient gitClient,
			final CommitComments comments,
			final CommentMailer commentMailer,
			final Commits commits,
            final Config config,
			final Warnings warnings) {

		this.templateEngine = templateEngine;
		this.group = group;
		this.currentUser = currentUser;
		this.commentBackend = commentBackend;
		this.buildResults = buildResults;
		this.pullRequests = pullRequests;
        this.client = client;
        this.buildBackend = buildBackend;
		this.gitClient = gitClient;
		this.comments = comments;
		this.commentMailer = commentMailer;
		this.commits = commits;
        this.config = config;
		this.warnings = warnings;
	}

	@GET
	@Transactional
	public Response showProjectOverview(@Context HttpServletRequest request,
										@QueryParam("fatal") String fatal) throws IOException, ApiError, GitClientException {

		return showBranchOverview(request, "master", 1, fatal);
	}

	@GET
	@Path("/branch/{branchName}")
	@Transactional
	public Response showBranchOverview(@Context HttpServletRequest request,
									   @PathParam("branchName") String branchName,
									   @QueryParam("page") @DefaultValue("1") int page,
									   @QueryParam("fatal") String fatal) throws IOException, ApiError, GitClientException {

		Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("group", group);
		parameters.put("warnings", warnings.commitsWithWarningsFor(group));
		parameters.put("states", new CommitChecker(group, buildResults));
		parameters.put("comments", new HasCommentsChecker());
		parameters.put("repository", repository);

		try {
			Branch branch = repository.retrieveBranch(branchName);
			CommitSubList commits = branch.retrieveCommits((page - 1) * PAGE_SIZE, PAGE_SIZE);
			parameters.put("commits", commits);
			parameters.put("branch", branch);
			parameters.put("pagination", new Pagination(page, commits.getTotal()));

			PullRequest pullRequest = pullRequests.findOpenPullRequest(group, branch.getName());
			if(pullRequest != null) {
				parameters.put("pullRequest", pullRequest);
			}
		}
		catch (NotFoundException e) {}

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-view.ftl", locales, parameters));
	}

	@GET
	@Path("/contributors")
	@Transactional
	public Response showContributors(@Context HttpServletRequest request) throws IOException, GitClientException {

		Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("group", group);
		parameters.put("repository", repository);

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

		CommitComment comment = new CommitComment();
		comment.setContent(message);
		comment.setCommit(commits.ensureExists(group, linkCommitId));
		comment.setTime(new Date());
		comment.setUser(currentUser);

		if(sourceCommitId != null) {
			// In-line comment
			Source source = new Source();
			source.setSourceCommit(commits.ensureExists(group, sourceCommitId));
			source.setSourceFilePath(sourceFileName);
			source.setSourceLineNumber(sourceLineNumber);
			comment.setSource(source);
		}

		comments.persist(comment);
		commentMailer.sendCommentMail(comment, redirect);

		CommentResponse response = new CommentResponse();
		response.setContent(message);
		response.setDate(comment.getTime().toString());
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
									@PathParam("courseCode") String courseCode,
									@PathParam("groupNumber") String groupNumber,
									@PathParam("commitId") String commitId,
									@QueryParam("fatal") String fatal) throws IOException, ApiError, GitClientException {

		Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());
		Commit commit = repository.retrieveCommit(commitId);

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("group", group);
		parameters.put("commit", commit);
		parameters.put("states", new CommitChecker(group, buildResults));
		parameters.put("repository", repository);

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-commit-view.ftl", locales, parameters));
	}

    @GET
    @Path("/commits/{commitId}/rebuild")
    @Transactional
    public Response rebuildCommit(@Context HttpServletRequest request,
                                  @PathParam("courseCode") String courseCode,
                                  @PathParam("groupNumber") String groupNumber,
                                  @PathParam("commitId") String commitId)
			throws URISyntaxException, UnsupportedEncodingException, GitClientException {

        BuildResult buildResult;

        try {
            buildResult = buildResults.find(group, commitId);

            if(buildResult.getSuccess() == null) {
                // There is a build queued
                URI responseUri = new URI(request.getRequestURI()).resolve("./diff");
                return Response.seeOther(responseUri).build();
            }
            else {
                buildResult.setSuccess(null);
                buildResult.setLog(null);
                buildResults.merge(buildResult);
            }
        }
        catch (EntityNotFoundException e) {
            buildResult = BuildResult.newBuildResult(group, commitId);
            buildResults.persist(buildResult);
        }

        MavenBuildInstruction instruction = new MavenBuildInstruction();
        instruction.setWithDisplay(true);
        instruction.setPhases(new String[] { "package" });

        Repositories repositories = client.repositories();
		Repository repository = repositories.retrieve(group.getRepositoryName());

        StringBuilder callbackBuilder = new StringBuilder();
        callbackBuilder.append(config.getHttpUrl());
        callbackBuilder.append("/hooks/build-result");
        callbackBuilder.append("?repository=" + URLEncoder.encode(repository.getName(), "UTF-8"));
        callbackBuilder.append("&commit=" + URLEncoder.encode(commitId, "UTF-8"));

        GitSource source = new GitSource();
        source.setRepositoryUrl(repository.getUrl());
        source.setCommitId(commitId);

        BuildRequest buildRequest = new BuildRequest();
        buildRequest.setCallbackUrl(callbackBuilder.toString());
        buildRequest.setInstruction(instruction);
        buildRequest.setSource(source);
        buildRequest.setTimeout(group.getBuildTimeout());

        buildBackend.offerBuild(buildRequest);
        URI responseUri = new URI(request.getRequestURI()).resolve("./diff");
        return Response.seeOther(responseUri).build();
    }

	@GET
	@Path("/commits/{commitId}/diff")
	@Transactional
	public Response showCommitChanges(@Context HttpServletRequest request,
									  @PathParam("commitId") String commitId)
			throws IOException, ApiError, GitClientException {

		Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());
		nl.tudelft.ewi.git.client.Commit commit = repository.retrieveCommit(commitId);
		DiffBlameModel diffBlameModel = commit.diffBlame();

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("group", group);
		parameters.put("commit", commit);
		parameters.put("repository", repository);
		parameters.put("diffViewModel", diffBlameModel);
		parameters.put("comments", comments.getCommentsFor(group, commitId));
		parameters.put("commentChecker", commentBackend.getCommentChecker(Lists.newArrayList(commitId)));
		parameters.put("states", new CommitChecker(group, buildResults));

		List<LineWarning> lineWarnings = warnings.getLineWarningsFor(group, commitId);
		parameters.put("warnings", warnings.getWarningsFor(group, commitId));
		parameters.put("lineWarnings", new WarningResolver(lineWarnings));

    		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-diff-view.ftl", locales, parameters));
	}

    @GET
	@Path("/commits/{commitId}/tree")
	@Transactional
	public Response getTree(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("commitId") String commitId)
					throws ApiError, IOException, GitClientException {
		return getTree(request, courseCode, groupNumber, commitId, "");
	}
	
	@GET
	@Path("/commits/{commitId}/tree/{path:.+}")
	@Transactional
	public Response getTree(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("commitId") String commitId,
			@PathParam("path") String path) throws ApiError, IOException, GitClientException {

		Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());
		Map<String, EntryType> entries = new TreeMap<>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
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
			}
			
		});

		entries.putAll(repository.listDirectoryEntries(commitId, path));
		
		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("commit", repository.retrieveCommit(commitId));
		parameters.put("path", path);
		parameters.put("group", group);
		parameters.put("repository", repository);
		parameters.put("entries", entries);
		parameters.put("states", new CommitChecker(group, buildResults));
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-folder-view.ftl", locales, parameters));
	}

	@GET
	@Path("/commits/{commitId}/raw/{path:.+}")
	@Transactional
	public Response getRawFile(@Context HttpServletRequest request,
							@PathParam("courseCode") String courseCode,
							@PathParam("groupNumber") long groupNumber,
							@PathParam("commitId") String commitId,
							@PathParam("path") String path) throws ApiError, IOException, GitClientException {

		Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());
		return Response.ok(repository.showBinFile(commitId, path))
				.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
				.build();
	}

	@GET
	@Path("/commits/{commitId}/blob/{path:.+}")
	@Transactional
	public Response getBlob(@Context HttpServletRequest request,
                            @PathParam("courseCode") String courseCode,
                            @PathParam("groupNumber") long groupNumber,
                            @PathParam("commitId") String commitId,
                            @PathParam("path") String path) throws ApiError, IOException, GitClientException {

		String folderPath = "";
		String fileName = path;
		if (path.contains("/")) {
			folderPath = path.substring(0, path.lastIndexOf('/'));
			fileName = path.substring(path.lastIndexOf('/') + 1);
		}

		Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());
		Commit commit = repository.retrieveCommit(commitId);
		Map<String, EntryType> entries = repository.listDirectoryEntries(commitId, folderPath);

		EntryType type = entries.get(fileName);

		if (type == EntryType.BINARY) {
			return Response.ok(repository.showBinFile(commitId, path))
					.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
					.build();
		}

		String[] contents = repository.showFile(commitId, path).split("\\r?\\n");
        BlameModel blame = commit.blame(path);

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("commit", commit);
        parameters.put("blame", blame);
		parameters.put("path", path);
		parameters.put("contents", contents);
		parameters.put("highlight", Highlight.forFileName(path));
		parameters.put("group", group);
		parameters.put("repository", repository);
		parameters.put("states", new CommitChecker(group, buildResults));

		List<String> blameCommits = getCommitsForBlame(blame);
        parameters.put("comments", commentBackend.getCommentChecker(blameCommits));
		List<LineWarning> lineWarnings = warnings.getLineWarningsFor(group, blameCommits);
		parameters.put("lineWarnings", new WarningResolver(lineWarnings));

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-file-view.ftl", locales, parameters));
	}

	private List<String> getCommitsForBlame(BlameModel blame) {
		return blame.getBlames().stream()
			.map(BlameModel.BlameBlock::getFromCommitId)
				.distinct()
				.collect(Collectors.toList());
	}

	@Data
	public class HasCommentsChecker {

		/**
		 * Check how many comments there are for a commitId
		 * @param commitId the commitId
		 * @return the amount of commits
		 */
		public long amountOfCommits(String commitId) {
			return comments.amountOfComments(group, commitId);
		}

	}
	
	@Data
	static public class Pagination {
				
		private final int page, total;
		
		public int getPageCount() {
			return (total + PAGE_SIZE - 1) / PAGE_SIZE;
		}
		
	}
	
}
