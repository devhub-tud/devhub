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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.tudelft.ewi.devhub.server.backend.SshKeyBackend;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.filters.RequestScope;
import nl.tudelft.ewi.devhub.server.web.filters.RequireAuthenticatedUser;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import org.eclipse.jetty.util.UrlEncoded;
import org.jboss.resteasy.plugins.guice.RequestScoped;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

@RequestScoped
@Path("account")
@Produces(MediaType.TEXT_HTML)
@RequireAuthenticatedUser
public class AccountResource {

	private final TemplateEngine templateEngine;
	private final SshKeyBackend backend;
	private final RequestScope scope;

	@Inject
	AccountResource(TemplateEngine templateEngine, SshKeyBackend backend, RequestScope scope) {
		this.templateEngine = templateEngine;
		this.backend = backend;
		this.scope = scope;
	}

	@GET
	public String showUserPage(@Context HttpServletRequest request) throws IOException {
		User requester = scope.getUser();
		
		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", requester);
		parameters.put("keys", backend.listKeys(requester));

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("account.ftl", locales, parameters);
	}
	
	@GET
	@Path("setup")
	public String showNewSshKeyPage(@Context HttpServletRequest request, @QueryParam("error") String error) 
			throws IOException {
		
		List<Locale> locales = Collections.list(request.getLocales());
		
		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", scope.getUser());
		if (!Strings.isNullOrEmpty(error)) {
			parameters.put("error", error);
		}
		
		return templateEngine.process("account-setup.ftl", locales, parameters);
	}
	
	@POST
	@Path("setup")
	public Response addNewKey(@FormParam("name") String name, @FormParam("contents") String contents) 
			throws URISyntaxException {
		
		try {
			backend.createNewSshKey(scope.getUser(), name, contents);
			return Response.seeOther(new URI("/account")).build();
		}
		catch (ApiError e) {
			String error = UrlEncoded.encodeString(e.getResourceKey());
			return Response.seeOther(new URI("/account/setup?error=" + error)).build();
		}
	}
	
	@POST
	@Path("delete")
	public Response deleteExistingKey(@FormParam("name") String name) throws URISyntaxException {
		try {
			backend.deleteSshKey(scope.getUser(), name);
			return Response.seeOther(new URI("/account")).build();
		}
		catch (ApiError e) {
			String error = UrlEncoded.encodeString(e.getResourceKey());
			return Response.seeOther(new URI("/account?error=" + error)).build();
		}
	}
	
}
