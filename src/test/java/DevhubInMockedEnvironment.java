import nl.tudelft.ewi.devhub.modules.MockedGitoliteGitServerModule;
import nl.tudelft.ewi.devhub.server.DevhubServer;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationBackend;
import nl.tudelft.ewi.devhub.server.backend.Bootstrapper;
import nl.tudelft.ewi.devhub.server.backend.MockedAuthenticationBackend;
import nl.tudelft.ewi.devhub.server.backend.MockedMailBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.MailBackend;

import com.google.inject.AbstractModule;
import nl.tudelft.ewi.git.web.CucumberModule;


public class DevhubInMockedEnvironment {

	public static void main(String[] args) throws Exception {
		final DevhubServer server = new DevhubServer(new AbstractModule() {
			@Override
			protected void configure() {
				install(new MockedGitoliteGitServerModule());
				bind(AuthenticationBackend.class).to(MockedAuthenticationBackend.class);
//				bind(AuthenticationProvider.class).to(BasicAuthenticationProvider.class);

				bind(MailBackend.class).to(MockedMailBackend.class);
			}
		});
		
		server.startServer();
		server.getInstance(Bootstrapper.class).prepare("/simple-environment.json");
		server.joinThread();
	}

}
