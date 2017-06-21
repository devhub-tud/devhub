package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.devhub.server.backend.NotificationBackend;
import nl.tudelft.ewi.devhub.server.backend.SshKeyBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.Notification;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.NotificationsToUsers;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
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
	private final NotificationBackend backend;
	private final User currentUser;
	private final Users users;

	@Inject
    NotificationResource(TemplateEngine templateEngine, NotificationBackend backend, @Named("current.user") User currentUser, Users users) {

		this.templateEngine = templateEngine;
		this.backend = backend;
		this.currentUser = currentUser;
		this.users = users;
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
		parameters.put("notifications",currentUser.getNotificationsToUsersList());

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("notifications.ftl", locales, parameters);
	}

	@POST
	@Path("{netId}/markRead")
	@com.google.inject.persist.Transactional
	public Response markAsRead(@PathParam("netId") String netId, @FormParam("notificationId") long notificationId)
			throws IOException, ApiError, URISyntaxException {
		User account = users.findByNetId(netId);
		if(!currentUser.equals(account)) {
			throw new UnauthorizedException();
		}
		boolean setRead = setRead(currentUser.getNotificationsToUsersList(),notificationId);
		if(setRead) {
			return Response.seeOther(new URI("/notifications/" + netId))
					.build();
		}
		return Response.seeOther(new URI("/notifications/" + netId + "?error=NotificationNotFound")).build();

	}


	@com.google.inject.persist.Transactional
	private boolean setRead(List<NotificationsToUsers> notificationList, long notificationId) {

		for(NotificationsToUsers notificationsToUsers: notificationList) {
			if(notificationsToUsers.equalsNotificationId(notificationId)) {
				notificationsToUsers.setRead(true);
				return true;
			}
		}
		return false;
	}
}
