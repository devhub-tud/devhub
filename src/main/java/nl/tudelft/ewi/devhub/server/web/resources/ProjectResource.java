package nl.tudelft.ewi.devhub.server.web.resources;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.*;
import nl.tudelft.ewi.devhub.server.database.entities.*;
import nl.tudelft.ewi.devhub.server.web.view.models.DiffViewModel;
import nl.tudelft.ewi.git.models.*;
import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;
import nl.tudelft.ewi.devhub.server.backend.GitBackend;
import nl.tudelft.ewi.devhub.server.util.Highlight;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;

@Slf4j
@RequestScoped
@Path("courses/{courseCode}/groups/{groupNumber}")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class ProjectResource extends Resource {
	
	private static final int PAGE_SIZE = 25;

	private final TemplateEngine templateEngine;
	private final GitBackend gitBackend;
	private final User currentUser;
	private final BuildResults buildResults;
	private final Group group;
	private final PullRequests pullRequests;
	private final CommentBackend commentBackend;

	@Inject
	ProjectResource(final TemplateEngine templateEngine,
			final GitBackend gitBackend,
			final @Named("current.user") User currentUser,
			final @Named("current.group") Group group,
			final CommentBackend commentBackend,
			final BuildResults buildResults,
			final PullRequests pullRequests) {

		this.templateEngine = templateEngine;
		this.group = group;
		this.gitBackend = gitBackend;
		this.currentUser = currentUser;
		this.commentBackend = commentBackend;
		this.buildResults = buildResults;
		this.pullRequests = pullRequests;
	}

	@GET
	@Transactional
	public Response showProjectOverview(@Context HttpServletRequest request,
			@PathParam("courseCode") String courseCode, @PathParam("groupNumber") long groupNumber,
			@QueryParam("fatal") String fatal) throws IOException, ApiError {


		DetailedRepositoryModel repository = gitBackend.fetchRepositoryView(group);
		DetailedBranchModel branch;

		try {
			branch = gitBackend.retrieveBranch(repository, "master", 0, PAGE_SIZE);
		}
		catch (Throwable e) {
			if(!repository.getBranches().isEmpty()) {
				String branchName = repository.getBranches().iterator().next().getName();
				branch = gitBackend.fetchBranch(repository, branchName, 1, PAGE_SIZE);
			}
			else {
				branch = null; // no commits
			}
		}

		return showBranchOverview(request, group, repository, branch, 1);
	}

	@GET
	@Path("/branch/{branchName}")
	@Transactional
	public Response showBranchOverview(@Context HttpServletRequest request,
			@PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") String groupNumber,
			@PathParam("branchName") String branchName,
			@QueryParam("page") @DefaultValue("1") int page,
			@QueryParam("fatal") String fatal) throws IOException, ApiError {

		DetailedRepositoryModel repository = gitBackend.fetchRepositoryView(group);
		DetailedBranchModel branch = gitBackend.fetchBranch(repository, branchName, page, PAGE_SIZE);
		
		return showBranchOverview(request, group, repository, branch, page);
	}
	
	private Response showBranchOverview(@Context HttpServletRequest request,
			Group group, DetailedRepositoryModel repository,
			DetailedBranchModel branch, int page) throws IOException {
		
		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("group", group);
		parameters.put("states", new CommitChecker(group, buildResults));
		parameters.put("repository", repository);
		
		if(branch != null) {
			parameters.put("branch", branch);
			parameters.put("pagination", new Pagination(page, branch.getAmountOfCommits()));
			
			PullRequest pullRequest = pullRequests.findOpenPullRequest(group, branch.getName());
			if(pullRequest != null) {
				parameters.put("pullRequest", pullRequest);
			}
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

    @GET
    @Transactional
    @Path("/pulls")
    public Response getPullRequests(@Context HttpServletRequest request) throws IOException {
        throw new javax.ws.rs.NotFoundException();
    }
	
	@GET
	@Transactional
	@Path("/pull/{pullId}")
	public Response getPullRequest(@Context HttpServletRequest request,
			@PathParam("pullId") long pullId) throws ApiError, IOException {

        PullRequest pullRequest = pullRequests.findById(pullId);
		DetailedRepositoryModel repository = gitBackend.fetchRepositoryView(group);
		BranchModel branchModel = repository.getBranch(pullRequest.getBranchName());
		CommitModel commitModel = gitBackend.fetchCommitView(repository, branchModel.getCommit().getCommit());
        CommitModel masterCommit = repository.getBranch("master").getCommit();
        CommitModel mergeBase = gitBackend.mergeBase(repository, masterCommit.getCommit(), commitModel.getCommit());

        DiffViewModel diffViewModel = DiffViewModel.builder(gitBackend, commentBackend)
            .setNewCommit(commitModel)
            .setOldCommit(mergeBase)
            .setRepository(repository)
            .build();

        Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("group", group);
		parameters.put("commit", commitModel);
        parameters.put("branch", branchModel);
		parameters.put("pullRequest", pullRequest);
		parameters.put("repository", repository);
        parameters.put("diffViewModel", diffViewModel);
        parameters.put("states", new CommitChecker(group, buildResults));

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-pull.ftl", locales, parameters));
	}



    @GET
    @Transactional
    @Path("/pull/{pullId}/diff")
    public Response getPullRequestDiff(@Context HttpServletRequest request,
                                       @PathParam("pullId") long pullId)
                                       throws ApiError, IOException {

        PullRequest pullRequest = pullRequests.findById(pullId);
        DetailedRepositoryModel repository = gitBackend.fetchRepositoryView(group);
        BranchModel branchModel = repository.getBranch(pullRequest.getBranchName());
        CommitModel commitModel = gitBackend.fetchCommitView(repository, branchModel.getCommit().getCommit());
        CommitModel masterCommit = repository.getBranch("master").getCommit();
        CommitModel mergeBase = gitBackend.mergeBase(repository, masterCommit.getCommit(), commitModel.getCommit());

        DiffViewModel diffViewModel = DiffViewModel.builder(gitBackend, commentBackend)
                .setNewCommit(commitModel)
                .setOldCommit(mergeBase)
                .setRepository(repository)
                .build();

        Map<String, Object> parameters = Maps.newLinkedHashMap();
        parameters.put("user", currentUser);
        parameters.put("group", group);
        parameters.put("commit", commitModel);
        parameters.put("branch", branchModel);
        parameters.put("pullRequest", pullRequest);
        parameters.put("repository", repository);
        parameters.put("diffViewModel", diffViewModel);
        parameters.put("states", new CommitChecker(group, buildResults));

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("project-pull-diff-view.ftl", locales, parameters));
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
	public Response showCommitBuild(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") String groupNumber, @PathParam("commitId") String commitId,
			@QueryParam("fatal") String fatal) throws IOException, ApiError {

		DetailedRepositoryModel repository = gitBackend.fetchRepositoryView(group);
		DetailedCommitModel commit = gitBackend.fetchCommitView(repository, commitId);

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
	@Path("/commits/{commitId}/diff")
	@Transactional
	public Response showCommitChanges(@Context HttpServletRequest request,
			@PathParam("commitId") String commitId)
			throws IOException, ApiError {
	
		return showDiff(request, commitId, null);
	}

	@GET
	@Path("/commits/{newId}/diff/{oldId}")
	@Transactional
	public Response showDiff(@Context HttpServletRequest request,
                             @PathParam("newId") String newId,
                             @PathParam("oldId") String oldId)
                             throws ApiError, IOException {
		
        DetailedRepositoryModel repository = gitBackend.fetchRepositoryView(group);
		CommitModel commitModel = gitBackend.fetchCommitView(repository, newId);

        if(commitModel.getParents().length != 0 && oldId == null) {
            oldId = commitModel.getParents()[0];
        }

        CommitModel oldCommitModel = oldId == null ? null : gitBackend.fetchCommitView(repository, oldId);
        DiffViewModel diffViewModel = DiffViewModel.builder(gitBackend, commentBackend)
                .setNewCommit(commitModel)
                .setOldCommit(oldCommitModel)
                .setRepository(repository)
                .build();

        Map<String, Object> parameters = Maps.newLinkedHashMap();
        parameters.put("user", currentUser);
        parameters.put("group", group);
        parameters.put("commit", commitModel);
        parameters.put("repository", repository);
        parameters.put("diffViewModel", diffViewModel);
        parameters.put("states", new CommitChecker(group, buildResults));
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-diff-view.ftl", locales, parameters));
	}

    private Map<DiffModel, BlameModel> getBlameModelsForDiff(DetailedRepositoryModel repository,
                                                             CommitModel commit,
                                                             CommitModel oldCommit,
                                                             DiffResponse diffResponse) throws ApiError {
        Map<DiffModel, BlameModel> blames = Maps.<DiffModel, BlameModel> newHashMap();
        for(DiffModel diffModel : diffResponse.getDiffs()) {
            if(diffModel.isDeleted()) {
                // If the file was added in this commit, there are no possible changes in previous commits
            }
            if(diffModel.isDeleted()) {
                // If the file was deleted, we should fetch the blame of the old path in the old commit
                blames.put(diffModel, gitBackend.blame(repository, oldCommit, diffModel.getOldPath()));
            }
            else {
                // Else, fetch the blame of the new path in the current commit
                blames.put(diffModel, gitBackend.blame(repository, commit, diffModel.getNewPath()));
            }
        }
        return blames;
    }

    @GET
	@Path("/commits/{commitId}/tree")
	@Transactional
	public Response getTree(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("commitId") String commitId)
					throws ApiError, IOException {
		return getTree(request, courseCode, groupNumber, commitId, "");
	}
	
	@GET
	@Path("/commits/{commitId}/tree/{path:.+}")
	@Transactional
	public Response getTree(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("commitId") String commitId,
			@PathParam("path") String path) throws ApiError, IOException {
		
		DetailedRepositoryModel repository = gitBackend.fetchRepositoryView(group);
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
		
		entries.putAll(gitBackend.listDirectoryEntries(repository, commitId, path));
		
		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("commit", gitBackend.fetchCommitView(repository, commitId));
		parameters.put("path", path);
		parameters.put("group", group);
		parameters.put("repository", repository);
		parameters.put("entries", entries);
		parameters.put("states", new CommitChecker(group, buildResults));
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-folder-view.ftl", locales, parameters));
	}

	@GET
	@Path("/commits/{commitId}/blob/{path:.+}")
	@Transactional
	public Response getBlob(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("commitId") String commitId,
			@PathParam("path") String path) throws ApiError, IOException {

		String folderPath = "";
		String fileName = path;
		if (path.contains("/")) {
			folderPath = path.substring(0, path.lastIndexOf('/'));
			fileName = path.substring(path.lastIndexOf('/') + 1);
		}
		
		DetailedRepositoryModel repository = gitBackend.fetchRepositoryView(group);
		Map<String, EntryType> entries = gitBackend.listDirectoryEntries(repository, commitId, folderPath);
		
		EntryType type = entries.get(fileName);
		
		if (type == EntryType.BINARY) {
			return Response.ok(gitBackend.showBinFile(repository, commitId, path))
					.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
					.build();
		}
		
		String[] contents = gitBackend.showFile(repository, commitId, path).split("\\r?\\n");
		
		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("commit", gitBackend.fetchCommitView(repository, commitId));
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
	static public class Pagination {
				
		private final int page, total;
		
		public int getPageCount() {
			return (total + PAGE_SIZE - 1) / PAGE_SIZE;
		}
		
	}
	
}
