package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.Inject;
import static com.mysema.query.group.GroupBy.*;;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.*;

import javax.persistence.EntityManager;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        return deliveriesMap.values().stream().map((deliveries) ->
                deliveries.stream().min(Comparator.<Delivery> naturalOrder()).get())
            .collect(Collectors.toList());
    }

    /**
     * Find delivery by id
     * @param deliveryId id for delivery
     * @return Delivery for id
     */
    @Transactional
    public Delivery find(Long deliveryId) {
        return ensureNotNull(query().from(QDelivery.delivery)
                .where(QDelivery.delivery.deliveryId.eq(deliveryId))
                .singleResult(QDelivery.delivery),
            "No delivery found for id " + deliveryId);
    }
}
