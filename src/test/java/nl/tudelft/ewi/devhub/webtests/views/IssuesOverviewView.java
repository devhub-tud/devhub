package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class IssuesOverviewView extends AuthenticatedView {

	public IssuesOverviewView(WebDriver driver) {
		super(driver);
	}

	public IssueCreateView addIssue() {
		getDriver().findElement(By.linkText("Add Issue")).click();
		return new IssueCreateView(getDriver());
		
	}

}
