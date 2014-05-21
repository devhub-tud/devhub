package nl.tudelft.ewi.devhub;

import com.google.inject.AbstractModule;
import nl.tudelft.ewi.devhub.server.DevhubServer;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationBackend;
import nl.tudelft.ewi.devhub.web.LoginView;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class WebTest {
	
	public static final String NET_ID = "test-student";
	public static final String PASSWORD = "test-pw";

	private static DevhubServer server;
	private static MockedAuthenticationBackend authBackend;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		server = new DevhubServer(new AbstractModule() {
			@Override
			protected void configure() {
				bind(AuthenticationBackend.class).to(MockedAuthenticationBackend.class);
			}
		});
		server.startServer();
		
		authBackend = server.getInstance(MockedAuthenticationBackend.class);
		authBackend.addUser(NET_ID, PASSWORD);
	}
	
	private LoginView session;
	
	@Before
	public void setUp() {
		session = LoginView.create("http://localhost:8080");
	}
	
	public LoginView getSession() {
		return session;
	}
	
	@After
	public void tearDown() {
		session.terminateBrowser();
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		server.stopServer();
	}
	
}
