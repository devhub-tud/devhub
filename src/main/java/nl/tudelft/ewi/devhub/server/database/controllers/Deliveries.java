package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Group;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import javax.persistence.EntityManager;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mysema.query.group.GroupBy.groupBy;
import static com.mysema.query.group.GroupBy.list;
import static nl.tudelft.ewi.devhub.server.database.entities.Delivery.State;
import static nl.tudelft.ewi.devhub.server.database.entities.QDelivery.delivery;

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
        return query().from(delivery)
            .where(delivery.assignment.eq(assignment))
            .where(delivery.group.eq(group))
            .orderBy(delivery.timestamp.desc())
            .singleResult(delivery);
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
        return query().from(delivery)
			.where(delivery.assignment.eq(assignment)
				.and(delivery.group.eq(group))
				.and(delivery.review.state.in(State.APPROVED, State.DISAPPROVED)))
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
        return query().from(delivery)
			.where(delivery.assignment.eq(assignment)
				.and(delivery.group.eq(group)))
			.orderBy(delivery.timestamp.desc())
			.list(delivery);
    }

    /**
     * Get the most recent delivery for every group in this assignment
     * @param assignment current assignment
     * @return a list of deliveries
     */
    @Transactional
    public List<Delivery> getLastDeliveries(Assignment assignment) {
        Map<Group, List<Delivery>> deliveriesMap = query().from(delivery)
			.where(delivery.assignment.eq(assignment))
			.orderBy(delivery.timestamp.desc())
            .transform(groupBy(delivery.group).as(list(delivery)));

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
		return ensureNotNull(query().from(delivery)
				.where(delivery.deliveryId.eq(deliveryId)
					.and(delivery.group.eq(group)))
				.singleResult(delivery),
            "No delivery found for id " + deliveryId);
    }

    /**
     * Return the most recent deliveries.
     * @param groups Groups to search deliveries for.
     * @param limit Maximal number of results.
     * @return A list of deliveries.
     */
    @Transactional
    public Stream<Delivery> getMostRecentDeliveries(List<Group> groups, long limit) {
        Map<Group, List<Delivery>> deliveriesMap = query().from(delivery)
            .where(delivery.group.in(groups))
            .orderBy(delivery.timestamp.desc())
            .limit(limit)
            .transform(groupBy(delivery.group).as(list(delivery)));

        Comparator<Delivery> byState = Comparator.comparing(Delivery::getState);
        Comparator<Delivery> bySubmissionDate = Comparator.<Delivery> naturalOrder();

        return deliveriesMap.values().stream().map((deliveries) ->
            deliveries.stream().max(bySubmissionDate).get())
            .sorted(byState.thenComparing(bySubmissionDate));
    }
}
