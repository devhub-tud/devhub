package nl.tudelft.ewi.devhub.server.web.resources;

import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.database.entities.BuildServer;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;

import org.eclipse.jetty.util.UrlEncoded;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RequestScoped
@Path("build-servers")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class BuildServerResource extends Resource {

	private final TemplateEngine templateEngine;
	private final BuildsBackend backend;
	private final User currentUser;

	@Inject
	BuildServerResource(TemplateEngine templateEngine, BuildsBackend backend, @Named("current.user") User currentUser) {
		this.templateEngine = templateEngine;
		this.backend = backend;
		this.currentUser = currentUser;
	}

	@GET
	public Response showBuildServers(@Context HttpServletRequest request, @QueryParam("error") String error) 
			throws IOException, URISyntaxException {
		
		if (!currentUser.isAdmin()) {
			return Response.seeOther(new URI("/")).build();
		}
		
		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", currentUser);
		parameters.put("servers", backend.listActiveBuildServers());
		if (!Strings.isNullOrEmpty(error)) {
			parameters.put("error", error);
		}

		List<Locale> locales = Collections.list(request.getLocales());
		return Response.ok(templateEngine.process("build-servers.ftl", locales, parameters)).build();
	}
	
	@GET
	@Path("setup")
	public Response showNewBuildServerSetupPage(@Context HttpServletRequest request, @QueryParam("error") String error) 
			throws IOException, URISyntaxException {
		
		if (!currentUser.isAdmin()) {
			return Response.seeOther(new URI("/")).build();
		}
		
		List<Locale> locales = Collections.list(request.getLocales());
		
		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", currentUser);
		if (!Strings.isNullOrEmpty(error)) {
			parameters.put("error", error);
		}
		
		return Response.ok(templateEngine.process("build-server-setup.ftl", locales, parameters)).build();
	}
	
	@POST
	@Path("setup")
	public Response addNewBuildServer(@FormParam("name") String name, @FormParam("secret") String secret, @FormParam("host") String host) 
			throws URISyntaxException {
		
		if (!currentUser.isAdmin()) {
			return Response.seeOther(new URI("/")).build();
		}
		
		try {
			BuildServer server = new BuildServer();
			server.setHost(host);
			server.setName(name);
			server.setSecret(secret);
			backend.addBuildServer(server);
			
			return Response.seeOther(new URI("/build-servers")).build();
		}
		catch (ApiError e) {
			String error = UrlEncoded.encodeString(e.getResourceKey());
			return Response.seeOther(new URI("/build-servers/setup?error=" + error)).build();
		}
	}
	
	@POST
	@Path("delete")
	@Transactional
	public Response deleteBuildServer(@FormParam("id") long id) throws URISyntaxException {
		if (!currentUser.isAdmin()) {
			return Response.seeOther(new URI("/")).build();
		}
		
		try {
			backend.deleteBuildServer(id);
			return Response.seeOther(new URI("/build-servers")).build();
		}
		catch (ApiError e) {
			String error = UrlEncoded.encodeString(e.getResourceKey());
			return Response.seeOther(new URI("/build-servers?error=" + error)).build();
		}
	}
	
}
