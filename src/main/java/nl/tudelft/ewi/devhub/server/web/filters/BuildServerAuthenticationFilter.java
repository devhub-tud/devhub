package nl.tudelft.ewi.devhub.server.web.filters;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;

import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.interception.PostMatchContainerRequestContext;
import org.jboss.resteasy.util.Base64;

import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
@javax.ws.rs.ext.Provider
public class BuildServerAuthenticationFilter implements ContainerRequestFilter {
	
	private final Provider<BuildsBackend> backends;

	@Inject
	BuildServerAuthenticationFilter(Provider<BuildsBackend> backends) {
		this.backends = backends;
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
		
		checkBuildServerAuthentication(ctx, method, resource);
	}
	
	private void checkBuildServerAuthentication(ContainerRequestContext ctx, Method method, Class<?> resource) {
		RequireAuthenticatedBuildServer annotation = getAnnotation(method, resource, RequireAuthenticatedBuildServer.class);
		if (annotation != null) {
			String authHeader = ctx.getHeaderString("Authorization");
			if (Strings.isNullOrEmpty(authHeader) || !authHeader.startsWith("Basic ")) {
				fail(ctx, Status.UNAUTHORIZED, "You need to be authorized to access this resource.");
				return; 
			}
			
			String username = null;
			String password = null;
			
			try {
				authHeader = authHeader.substring(6);
				String decodedHeader = new String(Base64.decode(authHeader));
				String[] chunks = decodedHeader.split(":");
				if (chunks.length != 2) {
					throw new IOException();
				}
				
				username = chunks[0];
				password = chunks[1];
			}
			catch (IOException e) {
				fail(ctx, Status.UNAUTHORIZED, "Could not decode your Authorization header.");
				return;
			}
			
			BuildsBackend backend = backends.get();
			if (!backend.authenticate(username, password)) {
				fail(ctx, Status.UNAUTHORIZED, "You have specified invalid credentials.");
			}
		}
	}
	
	private <T extends Annotation> T getAnnotation(Method method, Class<?> resource, Class<T> annotationClass) {
		T annotation = method.getAnnotation(annotationClass);
		if (annotation == null) {
			annotation = resource.getAnnotation(annotationClass);
		}
		return annotation;
	}
	
	private void fail(ContainerRequestContext context, Status status, String message) {
		Response response = Response.status(status)
				.entity(message)
				.build();
		
		context.abortWith(response);
	}
	
}
