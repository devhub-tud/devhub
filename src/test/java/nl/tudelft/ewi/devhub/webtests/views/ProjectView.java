package nl.tudelft.ewi.devhub.webtests.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProjectView extends AuthenticatedView {

	private static final By GIT_CLONE_URL = By.xpath("//h4[starts-with(normalize-space(.), 'Git clone URL')]");
	
	ProjectView(WebDriver driver) {
		super(driver);
		assertInvariant();
	}

	private void assertInvariant() {
		assertTrue(currentPathEquals("/projects"));
		assertNotNull(getDriver().findElement(GIT_CLONE_URL));
	}
	
}
