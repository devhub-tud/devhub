package nl.tudelft.ewi.devhub.server.web.resources;

import java.net.URI;
import java.net.URLEncoder;

import javax.ws.rs.core.Response;

import lombok.SneakyThrows;

public class Resource {
	
	public static final String UTF8_CHARSET = "; charset=UTF8";

	@SneakyThrows
	public Response redirect(String path) {
		return Response.seeOther(new URI(path))
			.build();
	}

	public Response display(String html) {
		return Response.ok(html)
			.build();
	}

	@SneakyThrows
	String encode(String value) {
		return URLEncoder.encode(value, "UTF-8");
	}
	
}
