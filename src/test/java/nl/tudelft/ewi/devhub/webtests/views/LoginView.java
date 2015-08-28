package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LoginView extends View {

	private static final By LOGIN_FORM = By.id("login-form");
	private static final By USERNAME_FIELD = By.name("netID");
	private static final By PASSWORD_FIELD = By.name("password");
	private static final By LOGIN_BUTTON = By.name("login");

	public static LoginView create(WebDriver driver, String host) {
		driver.navigate().to(host);
		return new LoginView(driver);
	}

	private LoginView(WebDriver driver) {
		super(driver);
		assertInvariant();
	}

	private void assertInvariant() {
		assertTrue(currentPathEquals("/login"));
		assertNotNull(getDriver().findElement(LOGIN_FORM));
		assertNotNull(getDriver().findElement(USERNAME_FIELD));
		assertNotNull(getDriver().findElement(PASSWORD_FIELD));
		assertNotNull(getDriver().findElement(LOGIN_BUTTON));
	}

	/**
	 * Logs in using the specified <code>username</code> and <code>password</code>.
	 * 
	 * @param username
	 *            The username to use when logging in.
	 * @param password
	 *            The password to use when logging in.
	 * @return The {@link CoursesView} to which the user will be redirected upon a
	 *         successful login. In case of an unsuccessful login, a
	 *         {@link AssertionError} will be thrown.
	 */
	public CoursesView login(String username, String password) {
		assertInvariant();
		String url = getDriver().getCurrentUrl();

		setUsernameField(username);
		setPasswordField(password);
		clickLoginButton();

		waitUntilCurrentUrlDiffersFrom(url);
		return new CoursesView(getDriver());
	}

	/**
	 * This method fills in the specified <code>username</code> in the login form.
	 * 
	 * @param username
	 *            The username to fill in the username field.
	 * @return The current {@link LoginView}.
	 */
	public LoginView setUsernameField(String username) {
		assertInvariant();
		WebElement usernameField = getDriver().findElement(USERNAME_FIELD);
		usernameField.sendKeys(username);
		return this;
	}

	/**
	 * This method fills in the specified <code>password</code> in the login form.
	 * 
	 * @param password
	 *            The password to fill in the password field.
	 * @return The current {@link LoginView}.
	 */
	public LoginView setPasswordField(String password) {
		assertInvariant();
		WebElement passwordField = getDriver().findElement(PASSWORD_FIELD);
		passwordField.sendKeys(password);
		return this;
	}

	/**
	 * This method clicks on the login button in the login form.
	 * 
	 * @return The current {@link LoginView}.
	 */
	public LoginView clickLoginButton() {
		assertInvariant();
		WebElement loginButton = getDriver().findElement(LOGIN_BUTTON);
		loginButton.click();
		return this;
	}

}
