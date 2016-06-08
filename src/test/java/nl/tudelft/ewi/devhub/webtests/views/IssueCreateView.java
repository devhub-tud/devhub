package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class IssueCreateView extends AbstractIssueEditView {

	public IssueCreateView(WebDriver driver) {
		super(driver);
	}
	
	public IssueOverviewView create(){
		getDriver().findElement(By.cssSelector("button[type=\"submit\"]")).click();
		return new IssueOverviewView(getDriver());
	}

}
