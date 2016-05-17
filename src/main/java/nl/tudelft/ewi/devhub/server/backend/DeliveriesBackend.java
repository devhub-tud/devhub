package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery.Review;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery.State;
import nl.tudelft.ewi.devhub.server.database.entities.DeliveryAttachment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;

import org.apache.commons.lang.StringUtils;

import javax.ws.rs.NotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The DeliveriesBackend is used to create and update Deliveries.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@RequestScoped
public class DeliveriesBackend {

	/**
	 * Error code when the delivery could not be stored.
	 */
    private static final String ERROR_COULD_NOT_DELIVER = "error.could-not-deliver";
    
    /**
     * Error message when the subbmission already had a (dis)approved submission.
     */
	private static final String ALREADY_SUBMITTED = "Cannot submit because there is a (dis)approved submission";
	
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
     * Persist a delivery.
     * 
     * @param delivery
     * 		Delivery to persist
     * @throws ApiError
     * 		If there an exception occurred while persisting the delivery
     * @throws UnauthorizedException 
     * 		When the User is not allowed to deliver for the project
     */
    @Transactional
    public void deliver(Delivery delivery) throws ApiError, UnauthorizedException {
        Group group = delivery.getGroup();
        Assignment assignment = delivery.getAssignment();
        checkUserAuthorized(group);

        if (deliveriesDAO.lastDeliveryIsApprovedOrDisapproved(assignment, group)) {
            throw new IllegalStateException(ALREADY_SUBMITTED);
        }

        Optional<Delivery> optionalLastDelivery = deliveriesDAO.getLastDelivery(assignment, group);

        if (optionalLastDelivery.isPresent()) {
            Delivery lastDelivery = optionalLastDelivery.get();
            // Copy review from last delivery
            Review lastReview = lastDelivery.getReview();
            if (lastReview != null) {
                Review review = new Review();
                review.setCommentary(lastReview.getCommentary());
                review.setReviewTime(lastReview.getReviewTime());
                review.setReviewUser(lastReview.getReviewUser());
                review.setState(State.SUBMITTED);
                delivery.setReview(review);
            }

            // Copy the rubrics from the last delivery
            delivery.setRubrics(Maps.newHashMap(lastDelivery.getRubrics()));
        }

        try {
            delivery.setCreatedUser(currentUser);
            deliveriesDAO.persist(delivery);
            log.info("{} submitted {}", currentUser, delivery);
        }
        catch (Exception e) {
            throw new ApiError(ERROR_COULD_NOT_DELIVER, e);
        }
    }

    /**
     * Attach an DeliveryAttachment to a Delivery.
     * 
     * @param delivery
     * 		Delivery to add an attachment for
     * @param fileName
     * 		Filename for the attachment
     * @param in
     * 		InputStream providing the file contents
     * @throws ApiError
     * 		When an exception occurred while persisting the delivery
     * @throws UnauthorizedException 
     * 		When the User is not allowed to deliver for the project
     */
    @Transactional
    public void attach(Delivery delivery, String fileName, InputStream in)
    		throws ApiError, UnauthorizedException {
        Group group = delivery.getGroup();
        checkUserAuthorized(group);

        List<DeliveryAttachment> attachments = delivery.getAttachments();
        
        if (attachments == null) {
            attachments = Lists.<DeliveryAttachment> newArrayList();
            delivery.setAttachments(attachments);
        }

        String folderName = StringUtils.join(new Object[] {
            group.getCourseEdition().getCourse().getCode(),
            group.getCourseEdition().getCode(),
            group.getGroupNumber(),
            delivery.getDeliveryId()
        }, File.separatorChar);

        storeDeliveryAttachments(delivery, fileName, in, attachments,
				folderName);
    }

	private void storeDeliveryAttachments(Delivery delivery, String fileName,
			InputStream in, List<DeliveryAttachment> attachments,
			String folderName) throws ApiError {
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
                throw new ApiError(ERROR_COULD_NOT_DELIVER, e);
            }
        }
        catch (IOException e) {
            throw new ApiError(ERROR_COULD_NOT_DELIVER, e);
        }
	}

    private void checkUserAuthorized(final Group group) throws UnauthorizedException {
        assert group != null : "Group should not be null";

        if (!(currentUser.isAdmin()
        		|| currentUser.isAssisting(group.getCourseEdition())
        		|| group.getMembers().contains(currentUser))) {
            throw new UnauthorizedException();
        }
    }

    /**
     * Persist a review for a delivery.
     * 
     * @param delivery
     * 		delivery to review
     * @param review
     * 		review for delivery
     * @throws ApiError 
     * 		When an exception occurred while persisting the review
     * @throws UnauthorizedException
     * 		When the User is not allowed to review for the project
     */
    @Transactional
    public void review(Delivery delivery, Delivery.Review review)
    		throws ApiError, UnauthorizedException {
        Group group = delivery.getGroup();
        
        if (!(currentUser.isAdmin() || currentUser.isAssisting(group.getCourseEdition()))) {
            throw new UnauthorizedException();
        }

        try {
            review.setReviewUser(currentUser);
            review.setReviewTime(new Date());
            delivery.setReview(review);
            deliveriesDAO.merge(delivery);
            log.info("{} reviewed {}", currentUser, delivery);
        }
        catch (Exception e) {
            throw new ApiError("error.could-not-review", e);
        }
    }

    /**
     * Get the attachment for an assignment.
     * 
     * @param delivery
     * 		Delivery to get the attachment for
     * @param group
     * 		Group to get the assignment path for
     * @param attachmentPath
     * 		path to the file
     * @return the File for the assignment
     * @throws UnauthorizedException
     * 		When the file does not belong to the assignment or group
     * @throws NotFoundException
     * 		When the file could not be found
     */
    public File getAttachment(Delivery delivery, Group group, String attachmentPath)
    		throws UnauthorizedException, NotFoundException {
        checkUserAuthorized(group);
        File file = storageBackend.getFile(attachmentPath);

        // VERY IMPORTANT
        //   VERIFY THAT FILE ACTUALLY BELONGS TO THIS DELIVERY
        if (delivery.getAttachments().stream().noneMatch((attachment) ->
                attachment.getPath().equals(attachmentPath))) {
            throw new UnauthorizedException();
        }

        return file;
    }

    /**
     * Get the AssignmentStats for an Assignment.
     * 
     * @param assignment
     * 		The assignment to get the statistics for
     * @return statistics for the assignment
     */
    public AssignmentStats getAssignmentStats(Assignment assignment) {
        List<Delivery> lastDeliveries = deliveriesDAO.getLastDeliveries(assignment);
        return getAssignmentStats(assignment, lastDeliveries);
    }

    public AssignmentStats getAssignmentStats(Assignment assignment, List<Delivery> lastDeliveries) {
        return new AssignmentStats(lastDeliveries, assignment.getCourseEdition().getGroups());
    }

}
