package nl.tudelft.ewi.devhub.server.web.filters;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityNotFoundException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;

import org.eclipse.jetty.http.HttpStatus;

import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

@Slf4j
@Singleton
public class RepositoryAuthorizeFilter implements Filter {

	private final TemplateEngine templateEngine;
	private final Provider<Groups> groupsProvider;
	private final Provider<Courses> coursesProvider;
	private final Provider<User> currentUserProvider;
	private final Pattern pattern;
	
	@Inject
	public RepositoryAuthorizeFilter(
			final @Named("current.user") Provider<User> currentUserProvider,
			final TemplateEngine templateEngine,
			final Provider<Groups> groupsProvider,
			final Provider<Courses> coursesProvider) {
		this.currentUserProvider = currentUserProvider;
		this.templateEngine = templateEngine;
		this.coursesProvider = coursesProvider;
		this.groupsProvider = groupsProvider;
		this.pattern = Pattern.compile("^/courses/([^/]+)/groups/(\\d+)(/.*)?");
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			doFilter((HttpServletRequest) request,
					(HttpServletResponse) response, chain);
		}
	}
	
	private void doFilter(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		User user = currentUserProvider.get();

		try {
			checkAuthorized(request, user);
			chain.doFilter(request, response);
		} catch (UnauthorizedException e) {
			notAuthorized(request, response, e, user);
		} catch (NotFoundException | EntityNotFoundException e) {
			notFound(request, response, e, user);
		} catch (Throwable t) {
			onException(request, response, t, user);
		}
	}
	
	private void checkAuthorized(HttpServletRequest request, User user) {
		String uri = request.getRequestURI();
		Matcher matcher = pattern.matcher(uri);
		
		if(matcher.matches()) {
			Course course = coursesProvider.get().find(matcher.group(1));
			Group group = groupsProvider.get().find(course, Long.parseLong(matcher.group(2)));
			if (!user.isAdmin() && !user.isAssisting(course) && !user.isMemberOf(group)) {
				throw new UnauthorizedException();
			}

			setAttribute(request, Group.class, Names.named("current.group"), group);
		}
		else {
			throw new IllegalArgumentException("Invalid request");
		}
	}
	
	private <T> void setAttribute(HttpServletRequest request, Class<T> klass, Annotation annotation, T object) {
		request.setAttribute(Key.get(klass, annotation).toString(), object);
	}

	private void notFound(HttpServletRequest request, HttpServletResponse response, Exception exception, User user) throws IOException {
		UUID id = UUID.randomUUID();
		log.warn(exception.getMessage() + " (" + id + ")");

		List<Locale> locales = Collections.list(request.getLocales());

		Map<String, Object> params = Maps.newHashMap();
		params.put("user", user);
		params.put("error_id", id);

		String template = templateEngine.process("error.not-found.ftl", locales, params);
		response.setStatus(HttpStatus.NOT_FOUND_404);
		response.addHeader("Content-Type", "text/html");
		response.addHeader("Content-Length", Integer.toString(template.length()));
		response.getWriter().write(template);
	}

	private void notAuthorized(HttpServletRequest request, HttpServletResponse response, UnauthorizedException exception, User user) throws IOException {
		UUID id = UUID.randomUUID();
		log.error(exception.getMessage() + " (" + id + ")");

		List<Locale> locales = Collections.list(request.getLocales());
		
		Map<String, Object> params = Maps.newHashMap();
		params.put("user", user);
		params.put("error_id", id);
		
		String template = templateEngine.process("error.unauthorized.ftl", locales, params);
		response.setStatus(HttpStatus.FORBIDDEN_403);
		response.addHeader("Content-Type", "text/html");
		response.addHeader("Content-Length", Integer.toString(template.length()));
		response.getWriter().write(template);
	}
	
	private void onException(HttpServletRequest request, HttpServletResponse response, Throwable t, User user) throws IOException {
		UUID id = UUID.randomUUID();
		log.error(t.getMessage() + " (" + id + ")", t);

		List<Locale> locales = Collections.list(request.getLocales());
		
		Map<String, Object> params = Maps.newHashMap();
		params.put("user", user);
		params.put("error_id", id);
		
		String template = templateEngine.process("error.fatal.ftl", locales, params);
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
		response.addHeader("Content-Type", "text/html");
		response.addHeader("Content-Length", Integer.toString(template.length()));
		response.getWriter().write(template);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
