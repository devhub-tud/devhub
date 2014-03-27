package nl.tudelft.ewi.devhub.server.web.resources;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.models.UserModel;

import com.google.common.collect.Maps;

@Path("account")
@Produces(MediaType.TEXT_HTML)
public class AccountResource {

	private static final int USER_ID = 1;

	private final TemplateEngine templateEngine;
	private final GitServerClient client;
	private final Users users;

	@Inject
	public AccountResource(TemplateEngine templateEngine, Users users, GitServerClient client) {
		this.templateEngine = templateEngine;
		this.users = users;
		this.client = client;
	}

	@GET
	public String showUserPage(@Context HttpServletRequest request) throws IOException {
		User requester = users.find(USER_ID);

		UserModel userModel = client.users().ensureExists(requester.getNetId());

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", requester);
		parameters.put("keys", userModel.getKeys());

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("account.ftl", locales, parameters);
	}

}
