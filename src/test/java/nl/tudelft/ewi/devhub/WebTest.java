package nl.tudelft.ewi.devhub;

import nl.tudelft.ewi.devhub.server.DevhubServer;
import nl.tudelft.ewi.devhub.web.LoginView;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class WebTest {

	private static DevhubServer server;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		server = new DevhubServer();
		server.startServer();
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
