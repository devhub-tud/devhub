import nl.tudelft.ewi.devhub.server.DevhubServer;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationBackend;
import nl.tudelft.ewi.devhub.server.backend.Bootstrapper;
import nl.tudelft.ewi.devhub.server.backend.mail.MailBackend;
import nl.tudelft.ewi.devhub.server.backend.MockedAuthenticationBackend;
import nl.tudelft.ewi.devhub.server.backend.MockedMailBackend;

import com.google.inject.AbstractModule;


public class DevhubInMockedEnvironment {

	public static void main(String[] args) throws Exception {
		final DevhubServer server = new DevhubServer(new AbstractModule() {
			@Override
			protected void configure() {
				bind(AuthenticationBackend.class).to(MockedAuthenticationBackend.class);
				bind(MailBackend.class).to(MockedMailBackend.class);
			}
		});
		
		server.startServer();
		server.getInstance(Bootstrapper.class).prepare("/simple-environment.json");
		server.joinThread();
	}

}
