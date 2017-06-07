package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.devhub.server.backend.SshKeyBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import org.eclipse.jetty.util.UrlEncoded;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
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
@Path("notifications")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class NotificationResource extends Resource {

	private final TemplateEngine templateEngine;
	private final SshKeyBackend backend;
	private final User currentUser;
	private final Users users;

	@Inject
    NotificationResource(TemplateEngine templateEngine, SshKeyBackend backend, @Named("current.user") User currentUser, Users users) {
		this.templateEngine = templateEngine;
		this.backend = backend;
		this.currentUser = currentUser;
		this.users = users;
	}


	@GET
	public String showUserPage(@Context HttpServletRequest request)
			throws IOException, ApiError {


		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", currentUser);
		parameters.put("path", request.getRequestURI());
		parameters.put("notifications",currentUser.getNotifications());

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("notifications.ftl", locales, parameters);
	}
}
