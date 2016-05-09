package nl.tudelft.ewi.devhub.webtests.utils;

import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.webtests.rules.DriverResource;
import nl.tudelft.ewi.devhub.webtests.rules.ServerResource;
import nl.tudelft.ewi.devhub.webtests.views.AuthenticatedView;
import nl.tudelft.ewi.devhub.webtests.views.LoginView;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.openqa.selenium.WebDriver;

import com.google.common.base.Predicate;

import javax.inject.Inject;

public abstract class WebTest {

	public static final String NET_ID = "student1";
	public static final String PASSWORD = "student1";

	public static ServerResource serverResource = new ServerResource();
	public static DriverResource driverResource = new DriverResource();
	@ClassRule public static RuleChain ruleChain = RuleChain.outerRule(serverResource).around(driverResource);

	@Inject private Config config;

	@Before
	public void setUp() {
		serverResource.getServer().getInjector().injectMembers(this);
	}
	
	public LoginView openLoginScreen() {
		return LoginView.create(getDriver(), "http://localhost:"  + config.getHttpPort());
	}
	
	public WebDriver getDriver() {
		return driverResource.getDriver();
	}

	@After
	public void logout() {
		new AuthenticatedView(getDriver()).logout();
	}
	
	protected void waitForCondition(int timeOutInSeconds, Predicate<WebDriver> condition) {
		Dom.waitForCondition(getDriver(), timeOutInSeconds, condition);		
	}
}