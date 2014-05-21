package nl.tudelft.ewi.devhub;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LoginTest extends WebTest {

	private static final String VALID_USERNAME = System.getProperty("username");
	private static final String VALID_PASSWORD = System.getProperty("password");

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
		getSession().login(VALID_USERNAME, VALID_PASSWORD);
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
		String url = getSession().getCurrentUrl();

		getSession().setUsernameField(VALID_USERNAME)
				.setPasswordField(VALID_PASSWORD + "!")
				.clickLoginButton();

		getSession().waitUntilCurrentUrlDiffersFrom(url);
		assertEquals(1, getSession().listAlerts().size());
	}

}
