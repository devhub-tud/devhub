package nl.tudelft.ewi.devhub.server.web.resources;

import lombok.SneakyThrows;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URLEncoder;

public class Resource {
	
	public static final String UTF8_CHARSET = "; charset=UTF8";

	@SneakyThrows
	public Response redirect(String path) {
		return redirect(new URI(path));
	}

	public Response redirect(URI path) {
		return Response.seeOther(path).build();
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
