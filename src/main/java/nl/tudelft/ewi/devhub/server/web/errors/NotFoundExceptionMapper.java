package nl.tudelft.ewi.devhub.server.web.errors;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

	@Context
	private HttpServletRequest request;

	private final TemplateEngine templateEngine;
	private final com.google.inject.Provider<User> currentUserProvider;

	@Inject
	public NotFoundExceptionMapper(TemplateEngine templateEngine,
								   @Named("current.user") com.google.inject.Provider<User> currentUserProvider) {
		this.templateEngine = templateEngine;
		this.currentUserProvider = currentUserProvider;
	}

	@Override
	public Response toResponse(NotFoundException exception) {
		UUID id = UUID.randomUUID();
		log.warn(
			String.format(
				"Resource was not found for method %s at %s, failed with: %s (%s)",
				request.getMethod(),
				request.getRequestURL(),
				exception.getMessage(),
				id
			),
			exception
		);

		List<Locale> locales = Collections.list(request.getLocales());

		try {
			Map<String, Object> params = Maps.newHashMap();
			User user = determineUser();
			if(user != null)
				params.put("user", determineUser());
			params.put("error_id", id);

			return Response
					.status(Response.Status.NOT_FOUND)
					.entity(templateEngine.process("error.not-found.ftl", locales, params))
					.build();
		}
		catch (IOException e) {
			return Response.serverError()
					.entity("If you see this, something is very very wrong...")
					.build();
		}
	}

	private User determineUser() {
		try {
			return currentUserProvider.get();
		}
		catch (Throwable e) {
			return null;
		}
	}

}
