package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.AssignmentView;
import nl.tudelft.ewi.devhub.webtests.views.AssignmentsView;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
		assertEquals(assignment.getName(), "Product vision");
		assertEquals(assignment.getNumber(), "1");
	}

	@Test
	public void testAssignment() throws ParseException {
		final String net_id = "assistant1";
		final String password = "assistant1";

		final AssignmentView view = openLoginScreen()
				.login(net_id, password)
				.listAssistingCourses()
				.get(0).click()
				.listGroups()
				.get(0).click()
				.toAssignmentView()
				.listAssignments()
				.get(0).click();

		final AssignmentView.Assignment assignment = view.getAssignment();
        assertEquals("Student Two", assignment.getAuthor());
        // TODO: 4-5-16 assert date is equal to current date
    }
	
}
