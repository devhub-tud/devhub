package nl.tudelft.ewi.devhub.webtests.views;

import static org.junit.Assert.assertEquals;

import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import nl.tudelft.ewi.devhub.webtests.utils.Dom;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.collect.Lists;

public class CourseView extends AuthenticatedView {
	
	private static final By TABLE_XPATH = By.xpath("//table");

	public CourseView(WebDriver driver) {
		super(driver);
	}

	@Override
	protected void invariant() {
		super.invariant();

		WebElement groupTable = getDriver().findElements(TABLE_XPATH).get(0);
		assertEquals(Dom.prevSibling(groupTable).getText(), "Groups");

		WebElement Assignmenttable = getDriver().findElements(TABLE_XPATH).get(1);
		assertEquals(Dom.prevSibling(Assignmenttable).getText(), "Assignments");
	}

	public List<Group> listGroups() {
		invariant();
		WebElement myGroupTable = getDriver().findElements(TABLE_XPATH).get(0);
		return listGroupsInTable(myGroupTable);
	}
	
	public List<Assignment> listAssignments() {
		invariant();
		return null;
	}
	
	private List<Group> listGroupsInTable(WebElement table) {
		List<WebElement> entries = table.findElements(By.tagName("td"));
		if (entries.size() == 1) {
			if (!Dom.isVisible(entries.get(0), By.tagName("a"))) {
				return Lists.newArrayList();
			}
		}

		List<Group> groups = Lists.newArrayList();
		for (WebElement entry : entries) {
			WebElement projectLink = entry.findElement(By.tagName("a"));
			Group project = new Group(projectLink.getText(), projectLink);
			groups.add(project);
		}
		return groups;
	}
	
	@Data
	public class Group {
		private final String name;

		@Getter(AccessLevel.NONE)
		private final WebElement anchor;

		public CommitsView click() {
			anchor.click();
			return new CommitsView(getDriver());
		}
	}

	@Data
	public class Assignment {
		private final String name;

		@Getter(AccessLevel.NONE)
		private final WebElement anchor;

		public AuthenticatedView click() {
			anchor.click();
			return new AuthenticatedView(getDriver());
		}
	}

}

