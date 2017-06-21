package nl.tudelft.ewi.devhub.webtests;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery.State;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.DeliveryReviewView;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.util.List;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Douwe Koopmans on 9-5-16.
 */
public class AssignmentReviewTest extends WebTest {

    public static final String DELIVERY_COMMENTARY = "Hello World!";

    public static final double INSUFFICIENT_GRADE = 4.0;

    @Inject
    private CourseEditions courseEditions;

    @Inject
    private Deliveries deliveries;

    @Inject
    private EntityManager entityManager;

    @Inject
    private Groups groups;

    @Test
    public void testDeliverable() throws ParseException {
        final DeliveryReviewView view = getDeliveryReviewView();

        final CourseEdition course = courseEditions.find(1);
        final Group group = groups.find(course).get(0);
        final Delivery modelDelivery = deliveries.find(group, 2L);
        final Assignment modelAssignment = modelDelivery.getAssignment();
        final DeliveryReviewView.Assignment viewAssignment = view.getAssignment();

        assertEquals(modelDelivery.getCreatedUser().getName(), viewAssignment.getAuthor());
        assertEquals(modelAssignment.getName(), viewAssignment.getName());
        assertEquals(modelDelivery.getState(), viewAssignment.getStatus());
    }

    @Test
    public void testReviewForm() throws ParseException {
        final DeliveryReviewView view = getDeliveryReviewView();

        final DeliveryReviewView.DeliveryForm deliveryForm = view.getDelivery();
        deliveryForm.setGrade(INSUFFICIENT_GRADE);
        deliveryForm.setState(State.REJECTED);
        deliveryForm.setCommentary(DELIVERY_COMMENTARY);

        assertEquals(INSUFFICIENT_GRADE, deliveryForm.getGrade(), 1e-4);
        assertEquals(Delivery.State.REJECTED, deliveryForm.getSelectedState());
        assertEquals(DELIVERY_COMMENTARY, deliveryForm.getCommentary());
    }

    @Test
    public void testSubmitReview() throws ParseException{
        final DeliveryReviewView view = getDeliveryReviewView();

        final DeliveryReviewView.DeliveryForm deliveryForm = view.getDelivery();
        deliveryForm.setCommentary(DELIVERY_COMMENTARY);
        deliveryForm.setGrade(7.0);
        deliveryForm.setState(State.APPROVED);
        deliveryForm.click();

        entityManager.clear();

        final CourseEdition course = courseEditions.find(1);
        final Group group = groups.find(course).get(0);
        final Delivery modelDelivery = deliveries.find(group, 2L);
        final Assignment modelAssignment = modelDelivery.getAssignment();
        final DeliveryReviewView.Assignment viewAssignment = view.getAssignment();

        assertEquals(modelDelivery.getCreatedUser().getName(), viewAssignment.getAuthor());
        assertEquals(modelAssignment.getName(), viewAssignment.getName());
        assertEquals(modelDelivery.getReview().getState(), viewAssignment.getStatus());
        assertEquals(modelDelivery.getReview().getReviewUser().getName(), viewAssignment.getReview().getReviewer().get());
    }

    @Test
    public void testSubmitDisabledInProgressReview() throws ParseException {
        final DeliveryReviewView view = getDeliveryReviewView(1, 0);

        final DeliveryReviewView.DeliveryForm deliveryForm = view.getDelivery();
        deliveryForm.setCommentary(DELIVERY_COMMENTARY);

        assertTrue("Submit button should not be clickable", deliveryForm.isSubmitButtonDisabled());
    }

    @Test
    public void testSubmitEnabledAfterFullReview() throws ParseException {
        final DeliveryReviewView view = getDeliveryReviewView(1, 0);

        final DeliveryReviewView.DeliveryForm deliveryForm = view.getDelivery();
        deliveryForm.setCommentary(DELIVERY_COMMENTARY);

        List<WebElement> masteries = deliveryForm.getFirstMasteryForEachCharacteristic();
        for (int i = 0; i < masteries.size(); i++) {
            // For every input field, excluding the last one, the submit button should still be disabled
            if (i < masteries.size()) {
                assertTrue("Submit button should not be clickable", deliveryForm.isSubmitButtonDisabled());
            }

            masteries.get(i).click();
        }

        // Filling in all masteries should make the submit button clickable
        assertFalse("Submit button should be clickable", deliveryForm.isSubmitButtonDisabled());
    }

    private DeliveryReviewView getDeliveryReviewView() throws ParseException {
        return getDeliveryReviewView(0, 1);
    }

    private DeliveryReviewView getDeliveryReviewView(int groupId, int assignmentId) throws ParseException {
        return openLoginScreen()
                .login(ASSISTANT_USERNAME, ASSISTANT_PASSWORD)
                .listAssistingCourses()
                .get(0).click()
                .listGroups()
                .get(groupId).click()
                .toAssignmentView()
                .listAssignments()
                .get(assignmentId).click()
                .getAssignment().click();
    }
}
