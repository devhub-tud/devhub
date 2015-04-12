package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.build.jaxrs.models.BuildRequest;
import nl.tudelft.ewi.build.jaxrs.models.GitSource;
import nl.tudelft.ewi.build.jaxrs.models.MavenBuildInstruction;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.PullRequestBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.util.Highlight;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.client.*;
import nl.tudelft.ewi.git.models.*;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

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
	private final PullRequestBackend pullRequestBackend;
    private final Config config;
	private final GitServerClient gitClient;
	private final CommitComments comments;

	@Inject
	ProjectResource(final TemplateEngine templateEngine,
			final @Named("current.user") User currentUser,
			final @Named("current.group") Group group,
			final CommentBackend commentBackend,
			final BuildResults buildResults,
			final PullRequests pullRequests,
            final GitServerClient client,
            final BuildsBackend buildBackend,
			final PullRequestBackend pullRequestBackend,
			final GitServerClient gitClient,
			final CommitComments comments,
            final Config config) {

		this.templateEngine = templateEngine;
		this.group = group;
		this.currentUser = currentUser;
		this.commentBackend = commentBackend;
		this.buildResults = buildResults;
		this.pullRequests = pullRequests;
        this.client = client;
        this.buildBackend = buildBackend;
		this.pullRequestBackend = pullRequestBackend;
		this.gitClient = gitClient;
		this.comments = comments;
        this.config = config;
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
		Branch branch = repository.retrieveBranch(branchName);
		CommitSubList commits = branch.retrieveCommits((page - 1) * PAGE_SIZE, PAGE_SIZE);

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("group", group);
		parameters.put("states", new CommitChecker(group, buildResults));
		parameters.put("comments", new HasCommentsChecker(comments));
		parameters.put("repository", repository);
		parameters.put("commits", commits);
		parameters.put("branch", branch);
		parameters.put("pagination", new Pagination(page, commits.getTotal()));

		PullRequest pullRequest = pullRequests.findOpenPullRequest(group, branch.getName());
		if(pullRequest != null) {
			parameters.put("pullRequest", pullRequest);
		}

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-view.ftl", locales, parameters));
	}
	
	@POST
	@Path("/pull")
	@Transactional
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createPullRequest(@Context HttpServletRequest request,
			@FormParam("branchName") String branchName) {
		
		Preconditions.checkNotNull(branchName);
		
		if(pullRequests.findOpenPullRequest(group, branchName) != null) {
			throw new IllegalArgumentException("There already is an open pull request for " + branchName);
		}
		
		PullRequest pullRequest = new PullRequest();
		pullRequest.setBranchName(branchName);
		pullRequest.setGroup(group);
		pullRequest.setOpen(true);
		pullRequests.persist(pullRequest);
		
		String uri = request.getRequestURI() + "/" + pullRequest.getIssueId();
		return Response.seeOther(URI.create(uri)).build();
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Pull {
		private PullRequest pullRequest;
		private Branch branchModel;
	}

    @GET
    @Transactional
    @Path("/pulls")
    public Response getPullRequests(@Context HttpServletRequest request) throws IOException, GitClientException {
		Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());

		List<PullRequest> openPullRequests = pullRequests.findOpenPullRequests(group);
		List<Pull> pulls = Lists.newArrayListWithCapacity(openPullRequests.size());

		for(PullRequest pull : openPullRequests) {
			Branch branch = repository.retrieveBranch(pull.getBranchName());

			if(branch.getAhead() == 0) {
				pull.setOpen(false);
				pullRequests.merge(pull);
			}
			else {
				pulls.add(new Pull(pull, branch));
			}
		}

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("group", group);
		parameters.put("course", group.getCourse());
		parameters.put("repository", repository);
		parameters.put("pulls", pulls);
		parameters.put("commitChecker", new CommitChecker(group, buildResults));

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("courses/assignments/group-pulls.ftl", locales, parameters));
    }
	
	@GET
	@Transactional
	@Path("/pull/{pullId}")
	public Response getPullRequest(@Context HttpServletRequest request,
								   @PathParam("pullId") long pullId) throws ApiError, IOException, GitClientException {

		PullRequest pullRequest = pullRequests.findById(group, pullId);
		Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());
		nl.tudelft.ewi.git.client.Branch branch = repository.retrieveBranch(pullRequest.getBranchName());

		if(branch.getAhead() == 0) {
			pullRequest.setOpen(false);
			pullRequests.merge(pullRequest);
		}

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("group", group);
		parameters.put("commit", branch.getCommit());
		parameters.put("branch", branch);
		parameters.put("pullRequest", pullRequest);
		parameters.put("events", pullRequestBackend.getEventsForPullRequest(repository, pullRequest));
		parameters.put("repository", repository);
		parameters.put("states", new CommitChecker(group, buildResults));

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-pull.ftl", locales, parameters));
	}

    @GET
    @Transactional
    @Path("/pull/{pullId}/diff")
    public Response getPullRequestDiff(@Context HttpServletRequest request,
                                       @PathParam("pullId") long pullId)
                                       throws ApiError, IOException, GitClientException {

		PullRequest pullRequest = pullRequests.findById(group, pullId);
		Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());
		nl.tudelft.ewi.git.client.Branch branch = repository.retrieveBranch(pullRequest.getBranchName());
		DiffBlameModel diffBlameModel = branch.diffBlame();
		List<String> commitIds = Lists.transform(diffBlameModel.getCommits(), CommitModel::getCommit);

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("group", group);
		parameters.put("commit", branch.getCommit());
		parameters.put("commentChecker", commentBackend.getCommentChecker(commitIds));
		parameters.put("branch", branch);
		parameters.put("pullRequest", pullRequest);
		parameters.put("repository", repository);
		parameters.put("diffViewModel", diffBlameModel);
		parameters.put("states", new CommitChecker(group, buildResults));

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-pull-diff-view.ftl", locales, parameters));
    }

	@POST
	@Path("/pull/{pullId}/merge")
	@Produces(MediaType.APPLICATION_JSON)
	public MergeResponse mergePullRequest(@Context HttpServletRequest request,
										  @PathParam("pullId") long pullId) throws GitClientException {

		PullRequest pullRequest = pullRequests.findById(group, pullId);
		Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());
		nl.tudelft.ewi.git.client.Branch branch = repository.retrieveBranch(pullRequest.getBranchName());

		String message = String.format("Merge pull request #%s from %s", pullRequest.getIssueId(), pullRequest.getBranchName());
		MergeResponse response = branch.merge(message, currentUser.getName(), currentUser.getEmail());

		if(response.isSuccess()) {
			pullRequest.setOpen(false);
			pullRequests.merge(pullRequest);
		}

		return response;
	}

    @POST
    @Transactional
    @Path("/comment")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response commentOnPull(@Context HttpServletRequest request,
								  @NotEmpty @FormParam("link-commit") String linkCommitId,
								  @NotEmpty @FormParam("source-commit") String sourceCommitId,
								  @FormParam("source-line-number") Integer sourceLineNumber,
								  @NotEmpty @FormParam("source-file-name") String sourceFileName,
								  @NotEmpty @FormParam("content") String message,
								  @NotEmpty @FormParam("redirect") String redirect)
			throws IOException, ApiError {

        commentBackend.commentBuilder()
                .setCommitId(linkCommitId)
                .setMessage(message)
                .setSourceFilePath(sourceFileName)
                .setSourceLineNumber(sourceLineNumber)
                .setSourceCommitId(sourceCommitId)
                .persist();

        return Response.seeOther(URI.create(redirect)).build();
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
		List<String> commitIds = Lists.transform(diffBlameModel.getCommits(), CommitModel::getCommit);

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("group", group);
		parameters.put("commit", commit);
		parameters.put("repository", repository);
		parameters.put("diffViewModel", diffBlameModel);
		parameters.put("commentChecker", commentBackend.getCommentChecker(commitIds));
		parameters.put("states", new CommitChecker(group, buildResults));

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
		Commit commit = repository.retrieveCommit(commitId);
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
        CommentBackend.CommentChecker commentChecker = commentBackend.getCommentChecker(Lists.newArrayList(commitId));

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("commit", commit);
        parameters.put("blame", blame);
        parameters.put("comments", commentChecker);
		parameters.put("path", path);
		parameters.put("contents", contents);
		parameters.put("highlight", Highlight.forFileName(path));
		parameters.put("group", group);
		parameters.put("repository", repository);
		parameters.put("states", new CommitChecker(group, buildResults));

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-file-view.ftl", locales, parameters));
	}

	@Data
	public static class CommitChecker {
		private final Group group;
		private final BuildResults buildResults;

		public boolean hasFinished(String commitId) {
			try {
				BuildResult buildResult = buildResults.find(group, commitId);
				return buildResult.getSuccess() != null;
			}
			catch (EntityNotFoundException e) {
				return false;
			}
		}

		public boolean hasStarted(String commitId) {
			try {
				buildResults.find(group, commitId);
				return true;
			}
			catch (EntityNotFoundException e) {
				return false;
			}
		}

		public boolean hasSucceeded(String commitId) {
			BuildResult buildResult = buildResults.find(group, commitId);
			return buildResult.getSuccess();
		}

		public String getLog(String commitId) {
			BuildResult buildResult = buildResults.find(group, commitId);
			return buildResult.getLog();
		}
	}

	@Data
	public static class HasCommentsChecker {

		private final CommitComments comments;

		public boolean hasComments(String commitId) {
			return comments.hasComments(commitId);
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
