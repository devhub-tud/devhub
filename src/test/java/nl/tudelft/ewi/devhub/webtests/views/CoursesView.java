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
import static org.junit.Assert.assertTrue;

public class CoursesView extends AuthenticatedView {

	private static final By MY_PROJECTS_HEADER = By.xpath("//h2[starts-with(normalize-space(.), 'My courses')]");
	private static final By ASSISTING_PROJECTS_HEADER = By.xpath("//h2[starts-with(normalize-space(.), 'Assisting projects')]");
    private static final By AVAILABLE_COURSES_HEADER = By.xpath("//h2[starts-with(normalize-space(.), 'Available courses')]");

	CoursesView(WebDriver driver) {
		super(driver);
	}

	@Override
	protected void invariant() {
		super.invariant();
		assertTrue(currentPathEquals("/courses"));
		assertNotNull(getDriver().findElement(MY_PROJECTS_HEADER));
	}

	/**
	 * @return A {@link List} of all {@link Project}s in the "My projects" section.
	 */
	public List<Project> listMyProjects() {
		invariant();
		WebElement myProjectsHeader = getDriver().findElement(MY_PROJECTS_HEADER);
		WebElement table = Dom.nextSibling(myProjectsHeader, "table");
		return listProjectsInTable(table);
	}

	/**
	 * @return True if the currently logged in user is assisting one or more projects.
	 */
	public boolean hasAssistingProjects() {
		invariant();
		return Dom.isVisible(getDriver(), ASSISTING_PROJECTS_HEADER);
	}

	/**
	 * @return A {@link List} of all {@link Project}s in the "Assisting projects" section.
	 */
	public List<CourseOverview> listAssistingCourses() {
		invariant();
		WebElement assistingProjectsHeader = getDriver().findElement(ASSISTING_PROJECTS_HEADER);
		WebElement table = Dom.nextSibling(assistingProjectsHeader, "table");
		return listProjectOverviewsInTable(table);
	}

    /**
     * @return A {@link List} of all {@link Project}s in the "Available courses" section.
     */
	public List<CourseOverview> listAvailableCourses() {
        invariant();
        WebElement availableHeader = getDriver().findElement(AVAILABLE_COURSES_HEADER);
        WebElement table = Dom.nextSibling(availableHeader, "table");
        return listProjectOverviewsInTable(table);
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
	
	private List<CourseOverview> listProjectOverviewsInTable(WebElement table) {
		List<WebElement> entries = table.findElements(By.tagName("td"));
		if (entries.size() == 1) {
			if (!Dom.isVisible(entries.get(0), By.tagName("a"))) {
				return Lists.newArrayList();
			}
		}

		List<CourseOverview> projects = Lists.newArrayList();
		for (WebElement entry : entries) {
			WebElement projectLink = entry.findElement(By.tagName("a"));
			CourseOverview project = new CourseOverview(projectLink.getText(), projectLink);
			projects.add(project);
		}
		return projects;
	}

	@Data
	public class Project {
		private final String name;

		@Getter(AccessLevel.NONE)
		private final WebElement anchor;

		public CommitsView click() {
			anchor.click();
			return new CommitsView(getDriver());
		}
	}

	@Data
	public class CourseOverview {
		private final String namel;
		
		@Getter(AccessLevel.NONE)
		private final WebElement anchor;
		
		public CourseView click() {
			anchor.click();
			return new CourseView(getDriver());
		}

        public GroupEnrollView clickEnroll() {
            anchor.click();
            return new GroupEnrollView(getDriver());
        }
	}
	
}
