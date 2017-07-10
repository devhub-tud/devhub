package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.devhub.server.backend.NotificationBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.NotificationController;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.Notification;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@RequestScoped
@Path("notifications")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class NotificationResource extends Resource {

	private final TemplateEngine templateEngine;
	private final NotificationBackend backend;
	private final NotificationController notificationController;
	private final User currentUser;
	private final Users users;

	@Inject
    NotificationResource(TemplateEngine templateEngine, NotificationBackend backend, NotificationController notificationController, @Named("current.user") User currentUser, Users users) {

		this.templateEngine = templateEngine;
		this.backend = backend;
		this.currentUser = currentUser;
		this.users = users;
		this.notificationController = notificationController;
	}

	@GET
	public Response showPersonalUserPage() throws URISyntaxException {
		return Response.seeOther(new URI("/notifications/" + currentUser.getNetId()))
				.build();
	}

	@GET
	@Path("{netId}")
	public String showUserPage(@Context HttpServletRequest request, @PathParam("netId") String netId)
			throws IOException, ApiError {


		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", currentUser);
		parameters.put("path", request.getRequestURI());
		parameters.put("notifications", notificationController.getLatestNotificationsFor(currentUser));

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("notifications.ftl", locales, parameters);
	}

	@POST
	@Deprecated
	@Path("{netId}/markRead")
	public Response markAsRead(@PathParam("netId") String netId, @FormParam("notificationId") long notificationId)
			throws IOException, ApiError, URISyntaxException {
		return markAsRead(notificationId);
	}

	@POST
	@Path("markRead")
	@com.google.inject.persist.Transactional
	public Response markAsRead(@FormParam("notificationId") long notificationId)
			throws IOException, ApiError, URISyntaxException {
		try {
			Notification notification = notificationController.findById(notificationId).get();
			notification.setRead(currentUser);
			return Response.seeOther(new URI("/notifications/" + currentUser.getNetId()))
					.build();

		}
		catch (NoSuchElementException e) {
			return Response.seeOther(new URI("/notifications/" + currentUser.getNetId() + "?error=NotificationNotFound")).build();
		}
	}

}
