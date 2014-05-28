package nl.tudelft.ewi.devhub.webtests.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import nl.tudelft.ewi.devhub.webtests.utils.Dom;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ProjectsView extends AuthenticatedView {

	private static final By MY_PROJECTS_HEADER = By.xpath("//h2[starts-with(normalize-space(.), 'My projects')]");
	private static final By CREATE_GROUP_BUTTON = By.xpath("//a[contains(normalize-space(.), 'Create new group')]");
	private static final By ASSISTING_PROJECTS_HEADER = By.xpath("//h2[starts-with(normalize-space(.), 'Assisting projects')]");

	ProjectsView(WebDriver driver) {
		super(driver);
		assertInvariant();
	}

	private void assertInvariant() {
		assertTrue(currentPathEquals("/projects"));
		assertNotNull(getDriver().findElement(MY_PROJECTS_HEADER));
		assertNotNull(getDriver().findElement(CREATE_GROUP_BUTTON));
	}

	/**
	 * @return A {@link List} of all {@link Project}s in the "My projects" section.
	 */
	public List<Project> listMyProjects() {
		assertInvariant();
		WebElement myProjectsHeader = getDriver().findElement(MY_PROJECTS_HEADER);
		WebElement table = Dom.nextSibling(myProjectsHeader, "table");
		return listProjectsInTable(table);
	}

	/**
	 * @return True if the currently logged in user is assisting one or more projects.
	 */
	public boolean hasAssistingProjects() {
		assertInvariant();
		return Dom.isVisible(getDriver(), ASSISTING_PROJECTS_HEADER);
	}

	/**
	 * @return A {@link List} of all {@link Project}s in the "Assisting projects" section.
	 */
	public List<Project> listAssistingProjects() {
		assertInvariant();
		WebElement assistingProjectsHeader = getDriver().findElement(ASSISTING_PROJECTS_HEADER);
		WebElement table = Dom.nextSibling(assistingProjectsHeader, "table");
		return listProjectsInTable(table);
	}

	private List<Project> listProjectsInTable(WebElement table) {
		List<WebElement> entries = table.findElements(By.tagName("td"));
		if (entries.size() == 1) {
			if (!Dom.isVisible(entries.get(0), By.tagName("a"))) {
				return Lists.newArrayList();
			}
		}

		List<Project> projects = Lists.newArrayList();
		for (WebElement entry : entries) {
			WebElement projectLink = entry.findElement(By.tagName("a"));
			Project project = new Project(projectLink.getText(), projectLink);
			projects.add(project);
		}
		return projects;
	}

	@Data
	public class Project {
		private final String name;

		@Getter(AccessLevel.NONE)
		private final WebElement anchor;

		public ProjectView click() {
			anchor.click();
			return new ProjectView(getDriver());
		}
	}

}
