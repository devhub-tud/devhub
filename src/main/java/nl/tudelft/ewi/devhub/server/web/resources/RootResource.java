package nl.tudelft.ewi.devhub.server.web.resources;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationBackend;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;

import org.apache.directory.api.ldap.model.exception.LdapException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Path("/")
@RequestScoped
public class RootResource {
	
	private final TemplateEngine engine;
	private final AuthenticationBackend authenticationBackend;
	private final Provider<User> currentUserProvider;

	@Inject
	public RootResource(TemplateEngine engine,
			AuthenticationBackend authenticationBackend,
			@Named("current.user") Provider<User> currentUserProvider) {
		this.engine = engine;
		this.authenticationBackend = authenticationBackend;
		this.currentUserProvider = currentUserProvider;
	}
	
	@GET
	public Response onEntry(@Context HttpServletRequest request) throws URISyntaxException {
		try {
			currentUserProvider.get();
			return Response.seeOther(new URI("/courses")).build();
		}
		catch (Exception e) {
			return Response.seeOther(new URI("/login")).build();
		}
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
	public Response serveLogout(@Context HttpServletRequest request) throws URISyntaxException {
		request.getSession().invalidate();
		return Response.seeOther(new URI("/login")).build();
	}
	
	@POST
	@Path("login")
	public Response handleLogin(@Context HttpServletRequest request, @FormParam("netID") String netId, 
			@FormParam("password") String password, @QueryParam("redirect") String redirectTo) 
			throws URISyntaxException, LdapException, IOException {
		
		try {
			if (authenticationBackend.authenticate(netId, password)) {
				request.getSession().setAttribute("netID", netId);
				if (Strings.isNullOrEmpty(redirectTo)) {
					return Response.seeOther(new URI("/courses")).build();
				}
				return Response.seeOther(new URI("/" + URLDecoder.decode(redirectTo, "UTF-8"))).build();
			}
		}
		catch (IllegalArgumentException e) {
			log.debug(String.format("Failed to login %s with %s", netId, password));
		}

		return Response.seeOther(new URI("/login?error=error.invalid-credentials")).build();

	}
	
}
