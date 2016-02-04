package nl.tudelft.ewi.devhub.webtests.views;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import nl.tudelft.ewi.devhub.webtests.utils.Dom;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class CommitsView extends ProjectSidebarView {

	private static final By RECENT_COMMITS_HEADER = By.xpath("//h4[starts-with(normalize-space(.), 'Recent commits')]");

	public CommitsView(WebDriver driver) {
		super(driver);
	}

	@Override
	protected void invariant() {
		super.invariant();
		assertNotNull(getDriver().findElement(RECENT_COMMITS_HEADER));
	}

	/**
	 * @return A {@link List} of all {@link Commit}s in the "Recent commits" section
	 */
	public List<Commit> listCommits() {
		invariant();
		WebElement table = getDriver().findElement(By.id("table-commits"));
		return listCommits(table);
	}

	private List<Commit> listCommits(WebElement table) {
		List<WebElement> entries = table.findElements(By.tagName("td"));
		List<Commit> commits = Lists.newArrayList();

		if (entries.size() == 1) {
			if (!Dom.isVisible(entries.get(0), By.tagName("a"))) {
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

		public DiffInCommitView click() {
			anchor.click();
			return new DiffInCommitView(getDriver());
		}

	}

}
