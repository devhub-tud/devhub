package nl.tudelft.ewi.devhub.server.web.errors;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.web.filters.RequestScope;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;

@Slf4j
@javax.ws.rs.ext.Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {
	
	@Context
	private HttpServletRequest request;
	
	private final TemplateEngine templateEngine;
	private final Provider<RequestScope> scopeProvider;

	@Inject
	public UnauthorizedExceptionMapper(TemplateEngine templateEngine, Provider<RequestScope> scopeProvider) {
		this.templateEngine = templateEngine;
		this.scopeProvider = scopeProvider;
	}

	@Override
	public Response toResponse(UnauthorizedException exception) {
		UUID id = UUID.randomUUID();
		log.error(exception.getMessage() + " (" + id + ")", exception);
		
		List<Locale> locales = Collections.list(request.getLocales());
		
		try {
			Map<String, Object> params = Maps.newHashMap();
			params.put("user", scopeProvider.get().getUser());
			params.put("error_id", id);
			
			return Response.ok()
					.entity(templateEngine.process("error.unauthorized.ftl", locales, params))
					.build();
		}
		catch (IOException e) {
			return Response.serverError().entity("If you see this, something is very very wrong...")
					.build();
		}
	}

}
