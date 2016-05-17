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

import java.text.ParseException;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;

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
