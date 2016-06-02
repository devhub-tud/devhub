package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class IssueCreateView extends IssueDetailsView {

	public IssueCreateView(WebDriver driver) {
		super(driver);
	}
	
	public IssueEditView create(){
		getDriver().findElement(By.linkText("Create Issue")).click();
		return new IssueEditView(getDriver());
	}

}
