package nl.devhub.server.web.pages;

import lombok.Data;
import lombok.experimental.Accessors;

import org.eclipse.jetty.server.Authentication.User;

@Data
@Accessors(chain = true)
public class Page {

	private User user;
	
}
