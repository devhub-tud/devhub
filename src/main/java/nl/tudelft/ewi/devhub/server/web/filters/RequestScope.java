package nl.tudelft.ewi.devhub.server.web.filters;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import org.jboss.resteasy.plugins.guice.RequestScoped;

import com.google.common.base.Strings;
import com.google.inject.Inject;

@RequestScoped
public class RequestScope {

	private final Users users;
	private final HttpServletRequest request;

	@Inject
	public RequestScope(HttpServletRequest request, Users users) {
		this.request = request;
		this.users = users;
	}

	public User getUser() {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}
		
		String netId = (String) session.getAttribute("netID");
		if (Strings.isNullOrEmpty(netId)) {
			return null;
		}
		
		try {
			return users.findByNetId(netId);
		}
		catch (EntityNotFoundException e) {
			return null;
		}
	}
	
	public void setUser(String netId) {
		HttpSession session = request.getSession(true);
		session.setAttribute("netID", netId);
	}
	
	public void invalidate() {
		request.getSession(true).invalidate();
	}
	
	public boolean isAdmin() {
		User user = getUser();
		if (user == null) {
			return false;
		}
		return user.isAdmin();
	}

}
