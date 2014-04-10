package nl.tudelft.ewi.devhub.server.web.resources;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import nl.tudelft.ewi.devhub.server.backend.LdapBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.filters.RequestScope;
import nl.tudelft.ewi.devhub.server.web.filters.RequireAuthenticatedUser;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import org.jboss.resteasy.plugins.guice.RequestScoped;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

@Path("/")
@RequestScoped
public class RootResource {
	
	private final TemplateEngine engine;
	private final LdapBackend ldapBackend;
	private final Users users;
	private final RequestScope scope;

	@Inject
	public RootResource(TemplateEngine engine, LdapBackend ldapBackend, Users users, RequestScope scope) {
		this.engine = engine;
		this.ldapBackend = ldapBackend;
		this.users = users;
		this.scope = scope;
	}
	
	@GET
	public Response onEntry(@Context HttpServletRequest request) throws URISyntaxException {
		if (scope.getUser() == null) {
			return Response.seeOther(new URI("/login")).build();
		}
		return Response.seeOther(new URI("/projects")).build();
	}
	
	@GET
	@Path("favicon.ico")
	public Response getFavicon() throws URISyntaxException {
		return Response.seeOther(new URI("/static/img/favicon.ico")).build();
	}

	@GET
	@Path("login")
	public String serveLogin(@Context HttpServletRequest request, @QueryParam("error") String error) throws IOException {
		List<Locale> locales = Collections.list(request.getLocales());
		Map<String, Object> parameters = Maps.newHashMap();
		if (!Strings.isNullOrEmpty(error)) {
			parameters.put("error", error);
		}
		
		return engine.process("login.ftl", locales, parameters);
	}

	@GET
	@Path("logout")
	@RequireAuthenticatedUser
	public Response serveLogout(@Context HttpServletRequest request) throws URISyntaxException {
		scope.invalidate();
		return Response.seeOther(new URI("/login")).build();
	}
	
	@POST
	@Path("login")
	public Response handleLogin(@Context HttpServletRequest request, @FormParam("netID") String netId, 
			@FormParam("password") String password, @QueryParam("redirect") String redirectTo) 
			throws URISyntaxException, UnsupportedEncodingException {
		
		if (ldapBackend.authenticate(netId, password)) {
			if (!userExists(netId)) {
				User user = new User();
				user.setNetId(netId);
				user.setAdmin(false);
				users.persist(user);
			}
			
			scope.setUser(netId);
			if (Strings.isNullOrEmpty(redirectTo)) {
				return Response.seeOther(new URI("/projects")).build();
			}
			return Response.seeOther(new URI("/" + URLDecoder.decode(redirectTo, "UTF-8"))).build();
		}
		return Response.seeOther(new URI("/login?error=error.invalid-credentials")).build();
	}
	
	private boolean userExists(String netId) {
		try {
			users.findByNetId(netId);
			return true;
		}
		catch (EntityNotFoundException e) {
			return false;
		}
	}
	
}
