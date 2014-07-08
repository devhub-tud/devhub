package nl.tudelft.ewi.devhub.webtests.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import nl.tudelft.ewi.devhub.webtests.utils.Dom;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.collect.Lists;

public class ProjectView extends AuthenticatedView {

	private static final By GIT_CLONE_URL = By.xpath("//h4[starts-with(normalize-space(.), 'Git clone URL')]");
	
	private static final By RECENT_COMMITS_HEADER = By.xpath("//h4[starts-with(normalize-space(.), 'Recent commits')]");
	
	public ProjectView(WebDriver driver) {
		super(driver);
		assertInvariant();
	}

	private void assertInvariant() {
		assertTrue(currentPathStartsWith("/projects"));
		assertNotNull(getDriver().findElement(GIT_CLONE_URL));
	}
	
	/**
	 * @return A {@link List} of all {@link Commit}s in the "Recent commits" section
	 */
	public List<Commit> listCommits() {
		assertInvariant();
		WebElement recentCommitsHeader = getDriver().findElement(RECENT_COMMITS_HEADER);
		WebElement table = Dom.nextSibling(recentCommitsHeader, "table");
		return listCommits(table);
	}
	
	private List<Commit> listCommits(WebElement table) {
		List<WebElement> entries = table.findElements(By.tagName("td"));
		List<Commit> commits = Lists.newArrayList();
		
		if(entries.size() == 1) {
			if(!Dom.isVisible(entries.get(0), By.tagName("a"))) {
				return commits;
			}
		}
		
		for (WebElement entry : entries) {
			WebElement diffLink = entry.findElement(By.tagName("a"));
			String message = entry.findElement(By.className("comment")).getText();
			String author = entry.findElement(By.className("committer")).getText();
			commits.add(new Commit(message, author, null, diffLink));
		}
		
		return commits;
		
	}
	
	@Data
	public class Commit {
		
		private final String message, author;
		
		private final List<String> tagNames;
		
		@Getter(AccessLevel.NONE)
		private final WebElement anchor;

		public DiffView click() {
			anchor.click();
			return new DiffView(getDriver());
		}
		
	}
	
}
