package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;

import org.junit.Test;

public class CoursesTest extends WebTest {
	
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
	
}
