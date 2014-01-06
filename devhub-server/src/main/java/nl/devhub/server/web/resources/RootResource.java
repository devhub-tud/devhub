package nl.devhub.server.web.resources;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class RootResource {
	
	@GET
	@Path("favicon.ico")
	public Response getFavicon() throws URISyntaxException {
		return Response.seeOther(new URI("/static/img/favicon.ico")).build();
	}

	@GET
	public Response redirect() throws URISyntaxException {
		return Response.seeOther(new URI("/projects")).build();
	}
	
}
