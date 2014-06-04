package nl.tudelft.ewi.devhub.webtests.utils;

import com.google.inject.AbstractModule;

import nl.tudelft.ewi.build.client.BuildServerBackend;
import nl.tudelft.ewi.build.client.MockedBuildServerBackend;
import nl.tudelft.ewi.devhub.server.DevhubServer;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationBackend;
import nl.tudelft.ewi.devhub.server.backend.Bootstrapper;
import nl.tudelft.ewi.devhub.server.backend.MockedAuthenticationBackend;
import nl.tudelft.ewi.devhub.webtests.views.LoginView;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.GitServerClientMock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public abstract class WebTest {
	
	public static final String NET_ID = "student1";
	public static final String PASSWORD = "student1";

	private static DevhubServer server;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		server = new DevhubServer(new AbstractModule() {
			@Override
			protected void configure() {
				bind(AuthenticationBackend.class).to(MockedAuthenticationBackend.class);
				bind(GitServerClient.class).to(GitServerClientMock.class);
				bind(GitServerClientMock.class).toInstance(new GitServerClientMock());
				bind(BuildServerBackend.class).to(MockedBuildServerBackend.class);
				bind(MockedBuildServerBackend.class).toInstance(new MockedBuildServerBackend(null, null));
			}
		});
		server.startServer();

		server.getInstance(Bootstrapper.class).prepare("/simple-environment.json");
	}
	
	private WebDriver driver;
	
	@Before
	public void setUp() {
		this.driver = new FirefoxDriver();
		driver.manage().window().maximize();
	}
	
	public LoginView openLoginScreen() {
		return LoginView.create(driver, "http://localhost:8080");
	}
	
	public WebDriver getDriver() {
		return driver;
	}
	
	protected static GitServerClient getGitServerClient() {
		return server.getInstance(GitServerClient.class);
	}
	
	@After
	public void tearDown() {
		driver.close();
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		server.stopServer();
	}
	
}