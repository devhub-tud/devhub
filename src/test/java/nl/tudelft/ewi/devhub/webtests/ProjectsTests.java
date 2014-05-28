package nl.tudelft.ewi.devhub.webtests;

import static org.junit.Assert.assertTrue;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.AuthenticatedView;

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
	
}
