package nl.tudelft.ewi.devhub.server.web.errors;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@javax.ws.rs.ext.Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {

	@Context
	private HttpServletRequest request;

	private final TemplateEngine templateEngine;
	private final Provider<User> currentUserProvider;

	@Inject
	public ThrowableMapper(TemplateEngine templateEngine,
			@Named("current.user") Provider<User> currentUserProvider) {
		this.templateEngine = templateEngine;
		this.currentUserProvider = currentUserProvider;
	}

	@Override
	public Response toResponse(Throwable exception) {
		UUID id = UUID.randomUUID();
		log.error(exception.getMessage() + " (" + id + ")", exception);

		List<Locale> locales = Collections.list(request.getLocales());

		try {
			Map<String, Object> params = Maps.newHashMap();
			User user = determineUser();
			if(user != null)
				params.put("user", determineUser());
			params.put("error_id", id);

			return Response.ok()
				.entity(templateEngine.process("error.fatal.ftl", locales, params))
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
