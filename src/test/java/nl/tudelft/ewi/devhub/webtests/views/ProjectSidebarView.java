package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public abstract class ProjectSidebarView extends AuthenticatedView {

	private static final By GIT_CLONE_URL = By.xpath("//h4[starts-with(normalize-space(.), 'Git clone URL')]");

	private static final By RECENT_COMMITS_HEADER = By.xpath("//h4[starts-with(normalize-space(.), 'Recent commits')]");

	private static final By BREADCRUMB = By.xpath("/html/body/div/ol");

	protected ProjectSidebarView(WebDriver driver) {
		super(driver);
	}

	@Override
	protected void invariant() {
		assertTrue(currentPathStartsWith("/courses"));
		assertNotNull(getBreadcrumb());
		assertNotNull(getDriver().findElement(RECENT_COMMITS_HEADER));
	}

	protected WebElement getBreadcrumb() {
		return getDriver().findElement(BREADCRUMB);
	}

	@Override
	public CoursesView toCoursesView() {
		getBreadcrumb().findElement(By.xpath("./li[1]/a")).click();
		return new CoursesView(getDriver());
	}

	public String getGroupName() {
		return getBreadcrumb().findElement(By.xpath("./li[3]")).getText();
	}

	public String getCloneUrl() {
		return getDriver().findElement(GIT_CLONE_URL)
			.findElement(By.xpath("./following-sibling::input"))
			.getAttribute("value");
	}

}
