package nl.tudelft.ewi.devhub.server.web;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {@Override
	
	public Response toResponse(NotFoundException exception) {
		log.warn("Failed attempt to request: " + exception.getMessage());
		return Response.status(Status.NOT_FOUND).build();
	}

}
