package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AuthenticatedView extends View {

	private static final By PROJECTS_VIEW = By.xpath("//div[@class='container']//a[text()='Projects']"); 
	
	public AuthenticatedView(WebDriver driver) {
		super(driver);
	}
	
	public ProjectsView toProjectsView() {
		getDriver().findElement(PROJECTS_VIEW).click();
		return new ProjectsView(getDriver());
	}
	
}
