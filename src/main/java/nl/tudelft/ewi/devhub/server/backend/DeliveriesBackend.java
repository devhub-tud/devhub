package nl.tudelft.ewi.devhub.server.backend;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import org.eclipse.jetty.util.annotation.Name;

import java.util.Date;

/**
 * Created by jgmeligmeyling on 04/03/15.
 */
@RequestScoped
public class DeliveriesBackend {

    private final User currentUser;
    private final Deliveries deliveries;

    @Inject
    public DeliveriesBackend(Deliveries deliveries,
                             @Named("current.user") User currentUser) {
        this.deliveries = deliveries;
        this.currentUser = currentUser;
    }

    /**
     * Persist a delivery
     * @param delivery Delivery to persist
     * @throws ApiError if there an exception occurred while persisting the delivery
     * @throws UnauthorizedException if the User is not allowed to deliver for the project
     */
    public void deliver(Delivery delivery) throws ApiError, UnauthorizedException {
        Group group = delivery.getGroup();
        if(!(currentUser.isAdmin() ||
                currentUser.isAssisting(group.getCourse()) ||
                group.getMembers().contains(currentUser))) {
            throw new UnauthorizedException();
        }

        try {
            delivery.setCreatedUser(currentUser);
            delivery.setCreated(new Date());
            deliveries.persist(delivery);
        }
        catch (Exception e) {
            throw new ApiError("error.could-not-deliver", e);
        }
    }

    /**
     * Persist a review for a delivery
     * @param delivery delivery to review
     * @param review review for delivery
     * @throws ApiError if there an exception occurred while persisting the review
     * @throws UnauthorizedException if the User is not allowed to review for the project
     */
    public void review(Delivery delivery, Delivery.Review review) throws ApiError, UnauthorizedException {
        Group group = delivery.getGroup();
        if(!(currentUser.isAdmin() || currentUser.isAssisting(group.getCourse()))) {
            throw new UnauthorizedException();
        }

        try {
            review.setReviewUser(currentUser);
            review.setReviewTime(new Date());
            delivery.setReview(review);
            deliveries.merge(delivery);
        }
        catch (Exception e) {
            throw new ApiError("error.could-not-review", e);
        }
    }

}
