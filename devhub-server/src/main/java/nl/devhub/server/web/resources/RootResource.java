package nl.devhub.server.web.resources;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import lombok.SneakyThrows;

@Path("/")
public class RootResource {

	@GET
	@SneakyThrows
	public Response redirect() {
		return Response.seeOther(new URI("/projects/TI2210/group/12")).build();
	}
	
}
