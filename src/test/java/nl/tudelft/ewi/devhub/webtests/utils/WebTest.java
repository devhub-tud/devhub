package nl.tudelft.ewi.devhub.webtests.utils;

import nl.tudelft.ewi.devhub.webtests.views.LoginView;
import nl.tudelft.ewi.devhub.server.backend.MockedAuthenticationBackend;

import com.google.inject.AbstractModule;

import nl.tudelft.ewi.build.client.BuildServerBackend;
import nl.tudelft.ewi.build.client.MockedBuildServerBackend;
import nl.tudelft.ewi.devhub.server.DevhubServer;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationBackend;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.GitServerClientMock;
import nl.tudelft.ewi.git.models.UserModel;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public abstract class WebTest {
	
	public static final String NET_ID = "test-student";
	public static final String USER_EMAIL = NET_ID + "@student.tudelft.nl";
	public static final String PASSWORD = "test-pw";

	private static DevhubServer server;
	private static MockedAuthenticationBackend authBackend;
	private static GitServerClientMock gitServerClient;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		gitServerClient = new GitServerClientMock();
		
		server = new DevhubServer(new AbstractModule() {
			@Override
			protected void configure() {
				bind(AuthenticationBackend.class).to(MockedAuthenticationBackend.class);
				bind(GitServerClient.class).toInstance(gitServerClient);
				bind(BuildServerBackend.class).toInstance(new MockedBuildServerBackend(null, null));
			}
		});
		server.startServer();
		
		authBackend = server.getInstance(MockedAuthenticationBackend.class);
		authBackend.addUser(NET_ID, PASSWORD, false);
		
		UserModel newUser = new UserModel();
		newUser.setName(NET_ID);
		gitServerClient.users().create(newUser);
		
	}
	
	protected static DevhubServer getServer() {
		return server;
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
	
	public static GitServerClientMock getGitServerClient() {
		return gitServerClient;
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
