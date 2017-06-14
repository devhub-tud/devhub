package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertNotNull;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public abstract class ProjectInCommitView extends AuthenticatedView {

	private static final By BREADCRUMB = By.xpath("/html/body/div/ol");

	private static final By HEADERS = By.xpath("//span[@class='headers']");
	private static final By MESSAGE_HEADER = By.xpath(".//h2[@class='header']");
	private static final By AUTHOR_SUB_HEADER = By.xpath(".//h5[@class='subheader']");
	private static final By LINES_ADDED = By.xpath(".//span[@class='addedlines']");
	private static final By LINES_REMOVED = By.xpath(".//span[@class='removedlines']");
	private static final By NEUTRAL_LINES = By.xpath(".//span[@class='neutrallines']");

	private static final By DROPDOWN_CARET = By.xpath("//button[contains(@class,'dropdown-toggle')]");
	private static final By VIEW_FILES_BUTTON = By.xpath("//a[starts-with(normalize-space(.), 'View files')]");

	protected ProjectInCommitView(WebDriver driver) {
		super(driver);
	}

	@Override
	protected void invariant() {
		super.invariant();
		assertNotNull(getBreadcrumb());
		assertNotNull(getHeaders());
	}

	protected WebElement getHeaders() {
		return getDriver().findElement(HEADERS);
	}

	protected WebElement getBreadcrumb() {
		return getDriver().findElement(BREADCRUMB);
	}

	/**
	 * @return the text content of the author header
	 */
	public String getAuthorHeader() {
		return getHeaders().findElement(AUTHOR_SUB_HEADER).getText();
	}

	/**
	 * @return the text content of the lines added header
	 */
	public String getLinesAdded() {
		return getHeaders().findElement(LINES_ADDED).getText();
	}

	/**
	 * @return the text content of the lines removed header
	 */
	public String getLinesRemoved() {
		return getHeaders().findElement(LINES_REMOVED).getText();
	}

	/**
	 * @return the text content of the neutral lines
	 */
	public String getNeutralLines() {
		return getHeaders().findElement(NEUTRAL_LINES).getText();
	}


	/**
	 * @return the text content of the message header
	 */
	public String getMessageHeader() {
		return getHeaders().findElement(MESSAGE_HEADER).getText();
	}


	@Override
	public CoursesView toCoursesView() {
		getBreadcrumb().findElement(By.xpath("./li[1]/a")).click();
		return new CoursesView(getDriver());
	}

	public CommitsView toCommitView() {
		getBreadcrumb().findElement(By.xpath("./li[3]/a")).click();
		return new CommitsView(getDriver());
	}

	public FolderView viewFiles() {
		invariant();

		WebElement container = getDriver().findElement(By.className("container"));
		WebElement dropdownCaret = container.findElement(DROPDOWN_CARET);
		dropdownCaret.click();
		WebElement viewFilesButton = container.findElement(VIEW_FILES_BUTTON);
		viewFilesButton.click();

		return new FolderView(getDriver());
	}

}
