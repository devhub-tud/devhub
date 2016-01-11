package nl.tudelft.ewi.devhub.server.web.filters;

import nl.tudelft.ewi.devhub.server.database.entities.User;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Singleton
public class UserAuthorizeFilter implements Filter {

	private final Provider<User> currentUserProvider;
	
	@Inject
	public UserAuthorizeFilter(final @Named("current.user") Provider<User> currentUserProvider) {
		this.currentUserProvider = currentUserProvider;
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if(request instanceof HttpServletRequest) {
			doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
		}
	}
	
	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			currentUserProvider.get();
			chain.doFilter(request, response);
		}
		catch (Exception e) {
			notLoggedIn(request, response);
		}
	}
	
	public void notLoggedIn(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, IOException {
		response.sendRedirect("/login?redirect=" + URLEncoder.encode(request.getRequestURI(), "UTF-8"));
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
