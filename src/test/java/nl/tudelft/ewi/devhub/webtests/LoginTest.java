package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.LoginView;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LoginTest extends WebTest {

	/**
	 * <h1>Scenario: A successful login attempt.</h1>
	 * 
	 * Given that:
	 * <ol>
	 *   <li>I am on the login page.</li>
	 *   <li>I fill in a netID of an existing user in the username field.</li>
	 *   <li>I fill in a valid password belonging to that user.</li>
	 * </ol>
	 * When:
	 * <ol>
	 *   <li>I click on the login button.</li>
	 * </ol>
	 * Then:
	 * <ol>
	 *   <li>I am redirected to the projects page for the specified user.</li>
	 * </ol>
	 */
	@Test
	public void testThatValidUserCanLogin() {
		openLoginScreen().login(NET_ID, PASSWORD).logout();
	}

	/**
	 * <h1>Scenario: A successful login attempt.</h1>
	 * 
	 * Given that:
	 * <ol>
	 *   <li>I am on the login page.</li>
	 *   <li>I fill in a netID of an existing user in the username field.</li>
	 *   <li>I fill in an invalid password belonging to that user.</li>
	 * </ol>
	 * When:
	 * <ol>
	 *   <li>I click on the login button.</li>
	 * </ol>
	 * Then:
	 * <ol>
	 *   <li>I am not redirected to the projects page for the specified user.</li>
	 *   <li>An alert is displayed telling me I filled in invalid credentials.</li>
	 * </ol>
	 */
	@Test
	public void testThatInvalidUserCannotLogin() {
		LoginView loginView = openLoginScreen();
		String url = getDriver().getCurrentUrl();

		loginView.setUsernameField(NET_ID)
				.setPasswordField(PASSWORD + "!")
				.clickLoginButton();
		
		loginView.waitUntilCurrentUrlDiffersFrom(url);
		assertEquals(1, loginView.listAlerts().size());
	}

	@Override
	public void logout() {
		// No-op, tests logout them selves or do not login at all...
	}
}
