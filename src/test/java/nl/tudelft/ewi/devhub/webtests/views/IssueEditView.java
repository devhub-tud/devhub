package nl.tudelft.ewi.devhub.webtests.views;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

public class IssueEditView extends AbstractIssueEditView {
	
	private static final DateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy HH:mm");

	public IssueEditView(WebDriver driver) {
		super(driver);
	}
	
	public AbstractIssueEditView setStatus(String status){
		Select statusDropdown = new Select(getDriver().findElement(By.id("status")));
		statusDropdown.selectByValue(status);
		return this;
	}
	
	public String getStatus(){
		Select statusDropdown = new Select(getDriver().findElement(By.id("status")));
		return statusDropdown.getFirstSelectedOption().getText();
	}
	
	public IssueOverviewView save(){
		By submitbutton = By.cssSelector("button[type=\"submit\"]");
		getDriver().findElement(submitbutton).click();
		return new IssueOverviewView(getDriver());
	}
	
	public Date getOpened() throws ParseException{
		String dateString = getDriver().findElement(By.id("timestampOpened")).getAttribute("value");
		return dateFormat.parse(dateString);
	}
	
	public Date getClosed() throws ParseException{
		String dateString = getDriver().findElement(By.id("timestampClosed")).getAttribute("value");
		return dateFormat.parse(dateString);
	}

}
