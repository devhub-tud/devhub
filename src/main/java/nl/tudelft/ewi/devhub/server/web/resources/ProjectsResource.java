package nl.tudelft.ewi.devhub.server.web.resources;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.tudelft.ewi.devhub.server.backend.ProjectsBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.filters.RequestScope;
import nl.tudelft.ewi.devhub.server.web.filters.RequireAuthenticatedUser;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

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

	private final ProjectsBackend projectsBackend;
	private final TemplateEngine templateEngine;
	private final Courses courses;
	private final RequestScope scope;
	private final Users users;

	@Inject
	ProjectsResource(TemplateEngine templateEngine, ProjectsBackend projectsBackend, Courses projects,
			RequestScope scope, Users users) {

		this.templateEngine = templateEngine;
		this.projectsBackend = projectsBackend;
		this.courses = projects;
		this.scope = scope;
		this.users = users;
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

}