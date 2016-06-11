package nl.tudelft.ewi.devhub.webtests.views;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;

public class IssueOverviewView extends IssueView {
		
	private static final DateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy HH:mm");

	public IssueOverviewView(WebDriver driver) {
		super(driver);
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

	public void addComment(String comment) {
		
		getDriver().findElement(By.cssSelector("textarea[name=\"content\"]")).sendKeys(comment);
		getDriver().findElement(By.cssSelector("button[type=\"submit\"]")).click();
		
	}
	
	public List<Comment> listComments(){
		return getDriver().findElement(By.cssSelector("div.pull-feed"))
				.findElements(By.cssSelector("div.panel-comment"))
				.stream().map(x -> new Comment(x)).collect(Collectors.toList());
	}
	
	@Data
	public class Comment {

		@Getter(AccessLevel.NONE)
		private final DateFormat commentDateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a");
		
		private String posterName;
		
		private String content;
		
		private Date postedTime;
		
		@SneakyThrows
		public Comment(WebElement element){
			posterName = element.findElement(By.cssSelector("div.panel-heading strong")).getText();
			content = element.findElement(By.cssSelector("div.panel-body p")).getText();
			String postedTimeString = element.findElement(By.cssSelector("div.panel-heading a")).getText();
			postedTime = commentDateFormat.parse(postedTimeString);
		}
		
	}
	
	
}
