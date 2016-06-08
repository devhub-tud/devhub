package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public abstract class AbstractIssueEditView extends IssueView {

	public AbstractIssueEditView(WebDriver driver) {
		super(driver);
	}
	
	public AbstractIssueEditView setTitle(String title){
		WebElement titleElement = getDriver().findElement(By.id("title"));
		titleElement.clear();
		titleElement.sendKeys(title);
		return this;
	}
	
	public AbstractIssueEditView setDescription(String description){
		WebElement descriptionElement = getDriver().findElement(By.id("description"));
		descriptionElement.clear();
		descriptionElement.sendKeys(description);
		return this;
	}
	
	public AbstractIssueEditView setAssignee(String assigneeNetId){
		Select assigneeDropdown = new Select(getDriver().findElement(By.id("assignee")));
		assigneeDropdown.selectByValue(assigneeNetId);
		return this;
	}
	
	public String getTitle(){
		WebElement titleElement = getDriver().findElement(By.id("title"));
		return titleElement.getAttribute("value");		
	}
	
	public String getDescription(){
		WebElement descriptionElement = getDriver().findElement(By.id("description"));
		return descriptionElement.getText();		
	}
	
	/**
	 * @return The netId of the assignee
	 */
	public String getAssignee(){
		Select assigneeDropdown = new Select(getDriver().findElement(By.id("assignee")));
		return assigneeDropdown.getFirstSelectedOption().getText();		
	}

}
