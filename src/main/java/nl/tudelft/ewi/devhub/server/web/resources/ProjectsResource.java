package nl.tudelft.ewi.devhub.server.web.resources;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
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

import nl.tudelft.ewi.devhub.server.backend.ProjectsBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.GroupMemberships;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupMembership;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.FatalNotAllowedException;
import nl.tudelft.ewi.devhub.server.web.filters.RequestScope;
import nl.tudelft.ewi.devhub.server.web.filters.RequireAuthenticatedUser;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.client.GitServerClient;

import org.eclipse.jetty.util.UrlEncoded;
import org.jboss.resteasy.plugins.guice.RequestScoped;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.persist.Transactional;

@RequestScoped
@Path("projects")
@Produces(MediaType.TEXT_HTML)
@RequireAuthenticatedUser
public class ProjectsResource {

	private final ProjectsBackend projectsBackend;
	private final TemplateEngine templateEngine;
	private final GroupMemberships groupMemberships;
	private final GitServerClient client;
	private final Groups groups;
	private final Courses courses;
	private final RequestScope scope;

	@Inject
	ProjectsResource(TemplateEngine templateEngine, GroupMemberships groupMemberships, Groups groups, 
			ProjectsBackend projectsBackend, Courses projects, GitServerClient client, RequestScope scope) {

		this.templateEngine = templateEngine;
		this.groupMemberships = groupMemberships;
		this.projectsBackend = projectsBackend;
		this.courses = projects;
		this.groups = groups;
		this.client = client;
		this.scope = scope;
	}

	@GET
	public String showProjectsOverview(@Context HttpServletRequest request) throws IOException {
		User requester = scope.getUser();
		
		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", requester);
		parameters.put("groups", groupMemberships.listParticipating(requester));
		
		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("projects.ftl", locales, parameters);
	}

	@GET
	@Path("setup")
	@Transactional
	public Response showProjectSetupPage(@Context HttpServletRequest request, @QueryParam("error") String error) 
			throws IOException {
		
		User requester = scope.getUser();

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", requester);
		parameters.put("courses", courses.listNotYetParticipatedCourses(requester));
		if (!Strings.isNullOrEmpty(error)) {
			parameters.put("error", error);
		}
		
		List<Locale> locales = Collections.list(request.getLocales());
		return Response.ok()
				.entity(templateEngine.process("project-setup.ftl", locales, parameters))
				.build();
	}
	
	@POST
	@Path("setup")
	public Response processProjectSetup(@Context HttpServletRequest request, @FormParam("course-id") int courseId)
			throws IOException, URISyntaxException {
		
		User user = scope.getUser();
		
		try {
			projectsBackend.processNewProjectSetup(user.getNetId(), courseId);
			return Response.seeOther(new URI("/projects")).build();
		}
		catch (ApiError e) {
			String error = UrlEncoded.encodeString(e.getResourceKey());
			return Response.seeOther(new URI("/projects/setup?error=" + error)).build();
		}
	}
	
	@GET
	@Path("{courseCode}/groups/{groupNumber}")
	public Response showProjectOverview(@Context HttpServletRequest request, 
			@PathParam("courseCode") String courseCode, 
			@PathParam("groupNumber") String groupNumber) throws URISyntaxException, IOException {
		
		User user = scope.getUser();
		Course course = courses.find(courseCode);
		Group group = groups.find(course, Long.parseLong(groupNumber));

		if (!user.isAdmin() && !isMemberOf(user, group)) {
			throw new FatalNotAllowedException();
		}

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", scope.getUser());
		parameters.put("group", group);
		parameters.put("repository", client.repositories().retrieve(group.getRepositoryName()));

		List<Locale> locales = Collections.list(request.getLocales());
		return Response.ok()
				.entity(templateEngine.process("project-view.ftl", locales, parameters))
				.build();
	}
	
	private boolean isMemberOf(User user, Group group) {
		for (GroupMembership membership : group.getMemberships()) {
			if (membership.getUser().getId() == user.getId()) {
				return true;
			}
		}
		return false;
	}

}
