package nl.tudelft.ewi.devhub.webtests.rules;

import com.google.inject.AbstractModule;
import nl.tudelft.ewi.build.client.BuildServerBackend;
import nl.tudelft.ewi.build.client.MockedBuildServerBackend;
import nl.tudelft.ewi.devhub.modules.MockedGitoliteGitServerModule;
import nl.tudelft.ewi.devhub.server.DevhubServer;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationBackend;
import nl.tudelft.ewi.devhub.server.backend.Bootstrapper;
import nl.tudelft.ewi.devhub.server.backend.MockedAuthenticationBackend;
import nl.tudelft.ewi.devhub.server.backend.MockedMailBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.MailBackend;
import org.junit.rules.ExternalResource;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class ServerResource extends ExternalResource {

	private final AtomicReference<DevhubServer> serverRef = new AtomicReference<>();

	@Override
	protected void before() throws Throwable {
		final DevhubServer server = new DevhubServer(new AbstractModule() {

			@Override
			protected void configure() {
				install(new MockedGitoliteGitServerModule());
				bind(AuthenticationBackend.class).to(MockedAuthenticationBackend.class);
				bind(BuildServerBackend.class).to(MockedBuildServerBackend.class);
				bind(MockedBuildServerBackend.class).toInstance(new MockedBuildServerBackend(null, null));
				bind(MailBackend.class).to(MockedMailBackend.class);
			}
		});
		server.startServer();

		server.getInstance(Bootstrapper.class).prepare("/simple-environment.json");
		serverRef.set(server);
	}

	@Override
	protected void after() {
		getServer().stopServer();
	}

	public DevhubServer getServer() {
		return serverRef.get();
	}

}
