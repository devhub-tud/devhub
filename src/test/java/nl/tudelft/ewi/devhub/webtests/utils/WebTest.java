package nl.tudelft.ewi.devhub.webtests.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import com.google.inject.AbstractModule;

import nl.tudelft.ewi.build.client.BuildServerBackend;
import nl.tudelft.ewi.build.client.MockedBuildServerBackend;
import nl.tudelft.ewi.devhub.server.DevhubServer;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationBackend;
import nl.tudelft.ewi.devhub.server.backend.Bootstrapper;
import nl.tudelft.ewi.devhub.server.backend.MailBackend;
import nl.tudelft.ewi.devhub.server.backend.MockedAuthenticationBackend;
import nl.tudelft.ewi.devhub.server.backend.MockedMailBackend;
import nl.tudelft.ewi.devhub.webtests.views.LoginView;
import nl.tudelft.ewi.git.client.*;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mockito;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import static org.mockito.Mockito.*;

public abstract class WebTest {

	public static final String NET_ID = "student1";
	public static final String PASSWORD = "student1";

	protected static DevhubServer server;
	protected static GitServerClientMock gitServerClientMock;

	@BeforeClass
	public static void beforeClass() throws Exception {
		gitServerClientMock = new GitServerClientMock();

		server = new DevhubServer(new AbstractModule() {
			@Override
			protected void configure() {
				bind(AuthenticationBackend.class).to(MockedAuthenticationBackend.class);
				bind(GitServerClient.class).toInstance(gitServerClientMock);
				bind(BuildServerBackend.class).to(MockedBuildServerBackend.class);
				bind(MockedBuildServerBackend.class).toInstance(new MockedBuildServerBackend(null, null));
				bind(MailBackend.class).to(MockedMailBackend.class);
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
	
	protected static GitServerClientMock getGitServerClient() {
		return gitServerClientMock;
	}
	
	@After
	public void tearDown() {
		driver.close();
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		server.stopServer();
	}
	
	@Rule
	public TestWatcher watchman = new TestWatcher() {

		@Override
		protected void failed(Throwable e, Description description) {
			try {
				long now = System.currentTimeMillis();
				createScreenshot(e, description, now);
				writeStacktrace(e, description, now);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		private void createScreenshot(Throwable e, Description description, long now) throws IOException {
			File input = ((FirefoxDriver) driver).getScreenshotAs(OutputType.FILE);
			String filename = String.format("Screenshot-%s-%s.jpg", now, description.getMethodName());
			File output = new File(filename);
			FileUtils.copyFile(input, output);			
		}
		
		private void writeStacktrace(Throwable e, Description description, long now) throws FileNotFoundException {
			String filename = String.format("Stacktrace-%s-%s.txt", now, description.getMethodName());
			e.printStackTrace(new PrintStream(filename));
		}
		
	};
	
}