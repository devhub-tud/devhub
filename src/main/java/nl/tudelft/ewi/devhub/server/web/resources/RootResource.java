package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationBackend;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.util.Version;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import org.apache.directory.api.ldap.model.exception.LdapException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
@Path("/")
@RequestScoped
public class RootResource extends Resource {
	
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
			@FormParam("password") String password, @QueryParam("redirect") @DefaultValue("courses") String redirectTo)
			throws URISyntaxException, LdapException, IOException {
		
		try {
			if (authenticationBackend.authenticate(netId, password)) {
				request.getSession().setAttribute("netID", netId);

				User currentUser = currentUserProvider.get();
				if (Strings.isNullOrEmpty(currentUser.getStudentNumber()) &&
					!currentUser.getGroups().isEmpty()) {
					return Response.seeOther(new URI(StudyNumberResource.STUDY_NUMBER_PATH)).build();
				}

				return Response.seeOther(new URI("/" + redirectTo)).build();
			}
		}
		catch (IllegalArgumentException e) {
			log.debug(String.format("Failed to login %s with %s", netId, password));
		}

		return Response.seeOther(new URI("/login?error=error.invalid-credentials")).build();

	}

    @GET
    @Path("version")
    @Produces(MediaType.APPLICATION_JSON)
    @SneakyThrows
    public Version handleVersion() {
        Properties properties = new Properties();
        try (InputStream inputStream = RootResource.class.getResourceAsStream("/devhub.git.properties")) {
            properties.load(inputStream);
        }

        return Version.of(properties.getProperty("git.build.version"), properties.getProperty("git.commit.id"),
                properties.getProperty("git.closest.tag.name"));
    }

}
