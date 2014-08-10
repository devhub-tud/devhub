package nl.tudelft.ewi.devhub.server.web.resources;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.Data;
import nl.tudelft.ewi.devhub.server.backend.ProjectsBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.util.DiffLine;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.filters.RequestScope;
import nl.tudelft.ewi.devhub.server.web.filters.RequireAuthenticatedUser;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedBranchModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.EntryType;

import org.eclipse.jetty.util.UrlEncoded;
import org.jboss.resteasy.plugins.guice.RequestScoped;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.persist.Transactional;

@RequestScoped
@Path("projects")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
@RequireAuthenticatedUser
public class ProjectsResource extends Resource {

	private static final int MAX_GROUP_SIZE = 8;
	private static final int MIN_GROUP_SIZE = 1;
	private static final int PAGE_SIZE = 25;

	private final ProjectsBackend projectsBackend;
	private final TemplateEngine templateEngine;
	private final GitServerClient client;
	private final Groups groups;
	private final Courses courses;
	private final RequestScope scope;
	private final Users users;
	private final BuildResults buildResults;

	@Inject
	ProjectsResource(TemplateEngine templateEngine, Groups groups, ProjectsBackend projectsBackend, Courses projects,
			GitServerClient client, RequestScope scope, Users users, BuildResults buildResults) {

		this.templateEngine = templateEngine;
		this.projectsBackend = projectsBackend;
		this.courses = projects;
		this.groups = groups;
		this.client = client;
		this.scope = scope;
		this.users = users;
		this.buildResults = buildResults;
	}

	@GET
	@Transactional
	public Response showProjectsOverview(@Context HttpServletRequest request) throws IOException {
		User requester = scope.getUser();

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", requester);

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("projects.ftl", locales, parameters));
	}

	@GET
	@Path("setup")
	@Transactional
	public Response showProjectSetupPage(@Context HttpServletRequest request, @QueryParam("error") String error,
			@QueryParam("step") Integer step) throws IOException {

		if (step != null) {
			if (step == 1) {
				return showProjectSetupPageStep1(request, error);
			}
			else if (step == 2) {
				return showProjectSetupPageStep2(request, error);
			}
			else if (step == 3) {
				return showProjectSetupPageStep3(request, error);
			}
		}
		return redirect("/projects/setup?step=1");
	}

	private Response showProjectSetupPageStep1(@Context HttpServletRequest request, @QueryParam("error") String error)
			throws IOException {

		User requester = scope.getUser();
		HttpSession session = request.getSession();

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", requester);
		parameters.put("courses", courses.listNotYetParticipatedCourses(requester));
		if (session.getAttribute("projects.setup.course") != null) {
			parameters.put("course", courses.find(String.valueOf(session.getAttribute("projects.setup.course"))));
		}
		if (!Strings.isNullOrEmpty(error)) {
			parameters.put("error", error);
		}

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-setup-1.ftl", locales, parameters));
	}

	@SuppressWarnings("unchecked")
	private Response showProjectSetupPageStep2(@Context HttpServletRequest request, @QueryParam("error") String error)
			throws IOException {

		User requester = scope.getUser();
		HttpSession session = request.getSession();
		Course course = courses.find(String.valueOf(session.getAttribute("projects.setup.course")));
		List<User> members = (List<User>) session.getAttribute("projects.setup.members");

		int maxGroupSize = getMaxGroupSize(course);
		int minGroupSize = getMinGroupSize(course);

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", requester);
		parameters.put("course", course);
		if (members != null && !members.isEmpty()) {
			parameters.put("members", members);
		}
		parameters.put("maxGroupSize", maxGroupSize);
		parameters.put("minGroupSize", minGroupSize);
		if (!Strings.isNullOrEmpty(error)) {
			parameters.put("error", error);
		}

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-setup-2.ftl", locales, parameters));
	}

	@SuppressWarnings("unchecked")
	private Response showProjectSetupPageStep3(@Context HttpServletRequest request, @QueryParam("error") String error)
			throws IOException {

		User requester = scope.getUser();
		HttpSession session = request.getSession();
		Course course = courses.find(String.valueOf(session.getAttribute("projects.setup.course")));
		List<User> members = (List<User>) session.getAttribute("projects.setup.members");

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", requester);
		parameters.put("course", course);
		parameters.put("members", members);
		if (!Strings.isNullOrEmpty(error)) {
			parameters.put("error", error);
		}

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-setup-3.ftl", locales, parameters));
	}

	@POST
	@Path("setup")
	@SuppressWarnings("unchecked")
	public Response processProjectSetup(@Context HttpServletRequest request, @QueryParam("step") int step)
			throws IOException {

		HttpSession session = request.getSession();
		User requester = scope.getUser();

		if (step == 1) {
			String courseCode = request.getParameter("course");
			Course course = courses.find(courseCode);
			courseCode = course.getCode();

			String previousCourseCode = String.valueOf(session.getAttribute("projects.setup.course"));
			session.setAttribute("projects.setup.course", courseCode);
			if (!courseCode.equals(previousCourseCode)) {
				session.removeAttribute("projects.setup.members");
			}

			return redirect("/projects/setup?step=2");
		}
		else if (step == 2) {
			List<User> groupMembers = getGroupMembers(request);
			Course course = courses.find(String.valueOf(session.getAttribute("projects.setup.course")));
			int maxGroupSize = getMaxGroupSize(course);
			int minGroupSize = getMinGroupSize(course);

			if (!groupMembers.contains(requester) && !requester.isAdmin() && !requester.isAssisting(course)) {
				return redirect("/projects/setup?step=2&error=error.must-be-group-member");
			}
			if (groupMembers.size() < minGroupSize || groupMembers.size() > maxGroupSize) {
				return redirect("/projects/setup?step=2&error=error.invalid-group-size");
			}

			for (User user : groupMembers) {
				if (user.isParticipatingInCourse(course)) {
					return redirect("/projects/setup?step=2&error=error.already-registered-for-course");
				}
			}

			session.setAttribute("projects.setup.members", groupMembers);
			return redirect("/projects/setup?step=3");
		}

		try {
			Course course = courses.find(String.valueOf(session.getAttribute("projects.setup.course")));
			List<User> members = (List<User>) session.getAttribute("projects.setup.members");
			projectsBackend.setupProject(course, members);
			
			session.removeAttribute("projects.setup.course");
			session.removeAttribute("projects.setup.members");
			return redirect("/projects");
		}
		catch (ApiError e) {
			String error = UrlEncoded.encodeString(e.getResourceKey());
			return redirect("/projects/setup?step=3&error=" + error);
		}
	}

	@GET
	@Path("{courseCode}/groups/{groupNumber}")
	@Transactional
	public Response showProjectOverview(@Context HttpServletRequest request,
			@PathParam("courseCode") String courseCode, @PathParam("groupNumber") String groupNumber,
			@QueryParam("fatal") String fatal) throws IOException, ApiError {

		User user = scope.getUser();
		Course course = courses.find(courseCode);
		Group group = groups.find(course, Long.parseLong(groupNumber));
		
		if (!user.isAdmin() && !user.isAssisting(course) && !user.isMemberOf(group)) {
			throw new UnauthorizedException();
		}

		DetailedRepositoryModel repository = fetchRepositoryView(group);
		DetailedBranchModel branch;
		
		try {
			branch = client.repositories().retrieveBranch(repository, "master", 0, PAGE_SIZE);
		}
		catch (Throwable e) {
			if(!repository.getBranches().isEmpty()) {
				String branchName = repository.getBranches().iterator().next().getName();
				branch = fetchBranch(repository, branchName, 1);
			}
			else {
				branch = null; // no commits
			}
		}
		
		return showBranchOverview(request, group, repository, branch, 1);
	}
	
	@GET
	@Path("{courseCode}/groups/{groupNumber}/branch/{branchName}")
	@Transactional
	public Response showBranchOverview(@Context HttpServletRequest request,
			@PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") String groupNumber,
			@PathParam("branchName") String branchName,
			@QueryParam("page") @DefaultValue("1") int page,
			@QueryParam("fatal") String fatal) throws IOException, ApiError {

		User user = scope.getUser();
		Course course = courses.find(courseCode);
		Group group = groups.find(course, Long.parseLong(groupNumber));
		
		if (!user.isAdmin() && !user.isAssisting(course) && !user.isMemberOf(group)) {
			throw new UnauthorizedException();
		}

		DetailedRepositoryModel repository = fetchRepositoryView(group);
		DetailedBranchModel branch = fetchBranch(repository, branchName, page);
		
		return showBranchOverview(request, group, repository, branch, page);
	}
	
	private Response showBranchOverview(HttpServletRequest request,
			Group group, DetailedRepositoryModel repository,
			DetailedBranchModel branch, int page) throws IOException {
		
		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", scope.getUser());
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
	@Path("{courseCode}/groups/{groupNumber}/commits/{commitId}")
	public Response showCommitOverview(@Context HttpServletRequest request) {
		return redirect(request.getPathInfo() + "/diff");
	}
	
	@GET
	@Path("{courseCode}/groups/{groupNumber}/commits/{commitId}/build")
	@Transactional
	public Response showCommitBuild(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") String groupNumber, @PathParam("commitId") String commitId,
			@QueryParam("fatal") String fatal) throws IOException, ApiError {

		User user = scope.getUser();
		Course course = courses.find(courseCode);
		Group group = groups.find(course, Long.parseLong(groupNumber));

		if (!user.isAdmin() && !user.isAssisting(course) && !user.isMemberOf(group)) {
			throw new UnauthorizedException();
		}

		DetailedRepositoryModel repository = fetchRepositoryView(group);
		CommitModel commit = fetchCommitView(repository, commitId);

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", scope.getUser());
		parameters.put("group", group);
		parameters.put("commit", commit);
		parameters.put("states", new CommitChecker(group, buildResults));
		parameters.put("repository", repository);

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-commit-view.ftl", locales, parameters));
	}
	
	@GET
	@Path("{courseCode}/groups/{groupNumber}/commits/{commitId}/diff")
	@Transactional
	public Response showCommitChanges(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("commitId") String commitId) throws IOException, ApiError {
	
		return showDiff(request, courseCode, groupNumber, commitId, null);
	}

	@GET
	@Path("{courseCode}/groups/{groupNumber}/commits/{oldId}/diff/{newId}")
	@Transactional
	public Response showDiff(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("oldId") String oldId,
			@PathParam("newId") String newId) throws ApiError, IOException {
		
		User user = scope.getUser();
		Course course = courses.find(courseCode);
		Group group = groups.find(course, groupNumber);

		if (!user.isAdmin() && !user.isAssisting(course) && !user.isMemberOf(group)) {
			throw new UnauthorizedException();
		}

		DetailedRepositoryModel repository = fetchRepositoryView(group);
		List<Diff> diffs = fetchDiffs(repository, newId, oldId);

		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", scope.getUser());
		parameters.put("group", group);
		parameters.put("diffs", diffs);
		parameters.put("commit", fetchCommitView(repository, oldId));
		parameters.put("repository", repository);
		parameters.put("states", new CommitChecker(group, buildResults));

		if(newId != null) {
			CommitModel newCommit = fetchCommitView(repository, newId);
			parameters.put("newCommit", newCommit);
		}
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-diff-view.ftl", locales, parameters));
	}
	
	@GET
	@Path("{courseCode}/groups/{groupNumber}/commits/{commitId}/tree")
	@Transactional
	public Response getTree(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("commitId") String commitId)
					throws ApiError, IOException {
		return getTree(request, courseCode, groupNumber, commitId, "");
	}
	
	@GET
	@Path("{courseCode}/groups/{groupNumber}/commits/{commitId}/tree/{path:.+}")
	@Transactional
	public Response getTree(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("commitId") String commitId,
			@PathParam("path") String path) throws ApiError, IOException {
		
		User user = scope.getUser();
		Course course = courses.find(courseCode);
		Group group = groups.find(course, groupNumber);

		if (!user.isAdmin() && !user.isAssisting(course) && !user.isMemberOf(group)) {
			throw new UnauthorizedException();
		}

		DetailedRepositoryModel repository = fetchRepositoryView(group);
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
		
		entries.putAll(client.repositories().listDirectoryEntries(repository, commitId, path));
		
		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", scope.getUser());
		parameters.put("commit", fetchCommitView(repository, commitId));
		parameters.put("path", path);
		parameters.put("group", group);
		parameters.put("repository", repository);
		parameters.put("entries", entries);
		parameters.put("states", new CommitChecker(group, buildResults));
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-folder-view.ftl", locales, parameters));
	}
	
	@GET
	@Path("{courseCode}/groups/{groupNumber}/commits/{commitId}/blob/{path:.+}")
	@Transactional
	public Response getBlob(@Context HttpServletRequest request, @PathParam("courseCode") String courseCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("commitId") String commitId,
			@PathParam("path") String path) throws ApiError, IOException {

		User user = scope.getUser();
		Course course = courses.find(courseCode);
		Group group = groups.find(course, groupNumber);

		if (!user.isAdmin() && !user.isAssisting(course) && !user.isMemberOf(group)) {
			throw new UnauthorizedException();
		}
		
		String folderPath = "";
		String fileName = path;
		if (path.contains("/")) {
			folderPath = path.substring(0, path.lastIndexOf('/'));
			fileName = path.substring(path.lastIndexOf('/') + 1);
		}
		
		DetailedRepositoryModel repository = fetchRepositoryView(group);
		Map<String, EntryType> entries = client.repositories().listDirectoryEntries(repository, commitId, folderPath);
		
		EntryType type = entries.get(fileName);
		
		if (type == EntryType.BINARY) {
			return Response.ok(client.repositories().showBinFile(repository, commitId, path))
					.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
					.build();
		}
		
		String[] contents = client.repositories().showFile(repository, commitId, path).split("\\r?\\n");
		
		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", scope.getUser());
		parameters.put("commit", fetchCommitView(repository, commitId));
		parameters.put("path", path);
		parameters.put("contents", contents);
		parameters.put("highlight", !path.matches("^[^.]+|.*[.](txt|md|xml)$"));
		parameters.put("group", group);
		parameters.put("repository", repository);
		parameters.put("states", new CommitChecker(group, buildResults));

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("project-file-view.ftl", locales, parameters));
	}
	
	private DetailedRepositoryModel fetchRepositoryView(Group group) throws ApiError {
		try {
			Repositories repositories = client.repositories();
			return repositories.retrieve(group.getRepositoryName());
		}
		catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable");
		}
	}

	private CommitModel fetchCommitView(DetailedRepositoryModel repository, String commitId) throws ApiError {
		try {
			Repositories repositories = client.repositories();
			return repositories.retrieveCommit(repository, commitId);
		}
		catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable");
		}
	}
	
	private List<Diff> fetchDiffs(DetailedRepositoryModel repository, String oldCommitId, String newCommitId) throws ApiError {
		try {
			Repositories repositories = client.repositories();
			List<Diff> result = Lists.newArrayList();
			List<DiffModel> diffs = repositories.listDiffs(repository, oldCommitId, newCommitId);
			
			for (DiffModel diff : diffs) {
				result.add(new Diff(diff));
			}
			
			return result;
		} catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable");
		}
	}
	
	private DetailedBranchModel fetchBranch(DetailedRepositoryModel repository,
			String branchName, int page) throws ApiError {
		try {
			return client.repositories().retrieveBranch(repository, branchName, (page - 1) * PAGE_SIZE, PAGE_SIZE);
		}
		catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable");
		}
	}

	@Data
	public static class Diff {
		
		private final List<DiffLine> lines;
		private final DiffModel diffModel;
		
		public Diff(DiffModel diffModel) {
			this.diffModel = diffModel;
			this.lines = DiffLine.getLinesFor(diffModel);
		}
		
		public boolean isDeleted() {
			return diffModel.getType().equals(DiffModel.Type.DELETE);
		}
		
		public boolean isAdded() {
			return diffModel.getType().equals(DiffModel.Type.ADD);
		}
		
		public boolean isModified() {
			return diffModel.getType().equals(DiffModel.Type.MODIFY);
		}
		
		public boolean isCopied() {
			return diffModel.getType().equals(DiffModel.Type.COPY);
		}
		
		public boolean isMoved() {
			return diffModel.getType().equals(DiffModel.Type.RENAME);
		}
		
	}

	private List<User> getGroupMembers(HttpServletRequest request) {
		String netId;
		int memberId = 1;
		Set<String> netIds = Sets.newHashSet();
		while (!Strings.isNullOrEmpty((netId = request.getParameter("member-" + memberId)))) {
			memberId++;
			netIds.add(netId);
		}

		Map<String, User> members = users.mapByNetIds(netIds);
		List<User> sortedMembers = Lists.newArrayList();
		for (String memberNetId : netIds) {
			sortedMembers.add(members.get(memberNetId));
		}
		return sortedMembers;
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

	public int getMinGroupSize(Course course) {
		User user = scope.getUser();
		if (user.isAdmin() || user.isAssisting(course)) {
			return MIN_GROUP_SIZE;
		}
		return course.getMinGroupSize();
	}

	public int getMaxGroupSize(Course course) {
		User user = scope.getUser();
		if (user.isAdmin() || user.isAssisting(course)) {
			return MAX_GROUP_SIZE;
		}
		return course.getMaxGroupSize();
	}
	
	@Data
	static public class Pagination {
				
		private final int page, total;
		
		public int getPageCount() {
			return (total + PAGE_SIZE - 1) / PAGE_SIZE;
		}
		
	}

}