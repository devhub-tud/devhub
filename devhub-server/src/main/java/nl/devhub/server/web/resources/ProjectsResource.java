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
import nl.devhub.server.web.templating.TemplateEngine;

import com.google.common.collect.Maps;

@Path("projects/{projectId}")
@Produces(MediaType.TEXT_HTML)
public class ProjectsResource {
	
	private final TemplateEngine templateEngine;

	@Inject
	public ProjectsResource(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	@GET
	@Path("group/{groupId}")
	@SneakyThrows
	public Response showGroup(@PathParam("projectId") String projectId, @PathParam("groupId") String groupId) throws IOException {
		return Response.seeOther(new URI("/projects/" + projectId + "/group/" + groupId + "/activity")).build();
	}
	
	@GET
	@Path("group/{groupId}/{view}")
	public String showView(@PathParam("projectId") String projectId, @PathParam("groupId") String groupId, @PathParam("view") String view, @Context HttpServletRequest request) throws IOException {
		List<Locale> locales = Collections.list(request.getLocales());
		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("project", projectId);
		parameters.put("group", groupId);
		parameters.put("view", Character.toUpperCase(view.charAt(0)) + view.substring(1));
		parameters.put("viewIcon", view + ".png");
		
		return templateEngine.process("project.ftl", locales, parameters);
	}
	
}
