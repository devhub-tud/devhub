package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.QDelivery;

import javax.persistence.EntityManager;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mysema.query.group.GroupBy.groupBy;
import static com.mysema.query.group.GroupBy.list;
import static nl.tudelft.ewi.devhub.server.database.entities.Delivery.State;

/**
 * Created by jgmeligmeyling on 04/03/15.
 */
public class Deliveries extends Controller<Delivery> {

    @Inject
    public Deliveries(EntityManager em) {
        super(em);
    }

    /**
     * Get the last delivery for a group
     * @param assignment assignment to look for
     * @param group group to look for
     * @return most recent delivery or null if not exists
     */
    @Transactional
    public Delivery getLastDelivery(Assignment assignment, Group group) {
        return query().from(QDelivery.delivery)
            .where(QDelivery.delivery.assignment.eq(assignment))
            .where(QDelivery.delivery.group.eq(group))
            .orderBy(QDelivery.delivery.created.desc())
            .singleResult(QDelivery.delivery);
    }

    /**
     * Check if an approved or disapproved submission exists.
     * No further submissions should be made if true.
     * @param assignment Assignment to check for
     * @param group Group to check for
     * @return True if an approved or disapproved submission exists
     */
    @Transactional
    public boolean lastDeliveryIsApprovedOrDisapproved(Assignment assignment, Group group) {
        QDelivery Delivery = QDelivery.delivery;
        return query().from(Delivery)
            .where(Delivery.assignment.eq(assignment)
            .and(Delivery.group.eq(group))
            .and(Delivery.review.state.in(State.APPROVED, State.DISAPPROVED)))
            .exists();
    }

    /**
     * Get all deliveries for a group
     * @param assignment assignment to look for
     * @param group group to look for
     * @return list of deliveries
     */
    @Transactional
    public List<Delivery> getDeliveries(Assignment assignment, Group group) {
        return query().from(QDelivery.delivery)
            .where(QDelivery.delivery.assignment.eq(assignment))
            .where(QDelivery.delivery.group.eq(group))
            .orderBy(QDelivery.delivery.created.desc())
                .list(QDelivery.delivery);
    }

    /**
     * Get the most recent delivery for every group in this assignment
     * @param assignment current assignment
     * @return a list of deliveries
     */
    @Transactional
    public List<Delivery> getLastDeliveries(Assignment assignment) {
        Map<Group, List<Delivery>> deliveriesMap = query().from(QDelivery.delivery)
            .where(QDelivery.delivery.assignment.eq(assignment))
            .orderBy(QDelivery.delivery.created.desc())
            .transform(groupBy(QDelivery.delivery.group).as(list(QDelivery.delivery)));

        Comparator<Delivery> byState = Comparator.comparing(Delivery::getState);
        Comparator<Delivery> bySubmissionDate = Comparator.<Delivery> naturalOrder();

        return deliveriesMap.values().stream().map((deliveries) ->
                deliveries.stream().max(bySubmissionDate).get())
            .sorted(byState.thenComparing(bySubmissionDate))
            .collect(Collectors.toList());
    }

    /**
     * Find delivery by id
     * @param deliveryId id for delivery
     * @return Delivery for id
     */
    @Transactional
    public Delivery find(Group group, long deliveryId) {
        return ensureNotNull(query().from(QDelivery.delivery)
                .where(QDelivery.delivery.deliveryId.eq(deliveryId)
                .and(QDelivery.delivery.group.eq(group)))
                .singleResult(QDelivery.delivery),
            "No delivery found for id " + deliveryId);
    }
}
