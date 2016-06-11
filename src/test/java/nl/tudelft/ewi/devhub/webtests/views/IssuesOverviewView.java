package nl.tudelft.ewi.devhub.webtests.views;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import lombok.Data;
import lombok.SneakyThrows;

public class IssuesOverviewView extends AuthenticatedView {

	private static final DateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy HH:mm");

	public IssuesOverviewView(WebDriver driver) {
		super(driver);
	}

	public IssueCreateView addIssue() {
		getDriver().findElement(By.linkText("Create issue")).click();
		return new IssueCreateView(getDriver());
		
	}

	public List<Issue> listOpenIssues() {
		WebElement openIssuesTable = getDriver().findElement(By.cssSelector("table:first-of-type"));
		return listIssues(openIssuesTable);
		
	}


	public List<Issue> listClosedIssues() {
		WebElement closedIssuesTable = getDriver().findElement(By.cssSelector("table:last-of-type"));
		return listIssues(closedIssuesTable);
		
	}
	
	private List<Issue> listIssues(WebElement element) {
		return element.findElements(By.tagName("a")).stream().map(x -> new Issue(x)).collect(Collectors.toList());
	}
	
	@Data
	public class Issue {
		
		private String title;
		
		private int id;
		
		private Date openedDate;
		
		private WebElement anchor;
		
		@SneakyThrows
		public Issue(WebElement element) {
			
			anchor = element;
			
			String header = element.findElement(By.cssSelector("div.comment")).getText();
			Pattern headerPattern = Pattern.compile("Issue #(\\d+): (\\D+)");
			Matcher matcher  = headerPattern.matcher(header);
			
			matcher.matches();
			id = Integer.parseInt(matcher.group(1));
			title = matcher.group(2);
			
			String openedDateString = element.findElement(By.cssSelector("div.timestamp")).getText().replace("Opened on ", "");
			
			openedDate = dateFormat.parse(openedDateString);
			
		}
		
		public IssueOverviewView click(){
			anchor.click();
			return new IssueOverviewView(getDriver());
		}
		
	}

}
