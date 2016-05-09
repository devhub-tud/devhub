package nl.tudelft.ewi.devhub.webtests;

import static org.junit.Assert.assertEquals;

import java.util.List;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.AssignmentsView;

import org.junit.Test;

public class AssignmentTest extends WebTest {

	@Test
	public void testListAssignments() {
		AssignmentsView view = openLoginScreen()
				.login(NET_ID, PASSWORD)
				.toCoursesView()
				.listMyProjects()
				.get(0).click()
				.toAssignmentView();
		

		List<AssignmentsView.Assignment> assignments = view.listAssignments();
		assertEquals(2, assignments.size());

		AssignmentsView.Assignment assignment = assignments.get(0);
		assertEquals(assignment.getName(), "Part 1. E2E & Boundary Testing");
		assertEquals(assignment.getNumber(), "1");
	}
	
}
