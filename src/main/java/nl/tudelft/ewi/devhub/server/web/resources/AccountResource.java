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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.tudelft.ewi.devhub.server.backend.SshKeyBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import org.eclipse.jetty.util.UrlEncoded;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
@Path("accounts")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class AccountResource extends Resource {

	private final TemplateEngine templateEngine;
	private final SshKeyBackend backend;
	private final User currentUser;
	private final Users users;

	@Inject
	AccountResource(TemplateEngine templateEngine, SshKeyBackend backend, @Named("current.user") User currentUser, Users users) {
		this.templateEngine = templateEngine;
		this.backend = backend;
		this.currentUser = currentUser;
		this.users = users;
	}

	@GET
	public Response showPersonalUserPage() throws URISyntaxException {
		return Response.seeOther(new URI("/accounts/" + currentUser.getNetId()))
			.build();
	}

	@GET
	@Path("{netId}")
	public String showUserPage(@Context HttpServletRequest request, @PathParam("netId") String netId)
			throws IOException, ApiError {

		User account = users.findByNetId(netId);

		if (!currentUser.isAdmin() && !currentUser.equals(account)) {
			throw new UnauthorizedException();
		}

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", currentUser);
		parameters.put("path", request.getRequestURI());
		parameters.put("keys", backend.listKeys(account));

		List<Locale> locales = Collections.list(request.getLocales());
		return templateEngine.process("account.ftl", locales, parameters);
	}

	@GET
	@Path("{netId}/setup")
	public String showNewSshKeyPage(@Context HttpServletRequest request, @PathParam("netId") String netId,
			@QueryParam("error") String error) throws IOException {

		List<Locale> locales = Collections.list(request.getLocales());

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", currentUser);
		if (!Strings.isNullOrEmpty(error)) {
			parameters.put("error", error);
		}

		return templateEngine.process("account-setup.ftl", locales, parameters);
	}

	@POST
	@Path("{netId}/setup")
	public Response addNewKey(@PathParam("netId") String netId, @FormParam("name") String name,
			@FormParam("contents") String contents) throws URISyntaxException {

		User account = users.findByNetId(netId);

		if (!currentUser.isAdmin() && !currentUser.equals(account)) {
			throw new UnauthorizedException();
		}

		try {
			backend.createNewSshKey(account, name, contents);
			return Response.seeOther(new URI("/accounts/" + netId))
				.build();
		}
		catch (ApiError e) {
			String error = UrlEncoded.encodeString(e.getResourceKey());
			return Response.seeOther(new URI("/accounts/" + netId + "/setup?error=" + error))
				.build();
		}
	}

	@POST
	@Path("{netId}/delete")
	public Response deleteExistingKey(@PathParam("netId") String netId, @FormParam("name") String name)
			throws URISyntaxException {

		User account = users.findByNetId(netId);

		if (!currentUser.isAdmin() && !currentUser.equals(account)) {
			throw new UnauthorizedException();
		}

		try {
			backend.deleteSshKey(account, name);
			return Response.seeOther(new URI("/accounts/" + netId))
				.build();
		}
		catch (ApiError e) {
			String error = UrlEncoded.encodeString(e.getResourceKey());
			return Response.seeOther(new URI("/accounts/" + netId + "?error=" + error))
				.build();
		}
	}

}
