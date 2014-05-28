package nl.tudelft.ewi.devhub.webtests;

import static org.junit.Assert.assertTrue;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.AuthenticatedView;
import nl.tudelft.ewi.devhub.webtests.views.ProjectView;

import org.junit.Test;

public class ProjectsTests extends WebTest {
	
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
	public void testICanOpenTheProjectsPage() {
		AuthenticatedView view = openLoginScreen().login(NET_ID, PASSWORD)
				.toProjectsView();
		
		assertTrue(view.currentPathEquals("/projects"));
	}
	
	/**
	 * <h1>Opening a project overview .</h1>
	 * 
	 * Given that:
	 * <ol>
	 *   <li>I am successfully logged in.</li>
	 *   <li>I have a project.</li>
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
	public void testICanOpenProject() {
		ProjectView view = openLoginScreen().login(NET_ID, PASSWORD)
				.toProjectsView().listMyProjects().get(0).click();
	}
	
}
