package nl.tudelft.ewi.devhub.webtests.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import nl.tudelft.ewi.devhub.webtests.views.ProjectView.Commit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FolderView extends View {

	private static final By HEADERS = By.xpath("//span[@class='headers']");
	private static final By DROPDOWN_CARET = By.xpath("//button[contains(@class,'dropdown-toggle')]");
	private static final By MESSAGE_HEADER = By.xpath(".//h2[@class='header']");
	private static final By AUTHOR_SUB_HEADER = By.xpath(".//h5[@class='subheader']");
	private static final By VIEW_FILES_BUTTON = By.xpath("//a[starts-with(normalize-space(.), 'View files')]");
	
	private final Commit commit;
	
	public FolderView(WebDriver driver, Commit commit) {
		super(driver);
		this.commit = commit;
		assertInvariant();
	}
	
	private void assertInvariant() {
		assertTrue(currentPathStartsWith("/projects"));
		WebElement headers = getDriver().findElement(HEADERS);
		
		assertNotNull(headers);
		assertNotNull(getDriver().findElement(DROPDOWN_CARET));
		assertNotNull(commit);
		
		assertEquals(commit.getAuthor(), headers.findElement(AUTHOR_SUB_HEADER).getText());
		assertEquals(commit.getMessage(), headers.findElement(MESSAGE_HEADER).getText());
	}

}
