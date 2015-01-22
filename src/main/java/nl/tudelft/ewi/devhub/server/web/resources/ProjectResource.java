package nl.tudelft.ewi.devhub.server.web.resources;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.Data;
import nl.tudelft.ewi.devhub.server.backend.GitBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.util.Highlight;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.models.DetailedBranchModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.EntryType;

import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
@Path("projects/{courseCode}/groups/{groupNumber}")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class ProjectResource extends Resource {
	
	private static final int PAGE_SIZE = 25;

	private final TemplateEngine templateEngine;
	private final GitBackend gitBackend;
	private final Groups groups;
	private final Courses courses;
	private final User currentUser;
	private final BuildResults buildResults;

	@Inject
	ProjectResource(TemplateEngine templateEngine, Groups groups,
			Courses projects, GitBackend gitBackend,
			@Named("current.user") User currentUser, BuildResults buildResults) {

		this.templateEngine = templateEngine;
		this.courses = projects;
		this.groups = groups;
		this.gitBackend = gitBackend;
		this.currentUser = currentUser;
		this.buildResults = buildResults;
	}
	
	@GET
	@Transactional
	public Response showProjectOverview(@Context HttpServletRequest request,
			@PathParam("courseCode") String courseCode, @PathParam("groupNumber") long groupNumber,
			@QueryParam("fatal") String fatal) throws IOException, ApiError {

		Course course = courses.find(courseCode);
		Group group = groups.find(course, groupNumber);
		
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

		Course course = courses.find(courseCode);
		Group group = groups.find(course, Long.parseLong(groupNumber));
		
		DetailedRepositoryModel repository = gitBackend.fetchRepositoryView(group);
		DetailedBranchModel branch = gitBackend.fetchBranch(repository, branchName, page, PAGE_SIZE);
		
		return showBranchOverview(request, group, repository, branch, page);
	}
	
	private Response showBranchOverview(HttpServletRequest request,
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
		}
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-view.ftl", locales, parameters));
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

		Course course = courses.find(courseCode);
		Group group = groups.find(course, Long.parseLong(groupNumber));

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
	public Response showCommitChanges(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("commitId") String commitId) throws IOException, ApiError {
	
		return showDiff(request, courseCode, groupNumber, commitId, null);
	}

	@GET
	@Path("/commits/{oldId}/diff/{newId}")
	@Transactional
	public Response showDiff(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("oldId") String oldId,
			@PathParam("newId") String newId) throws ApiError, IOException {
		
		Course course = courses.find(courseCode);
		Group group = groups.find(course, groupNumber);

		DetailedRepositoryModel repository = gitBackend.fetchRepositoryView(group);
		List<DiffModel> diffs = gitBackend.fetchDiffs(repository, newId, oldId);

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("group", group);
		parameters.put("diffs", diffs);
		parameters.put("commit", gitBackend.fetchCommitView(repository, oldId));
		parameters.put("repository", repository);
		parameters.put("states", new CommitChecker(group, buildResults));

		if(newId != null) {
			DetailedCommitModel newCommit = gitBackend.fetchCommitView(repository, newId);
			parameters.put("newCommit", newCommit);
		}
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-diff-view.ftl", locales, parameters));
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
		
		Course course = courses.find(courseCode);
		Group group = groups.find(course, groupNumber);

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

		Course course = courses.find(courseCode);
		Group group = groups.find(course, groupNumber);

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
