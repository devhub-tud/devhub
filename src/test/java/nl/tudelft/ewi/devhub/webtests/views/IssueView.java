package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public abstract class IssueView extends AuthenticatedView {

	public IssueView(WebDriver driver) {
		super(driver);
		// TODO Auto-generated constructor stub
	}
	
	public abstract String getTitle();
	
	public abstract String getDescription();
	
	/**
	 * @return The name of the assignee
	 */
	public abstract String getAssignee();

}
