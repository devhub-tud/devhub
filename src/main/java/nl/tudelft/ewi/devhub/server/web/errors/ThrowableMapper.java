package nl.tudelft.ewi.devhub.server.web.errors;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import com.google.inject.Inject;

@Slf4j
@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {
	
	@Context
	private HttpServletRequest request;
	
	private final TemplateEngine templateEngine;

	@Inject
	public ThrowableMapper(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	@Override
	public Response toResponse(Throwable exception) {
		log.error(exception.getMessage(), exception);
		
		List<Locale> locales = Collections.list(request.getLocales());
		
		try {
			return Response.ok()
					.entity(templateEngine.process("error-500.tpl", locales))
					.build();
		}
		catch (IOException e) {
			return Response.serverError().entity("Some server exploded. You might want to try again later.")
					.build();
		}
	}

}
