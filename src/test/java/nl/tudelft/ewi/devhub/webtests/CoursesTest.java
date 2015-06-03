package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;

import nl.tudelft.ewi.devhub.webtests.views.CourseView;
import nl.tudelft.ewi.devhub.webtests.views.CourseView.Group;
import nl.tudelft.ewi.devhub.webtests.views.CoursesView;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class CoursesTest extends WebTest {

	public static final String ASSISTANT_NET_ID = "assistant1";
	public static final String ASSISTANT_PASSWORD = "assistant1";
	public static final String PROJECT_NAME = "TI1705 - Softwarekwaliteit & Testen";
	
	/**
	 * <h1>Opening the projects page.</h1>
	 * 
	 * Given that:
	 * <ol>
	 *   <li>I am successfully logged in.</li>
	 * </ol>
	 * When:
	 * <ol>
	 *   <li>I click on the projects link in the top menu bar.</li>
	 * </ol>
	 * Then:
	 * <ol>
	 *   <li>I am redirected to the projects page.</li>
	 * </ol>
	 */
	@Test
	public void testThatICanOpenTheProjectsPage() {
		openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView();
	}
	
	/**
	 * <h1>Opening a project overview .</h1>
	 * 
	 * Given that:
	 * <ol>
	 *   <li>I am successfully logged in.</li>
	 *   <li>I have a project.</li>
	 *   <li>There are no commits in the project</li>
	 * </ol>
	 * When:
	 * <ol>
	 *   <li>I click on a project in the project list.</li>
	 * </ol>
	 * Then:
	 * <ol>
	 *   <li>I am redirected to the project page.</li>
	 * </ol>
	 */
	@Test
	public void testThatICanOpenProject() {
		openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click();
	}

	@Test
	public void testListAssistingProjects() {
		CoursesView view = openLoginScreen()
			.login(ASSISTANT_NET_ID, ASSISTANT_PASSWORD)
			.toCoursesView();

		assertTrue(view.hasAssistingProjects());
		CourseView overview = view.listAssistingCourses().get(0).click();

		List<Group> list = overview.listGroups();
		assertThat(list.get(0).getName(), containsString(PROJECT_NAME));
		assertThat(list.get(1).getName(), containsString(PROJECT_NAME));
	}

	@Test
	public void testAssistantCanNavigateToCourseOverview() {
		openLoginScreen()
			.login(ASSISTANT_NET_ID, ASSISTANT_PASSWORD)
			.toCoursesView()
			.listAssistingCourses().get(0).click();
	}

	@Test
	public void testNavigateToProjectFromCourseOverview() {
		openLoginScreen()
			.login(ASSISTANT_NET_ID, ASSISTANT_PASSWORD)
			.toCoursesView()
			.listAssistingCourses().get(0).click()
			.listGroups().get(0).click();
	}

}
