package nl.tudelft.ewi.devhub.webtests.views;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class IssueOverviewView extends IssueView {
		
	private static final DateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy HH:mm");

	public IssueOverviewView(WebDriver driver) {
		super(driver);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getTitle() {
		return getDriver().findElement(By.id("title")).getText();
	}

	@Override
	public String getDescription() {
		return getDriver().findElement(By.id("description")).getText();
	}

	@Override
	public String getAssignee() {
		return getDriver().findElement(By.id("assignee")).getText();
	}
	
	public IssueEditView edit(){
		getDriver().findElement(By.cssSelector("#create-issue-form a")).click();
		return new IssueEditView(getDriver());
	}

	public String getStatus() {
		return getDriver().findElement(By.id("status")).getText();
	}
	
	public Date getOpened() throws ParseException{
		String dateString = getDriver().findElement(By.id("timestampOpened")).getText();
		return dateFormat.parse(dateString);
	}
	
	public Date getClosed() throws ParseException{
		String dateString = getDriver().findElement(By.id("timestampClosed")).getText();
		return dateFormat.parse(dateString);
	}
	
	
}
