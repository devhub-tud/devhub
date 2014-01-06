package nl.devhub.server.web.resources;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.SneakyThrows;
import nl.devhub.server.database.controllers.GroupMemberships;
import nl.devhub.server.database.controllers.Groups;
import nl.devhub.server.database.controllers.Projects;
import nl.devhub.server.database.controllers.Users;
import nl.devhub.server.database.entities.Group;
import nl.devhub.server.database.entities.Project;
import nl.devhub.server.web.templating.TemplateEngine;

import com.google.common.collect.Maps;

@Path("projects")
@Produces(MediaType.TEXT_HTML)
public class ProjectsResource {

	private static final int USER_ID = 1;

	private final TemplateEngine templateEngine;
	private final GroupMemberships groupMemberships;
	private final Groups groups;
	private final Projects projects;
	private final Users users;

	@Inject
	public ProjectsResource(TemplateEngine templateEngine, GroupMemberships groupMemberships, Groups groups,
			Projects projects, Users users) {
		
		this.templateEngine = templateEngine;
		this.groupMemberships = groupMemberships;
		this.groups = groups;
		this.projects = projects;
		this.users = users;
	}

	@GET
	public String showProjectsOverview(@Context HttpServletRequest request) throws IOException {
		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", users.find(USER_ID));
		parameters.put("groups", groupMemberships.listParticipating(USER_ID));

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("projects.ftl", locales, parameters);
	}

	@GET
	@Path("{projectCode}/groups/{groupNumber}")
	@SneakyThrows
	public Response redirectToProjectDashboard(@PathParam("projectCode") String projectCode,
			@PathParam("groupNumber") String groupNumber) {
		
		return Response.seeOther(new URI("/projects/" + projectCode + "/groups/" + groupNumber + "/dashboard")).build();
	}

	@GET
	@Path("{projectCode}/groups/{groupNumber}/dashboard")
	public String showProjectDashboard(@PathParam("projectCode") String projectCode,
			@PathParam("groupNumber") long groupNumber, @Context HttpServletRequest request) throws IOException {
		
		Project project = projects.find(projectCode);
		Group group = groups.find(project, groupNumber);

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", users.find(USER_ID));
		parameters.put("group", group);
		parameters.put("section", "dashboard");
		parameters.put("pageHeader", "Dashboard");

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("project.ftl", locales, parameters);
	}

	@GET
	@Path("{projectCode}/groups/{groupNumber}/issues")
	public String showProjectIssues(@PathParam("projectCode") String projectCode,
			@PathParam("groupNumber") long groupNumber, @Context HttpServletRequest request) throws IOException {

		Project project = projects.find(projectCode);
		Group group = groups.find(project, groupNumber);

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", users.find(USER_ID));
		parameters.put("group", group);
		parameters.put("section", "issues");
		parameters.put("pageHeader", "Issues");

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("project.ftl", locales, parameters);
	}

	@GET
	@Path("{projectCode}/groups/{groupNumber}/issues/{issueNumber}")
	public String showProjectIssue(@PathParam("projectCode") String projectCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("issueNumber") long issueNumber,
			@Context HttpServletRequest request) throws IOException {

		Project project = projects.find(projectCode);
		Group group = groups.find(project, groupNumber);

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", users.find(USER_ID));
		parameters.put("group", group);
		parameters.put("section", "issues");
		parameters.put("subsection", issueNumber);
		parameters.put("pageHeader", "Issue #" + issueNumber);

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("project.ftl", locales, parameters);
	}

	@GET
	@Path("{projectCode}/groups/{groupNumber}/pull-requests")
	public String showProjectPullRequests(@PathParam("projectCode") String projectCode,
			@PathParam("groupNumber") long groupNumber, @Context HttpServletRequest request) throws IOException {

		Project project = projects.find(projectCode);
		Group group = groups.find(project, groupNumber);

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", users.find(USER_ID));
		parameters.put("group", group);
		parameters.put("section", "pull-requests");
		parameters.put("pageHeader", "Pull-requests");

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("project.ftl", locales, parameters);
	}

	@GET
	@Path("{projectCode}/groups/{groupNumber}/pull-requests/{pullRequestNumber}")
	public String showProjectPullRequest(@PathParam("projectCode") String projectCode,
			@PathParam("groupNumber") long groupNumber, @PathParam("pullRequestNumber") long pullRequestNumber,
			@Context HttpServletRequest request) throws IOException {

		Project project = projects.find(projectCode);
		Group group = groups.find(project, groupNumber);

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", users.find(USER_ID));
		parameters.put("group", group);
		parameters.put("section", "pull-requests");
		parameters.put("subsection", pullRequestNumber);
		parameters.put("pageHeader", "Pull-request #" + pullRequestNumber);

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("project.ftl", locales, parameters);
	}

	@GET
	@Path("{projectCode}/groups/{groupNumber}/deliverables")
	public String showDeliverables(@PathParam("projectCode") String projectCode,
			@PathParam("groupNumber") long groupNumber, @Context HttpServletRequest request) throws IOException {

		Project project = projects.find(projectCode);
		Group group = groups.find(project, groupNumber);

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", users.find(USER_ID));
		parameters.put("group", group);
		parameters.put("section", "deliverables");
		parameters.put("pageHeader", "Deliverables");

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("project.ftl", locales, parameters);
	}

}
