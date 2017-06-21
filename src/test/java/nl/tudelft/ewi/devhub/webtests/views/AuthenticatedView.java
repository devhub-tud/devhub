package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertNotNull;

public class AuthenticatedView extends View {

	private static final By HEADER = By.xpath("//nav");

	private static final By NOTIFICATIONS_BUTTON = By.xpath("//a[text()='Notifications'");

	private static final By BUILD_SERVERS_BUTTON = By.xpath("//a[text()='Build servers']");

	private static final By COURSES_BUTTON = By.xpath("//a[text()='Courses']");

	private static final By ACCOUNT_BUTTON = By.xpath("//a[text()='Account']");

	private static final By LOGOUT_BUTTON = By.xpath("//a[text()='Logout']");

	public AuthenticatedView(WebDriver driver) {
		super(driver);
	}

	@Override
	protected void invariant() {
		assertNotNull(getNavBar());
	}

	protected WebElement getNavBar() {
		return getDriver().findElement(HEADER);
	}

	/**
	 * Go to the courses view.
	 *
	 * @return the {@link CoursesView}.
	 */
	public CoursesView toCoursesView() {
		getNavBar().findElement(COURSES_BUTTON).click();
		return new CoursesView(getDriver());
	}

	/**
	 * Method added to facilitate testing logging out in the AccountTest file.
	 *
	 *
	 * @return The {@link LoginView} to which the user will be redirected to when logging out succeeded.
	 */
	public LoginView logout() {
		String url = getDriver().getCurrentUrl();

		getNavBar().findElement(LOGOUT_BUTTON).click();

		waitUntilCurrentUrlDiffersFrom(url);

		return new LoginView(getDriver());
	}

	public NotificationView toNotificationView() {

		String url = getDriver().getCurrentUrl();

		getDriver().findElement(NOTIFICATIONS_BUTTON).click();

		//waitUntilCurrentUrlDiffersFrom(url);

		return new NotificationView(getDriver());
	}

}
