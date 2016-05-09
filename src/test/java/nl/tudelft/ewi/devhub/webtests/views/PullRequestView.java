package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class PullRequestView extends AuthenticatedView {
	
	private static final By BREADCRUMB = By.xpath("/html/body/div/ol");
	
	private static final By HEADERS = By.xpath("//div[@class='headers']");
	private static final By MESSAGE_HEADER = By.xpath(".//h2[@class='header']");
	private static final By AUTHOR_SUB_HEADER = By.xpath(".//h5[@class='subheader']");

	public PullRequestView(WebDriver driver) {
		super(driver);
	}


	protected WebElement getBreadcrumb() {
		return getDriver().findElement(BREADCRUMB);
	}

	protected WebElement getHeaders() {
		return getDriver().findElement(HEADERS);
	}

	/**
	 * @return the text content of the author header.
	 */
	public String getAuthorHeader() {
		return getHeaders().findElement(AUTHOR_SUB_HEADER).getText();
	}

	/**
	 * @return the text content of the message header.
	 */
	public String getMessageHeader() {
		return getHeaders().findElement(MESSAGE_HEADER).getText();
	}

	/**
	 * Navigate to the courses overview page.
	 * @return a {@link CoursesView}.
     */
	public CoursesView toCoursesView() {
		getBreadcrumb().findElement(By.xpath("./li[1]/a")).click();
		return new CoursesView(getDriver());
	}

	/**
	 * Navigate to the course view. Note that the course view currently
	 * does not exist, and redirects to the course edition page.
	 * @return a {@link CoursesView}.
	 * @see PullRequestOverViewView#toCourseEditionView()
     */
	public AuthenticatedView toCourseView() {
		getBreadcrumb().findElement(By.xpath("./li[2]/a")).click();
		return new AuthenticatedView(getDriver());
	}

	/**
	 * Navigate to the {@code CourseEditionView}. Note that the course edition
	 * view is only for administrators and TA's. For students, this
	 * page redirects to the {@link CommitsView}.
	 * @return a {@link AuthenticatedView}
     */
	public AuthenticatedView toCourseEditionView() {
		getBreadcrumb().findElement(By.xpath("./li[3]/a")).click();
		return new AuthenticatedView(getDriver());
	}

	/**
	 * Go to the main project page (the {@link CommitsView}).
	 * @return a {@link CommitsView}.
     */
	public CommitsView toCommitsView() {
		getBreadcrumb().findElement(By.xpath("./li[4]/a")).click();
		return new CommitsView(getDriver());
	}

	/**
	 * Go to the pull requests overview page.
	 * @return An {@link AuthenticatedView}.
     */
	public AuthenticatedView toPullRequestsView() {
		getBreadcrumb().findElement(By.xpath("./li[5]/a")).click();
		return new AuthenticatedView(getDriver());
	}

	/**
	 * @return the pull request name. Usually in the format {@code Pull Request ${pullRequest.getIssueId()}}.
     */
	public String getPullRequestName() {
		return getBreadcrumb().findElement(By.xpath("./li[6]")).getText();
	}

}
