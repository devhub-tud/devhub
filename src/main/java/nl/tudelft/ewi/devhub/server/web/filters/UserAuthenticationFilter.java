package nl.tudelft.ewi.devhub.server.web.filters;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import nl.tudelft.ewi.devhub.server.database.entities.User;

import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.interception.PostMatchContainerRequestContext;

import com.google.common.base.Strings;
import com.google.inject.Provider;

@javax.ws.rs.ext.Provider
public class UserAuthenticationFilter implements ContainerRequestFilter {
	
	@Context
	HttpServletRequest webRequest;
	
	private final Provider<RequestScope> scope;

	@Inject
	UserAuthenticationFilter(Provider<RequestScope> scope) {
		this.scope = scope;
	}
	
	@Override
	public void filter(ContainerRequestContext ctx) throws IOException {
		if (!(ctx instanceof PostMatchContainerRequestContext)) {
			return;
		}
		
		PostMatchContainerRequestContext context = (PostMatchContainerRequestContext) ctx;
		ResourceMethodInvoker invoker = context.getResourceMethod();
		Method method = invoker.getMethod();
		Class<?> resource = method.getDeclaringClass();
		
		try {
			checkUserIsAuthenticated(ctx, method, resource);
		}
		catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}
	
	private void checkUserIsAuthenticated(ContainerRequestContext ctx, Method method, Class<?> resource) throws URISyntaxException, UnsupportedEncodingException {
		RequireAuthenticatedUser annotation = getAnnotation(method, resource, RequireAuthenticatedUser.class);
		if (annotation != null) {
			User user = scope.get().getUser();
			if (user == null) {
				fail(ctx);
			}
		}
	}

	private void fail(ContainerRequestContext ctx) throws URISyntaxException, UnsupportedEncodingException {
		String url = webRequest.getRequestURI().substring(1);
		if (!Strings.isNullOrEmpty(webRequest.getQueryString())) {
			url += "?" + webRequest.getQueryString();
		}
		
		ctx.abortWith(Response.seeOther(new URI("/login?redirect=" + URLEncoder.encode(url, "UTF-8"))).build());
	}
	
	private <T extends Annotation> T getAnnotation(Method method, Class<?> resource, Class<T> annotationClass) {
		T annotation = method.getAnnotation(annotationClass);
		if (annotation == null) {
			annotation = resource.getAnnotation(annotationClass);
		}
		return annotation;
	}
	
}
