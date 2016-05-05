package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.AssignmentView;
import nl.tudelft.ewi.devhub.webtests.views.AssignmentsView;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AssignmentTest extends WebTest {

	private static final String ASSISTANT_USERNAME = "assistant1";
	private static final String ASSISTANT_PASSWORD = "assistant1";

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
		final AssignmentView view = openLoginScreen()
				.login(ASSISTANT_USERNAME, ASSISTANT_PASSWORD)
				.listAssistingCourses()
				.get(0).click()
				.listGroups()
				.get(0).click()
				.toAssignmentView()
				.listAssignments()
				.get(0).click();

		final AssignmentView.Assignment assignment = view.getAssignment();
        assertEquals("Student Two", assignment.getAuthor());
		assertEquals("Product vision", assignment.getName());
        assertEquals("Submitted", assignment.getStatus());
    }
	
}
