package nl.tudelft.ewi.devhub.webtests.views;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
		statusDropdown.selectByVisibleText(status);
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
		String dateString = getDriver().findElement(By.id("timestampOpened")).getText();
		return dateFormat.parse(dateString);
	}
	
	public Date getClosed() throws ParseException{
		String dateString = getDriver().findElement(By.id("timestampClosed")).getText();
		return dateFormat.parse(dateString);
	}

	public IssueEditView selectLabels(String... labelTags) {
		// The select is transformed by multiple-select. Therefore org.openqa.selenium.support.ui.Select can't be used
		
		List<String> labelTagsList = Arrays.asList(labelTags);
		getDriver().findElement(By.cssSelector("button.ms-choice")).click();
		
		// Select/deselect appropriate elements
		getDriver().findElements(By.cssSelector("div.ms-drop input[type=\"checkbox\"]")).forEach(element -> {
			String elementText = element.findElement(By.xpath("..//span")).getText();
			if(element.isSelected() ^ labelTagsList.contains(elementText)){
				element.click();
			}
		});
		
		return this;
	}

}
