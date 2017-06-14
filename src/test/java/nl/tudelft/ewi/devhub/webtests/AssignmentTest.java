package nl.tudelft.ewi.devhub.webtests;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.*;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AssignmentTest extends WebTest {

    @Inject
    private CourseEditions courseEditions;

    @Inject
    private Deliveries deliveries;

    @Inject
    private Groups groups;

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
                .get(1).click();

        final CourseEdition course = courseEditions.find(1);
        final Group group = groups.find(course).get(0);
        final Delivery modelDelivery = deliveries.find(group, 2L);
        final Assignment modelAssignment = modelDelivery.getAssignment();
        final AssignmentView.Assignment viewAssignment = view.getAssignment();

        assertEquals(modelDelivery.getCreatedUser().getName(), viewAssignment.getAuthor());
        assertEquals(modelAssignment.getName(), viewAssignment.getName());
        assertEquals(modelDelivery.getState(), viewAssignment.getStatus());
    }

    @Test
    public void testReviewedAssignment() throws ParseException {
        final AssignmentView view = openLoginScreen()
                .login(ASSISTANT_USERNAME, ASSISTANT_PASSWORD)
                .listAssistingCourses()
                .get(0).click()
                .listGroups()
                .get(0).click()
                .toAssignmentView()
                .listAssignments()
                .get(0).click();

        final CourseEdition course = courseEditions.find(1);
        final Group group = groups.find(course).get(0);
        final Delivery modelDelivery = deliveries.find(group, 1L);
        final Delivery.Review modelReview = modelDelivery.getReview();
        final AssignmentView.Review viewReview = view.getAssignment().getReview();

        assertEquals(modelReview.getGrade(), viewReview.getGrade().get(), 1e-4);
        assertEquals(modelReview.getReviewUser().getName(), viewReview.getReviewer().get());
        assertEquals(modelReview.getCommentary(), viewReview.getCommentary());
    }

    @Test
    public void testAssignTAs() {
        openLoginScreen()
                .login(ADMIN_USERNAME, ADMIN_PASSWORD)
                .listCourses()
                .get(0).click()
                .listCourseEditions()
                .get(0).click()
                .listAssignments()
                .get(0)
                .goToAssignmentPage()
                .distributeTAs();
    }
}
