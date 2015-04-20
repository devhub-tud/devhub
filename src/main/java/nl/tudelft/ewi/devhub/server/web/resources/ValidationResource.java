package nl.tudelft.ewi.devhub.server.web.resources;

import java.net.URISyntaxException;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import nl.tudelft.ewi.devhub.server.database.controllers.Users;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@Path("validation")
@RequestScoped
public class ValidationResource {

	private final Users users;

	@Inject
	public ValidationResource(Users users) {
		this.users = users;
	}

	@GET
	@Path("netID")
	public Response validateNetId(@QueryParam("netID") String netId) throws URISyntaxException {
		try {
			users.findByNetId(netId);
			return Response.ok()
				.build();
		}
		catch (EntityNotFoundException e) {
			return Response.status(Status.NOT_FOUND)
				.build();
		}
	}
}
