package nl.devhub.server.web.resources;

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
import nl.devhub.server.database.controllers.Users;
import nl.devhub.server.database.entities.User;
import nl.devhub.server.web.templating.TemplateEngine;

import com.google.common.collect.Maps;

@Path("accounts")
@Produces(MediaType.TEXT_HTML)
public class AccountsResource {

	private static final int USER_ID = 1;

	private final TemplateEngine templateEngine;
	private final Users users;

	@Inject
	public AccountsResource(TemplateEngine templateEngine, Users users) {
		this.templateEngine = templateEngine;
		this.users = users;
	}

	@GET
	@SneakyThrows
	public Response redirectToProjectDashboard() {
		return Response.seeOther(new URI("/accounts/" + USER_ID)).build();
	}

	@GET
	@Path("{userId}")
	@SneakyThrows
	public String showAccountProfile(@PathParam("userId") long userId, @Context HttpServletRequest request) {
		User user = users.find(USER_ID);
		
		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", user);
		parameters.put("displayMenu", "account");

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("account.ftl", locales, parameters);
	}
	
	@GET
	@Path("{userId}/ssh-keys")
	@SneakyThrows
	public String showAccountSshKeys(@PathParam("userId") long userId, @Context HttpServletRequest request) {
		User user = users.find(USER_ID);
		
		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", user);
		parameters.put("displayMenu", "account");
		
		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("account-sshkeys.ftl", locales, parameters);
	}

}
