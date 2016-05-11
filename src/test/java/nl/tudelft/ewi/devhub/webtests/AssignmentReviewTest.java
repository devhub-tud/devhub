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
import nl.tudelft.ewi.devhub.webtests.views.DeliveryReviewView;
import org.junit.Test;

import java.text.ParseException;

import javax.persistence.EntityManager;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by Douwe Koopmans on 9-5-16.
 */
public class AssignmentReviewTest extends WebTest{
    private static final String ASSISTANT_USERNAME = "assistant1";
    private static final String ASSISTANT_PASSWORD = "assistant1";

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
        final Delivery modelDelivery = deliveries.find(group,2L);
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
        deliveryForm.setGrade("4.0");
        deliveryForm.setState("Rejected");
        deliveryForm.setCommentary("Hello World!");

        assertTrue(deliveryForm.getGrade() == 4.0D);
        assertEquals(Delivery.State.REJECTED, deliveryForm.getSelectedState());
        assertEquals("Hello World!", deliveryForm.getCommentary());
    }

    @Test
    public void testSubmitReview() throws ParseException{
        final DeliveryReviewView view = getDeliveryReviewView();

        final DeliveryReviewView.DeliveryForm deliveryForm = view.getDelivery();
        deliveryForm.setCommentary("Hello World!");
        deliveryForm.setGrade("7.0");
        deliveryForm.setState("Approved");
        deliveryForm.click();

        entityManager.clear();

        final CourseEdition course = courseEditions.find(1);
        final Group group = groups.find(course).get(0);
        final Delivery modelDelivery = deliveries.find(group,2L);
        final Assignment modelAssignment = modelDelivery.getAssignment();
        final DeliveryReviewView.Assignment viewAssignment = view.getAssignment();

        assertEquals(modelDelivery.getCreatedUser().getName(), viewAssignment.getAuthor());
        assertEquals(modelAssignment.getName(), viewAssignment.getName());
//        assertEquals(modelDelivery.getReview().getState(), viewAssignment.getStatus());
        assertEquals(modelDelivery.getReview().getReviewUser().getName(), viewAssignment.getReview().getReviewer());
    }

    private DeliveryReviewView getDeliveryReviewView() throws ParseException {
        return openLoginScreen()
                .login(ASSISTANT_USERNAME, ASSISTANT_PASSWORD)
                .listAssistingCourses()
                .get(0).click()
                .listGroups()
                .get(0).click()
                .toAssignmentView()
                .listAssignments()
                .get(1).click()
                .getAssignment().click();
    }
}
