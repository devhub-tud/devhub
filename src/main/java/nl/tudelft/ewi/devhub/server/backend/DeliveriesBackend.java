package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.DeliveryAttachment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * The DeliveriesBackend is used to create and update Deliveries
 *
 * @author Jan-Willem Gmelig Meyling
 */
@RequestScoped
public class DeliveriesBackend {

    private final User currentUser;
    private final Deliveries deliveries;
    private final StorageBackend storageBackend;

    @Inject
    public DeliveriesBackend(Deliveries deliveries,
                             StorageBackend storageBackend,
                             @Named("current.user") User currentUser) {
        this.deliveries = deliveries;
        this.storageBackend = storageBackend;
        this.currentUser = currentUser;
    }

    /**
     * Persist a delivery
     * @param delivery Delivery to persist
     * @throws ApiError if there an exception occurred while persisting the delivery
     * @throws UnauthorizedException if the User is not allowed to deliver for the project
     */
    @Transactional
    public void deliver(Delivery delivery) throws ApiError, UnauthorizedException {
        Group group = delivery.getGroup();
        checkUserAuthorized(group);

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
     * Attach an DeliveryAttachment to a Delivery
     * @param delivery Delivery to add an attachment for
     * @param fileName Filename for the attachment
     * @param in InputStream providing the file contents
     * @throws ApiError if there an exception occurred while persisting the delivery
     * @throws UnauthorizedException if the User is not allowed to deliver for the project
     */
    @Transactional
    public void attach(Delivery delivery, String fileName, InputStream in) throws ApiError, UnauthorizedException {
        Group group = delivery.getGroup();
        checkUserAuthorized(group);

        List<DeliveryAttachment> attachments = delivery.getAttachments();
        if(attachments == null) {
            attachments = Lists.<DeliveryAttachment> newArrayList();
            delivery.setAttachments(attachments);
        }

        String folderName = StringUtils.join(new Object[] {
            group.getCourse().getCode(),
            group.getGroupNumber(),
            delivery.getDeliveryId()
        }, File.separatorChar);

        try {
            String path = storageBackend.store(folderName, fileName, in);
            DeliveryAttachment attachment = new DeliveryAttachment();
            attachment.setDelivery(delivery);
            attachment.setPath(path);
            attachments.add(attachment);

            try {
                deliveries.merge(delivery);
            }
            catch (Exception e) {
                storageBackend.removeSilently(path, fileName);
                throw new ApiError("error.could-not-deliver", e);
            }
        }
        catch (IOException e) {
            throw new ApiError("error.could-not-deliver", e);
        }
    }

    private void checkUserAuthorized(final Group group) throws UnauthorizedException {
        assert group != null : "Group should not be null";

        if(!(currentUser.isAdmin() ||
                currentUser.isAssisting(group.getCourse()) ||
                group.getMembers().contains(currentUser))) {
            throw new UnauthorizedException();
        }
    }

    /**
     * Persist a review for a delivery
     * @param delivery delivery to review
     * @param review review for delivery
     * @throws ApiError if there an exception occurred while persisting the review
     * @throws UnauthorizedException if the User is not allowed to review for the project
     */
    @Transactional
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
