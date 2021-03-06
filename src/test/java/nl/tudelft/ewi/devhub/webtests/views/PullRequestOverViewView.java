package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.collect.Lists;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.assertTrue;

import java.util.List;

@Slf4j
public class PullRequestOverViewView extends PullRequestView {

	public PullRequestOverViewView(WebDriver driver) {
		super(driver);
	}

	@Override
	protected void invariant() {
		super.invariant();
		assertTrue(currentPathContains("/pull/"));
	}

	/**
	 * Check if the pull request is open.
	 * @return Returns true if the pull request is open.
     */
	public boolean isOpen() {
		if(getDriver().findElements(By.id("btn-close")).stream().findAny().isPresent()){
			return getDriver().findElement(By.id("btn-close")).isEnabled();
		} 
		return false;
	}

	/**
	 * Check if the pull request is closed.
	 * @return Returns true if the pull request is open.
     */
	public boolean isClosed() {
		if(getDriver().findElements(By.id("btn-close")).stream().findAny().isPresent()){
			return !getDriver().findElement(By.id("btn-close")).isEnabled();
		} 
		return false;
	}

	/**
	 * Check if the pull request is closed.
	 * @return Returns true if the pull request is open.
     */
	public boolean isMerged() {
		if(getDriver().findElements(By.id("btn-merge")).stream().findAny().isPresent()){
			return !getDriver().findElement(By.id("btn-merge")).isEnabled();
		} 
		return false;
	}

	/**
	 * Close the pull request.
	 */
	public void close() {
		getCloseButton().click();
	}

	/**
	 * Merge the pull request.
	 */
	public void merge() {
		getMergeButton().click();
	}
	
	/**
	 * Open diff view
	 */
	public DiffInPullRequestView openDiffView(){
		getDriver().findElement(By.xpath("//a[contains(text(),'View diff')]")).click();
		return new DiffInPullRequestView(getDriver());
	}

	/**
	 * Remove the branch.
	 */
	public void removeBranch() {
		getRemoveBranchButton().click();
	}
	
	public WebElement getMergeButton(){
		return getDriver().findElement(By.id("btn-merge"));
	}

	public WebElement getCloseButton(){
		return getDriver().findElement(By.id("btn-close"));
	}

	public WebElement getRemoveBranchButton(){
		return getDriver().findElement(By.id("btn-remove-branch"));
	}
	
	public List<Comment> listComments(){
		
		invariant();		
		WebElement container = getDriver().findElement(By.cssSelector("div.pull-feed"));
		return listComments(container);
		
		
	}
	
	private List<Comment> listComments(WebElement container){
		
		List<Comment> result = Lists.newArrayList();
		List<WebElement> entries = container.findElements(By.cssSelector("div.panel-comment"));
		
		for (WebElement webElement : entries) {
			result.add(new Comment(webElement));
		}
		
		return result;
		
	}
	
	public void addComment(String comment){
		
		WebElement commentForm = getDriver().findElement(By.id("pull-comment-form"));
		
		WebElement textArea = commentForm.findElement(By.tagName("textarea"));
		
		textArea.clear();
		textArea.sendKeys(comment);
		
		commentForm.findElement(By.tagName("button")).click();
		
	}
	
	@Data
	public static class Comment {
		
		private WebElement anchor;
		
		private String header;
		
		private String content;
		
		public Comment(WebElement element){
			
			anchor = element;
			
			header = element.findElement(By.cssSelector("div.panel-heading")).getText();
			
			content = element.findElement(By.cssSelector("div.panel-body")).getText();
			
		}		
		
	}

}
