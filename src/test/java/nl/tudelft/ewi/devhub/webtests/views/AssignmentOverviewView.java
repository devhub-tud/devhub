package nl.tudelft.ewi.devhub.webtests.views;

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

public class AssignmentOverviewView extends AuthenticatedView {

	private static final By TABLE_XPATH = By.xpath("//table");
	private static final int COMMIT_TABLE = 0;

	public AssignmentOverviewView(WebDriver driver) {
		super(driver);
		assertInvariant();
	}

	private void assertInvariant() {
		assertTrue(currentPathStartsWith("/courses"));
	}

	/**
	 * @return a list of all commits on an assignment.
	 */
	public List<Project> listGroups() {
		assertInvariant();
		WebElement myCommitTable = getDriver().findElements(TABLE_XPATH).get(COMMIT_TABLE);
		return listGroupsInTable(myCommitTable);
	}

	private List<Project> listGroupsInTable(WebElement table) {
		List<WebElement> entries = table.findElements(By.tagName("td"));
		if (isEmptyList(entries)) {
			return Lists.newArrayList();
		}

		return Lists.transform(entries, entry -> {
			WebElement projectLink = entry.findElement(By.tagName("a"));
			return new Project(projectLink.getText(), projectLink);
		});
	}

	private boolean isEmptyList(List<WebElement> entries) {
		// An empty list containing one row: "No entries to show"
		return entries.size() == 1 && !Dom.isVisible(entries.get(0), By.tagName("a"));
	}

	@Data
	public class Project {
		private final String Comment;

		@Getter(AccessLevel.NONE)
		private final WebElement anchor;

		public CommitsView click() {
			anchor.click();
			return new CommitsView(getDriver());
		}
	}

}
