package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.entities.*;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.NotFoundException;
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
    private final Deliveries deliveriesDAO;
    private final StorageBackend storageBackend;

    @Inject
    public DeliveriesBackend(Deliveries deliveriesDAO,
                             StorageBackend storageBackend,
                             @Named("current.user") User currentUser) {
        this.deliveriesDAO = deliveriesDAO;
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
        Assignment assignment = delivery.getAssignment();
        checkUserAuthorized(group);

        if(deliveriesDAO.lastDeliveryIsApprovedOrDisapproved(assignment, group)) {
            throw new IllegalStateException("Cannot submit because there is a (dis)approved submission");
        }

        try {
            delivery.setCreatedUser(currentUser);
            delivery.setCreated(new Date());
            deliveriesDAO.persist(delivery);
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
                deliveriesDAO.merge(delivery);
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
            deliveriesDAO.merge(delivery);
        }
        catch (Exception e) {
            throw new ApiError("error.could-not-review", e);
        }
    }

    /**
     * Get the attachment for an assignment
     * @param assignment Assignment to get the attachment for
     * @param group Group to get the assignment path for
     * @param attachmentPath path to the file
     * @return the File for the assignment
     * @throws UnauthorizedException if the file does not belong to the assignment or group
     * @throws NotFoundException if the file could not be found
     */
    public File getAttachment(Assignment assignment, Group group, String attachmentPath) throws UnauthorizedException, NotFoundException {
        checkUserAuthorized(group);
        File file = storageBackend.getFile(attachmentPath);
        List<Delivery> deliveriesForAssignment = deliveriesDAO.getDeliveries(assignment, group);

        // VERY IMPORTANT
        //   VERIFY THAT FILE ACTUALLY BELONGS TO THIS ASSIGNMENT
        if(deliveriesForAssignment.stream().noneMatch((delivery) ->
            delivery.getAttachments().stream().anyMatch((attachment) ->
                attachment.getPath().equals(attachmentPath)))) {
            throw new UnauthorizedException();
        }

        return file;
    }

    /**
     * Get the AssignmentStats for an Assignment
     * @param assignment the assignment to get the statistics for
     * @return statistics for the assignment
     */
    public AssignmentStats getAssignmentStats(Assignment assignment) {
        List<Delivery> deliveries = deliveriesDAO.getLastDeliveries(assignment);
        return new AssignmentStats(deliveries, assignment.getCourse().getGroups());
    }

}
