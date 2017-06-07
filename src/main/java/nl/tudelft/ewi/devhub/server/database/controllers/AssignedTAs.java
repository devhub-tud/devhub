package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.Inject;
import com.mysema.query.jpa.JPASubQuery;
import nl.tudelft.ewi.devhub.server.database.entities.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mysema.query.group.GroupBy.groupBy;
import static com.mysema.query.group.GroupBy.list;
import static java.util.Comparator.comparing;
import static nl.tudelft.ewi.devhub.server.database.entities.QAssignedTA.assignedTA;
import static nl.tudelft.ewi.devhub.server.database.entities.QDelivery.delivery;

/**
 * Created by sayra on 07/06/2017.
 */
public class AssignedTAs extends Controller<AssignedTA> {

    @Inject
    public AssignedTAs(EntityManager em) {
        super(em);
    }

    @Transactional
    public List<Delivery> getLastDeliveries(Assignment assignment, User user) {
        Map<Group, List<Delivery>> deliveriesMap = query().from(delivery)
                .where(delivery.assignment.eq(assignment)
                        .and(delivery.group.in(
                                new JPASubQuery()
                                        .from(assignedTA)
                                        .where(assignedTA.assignment.eq(assignment)
                                                .and(assignedTA.teachingAssistant.eq(user)))
                                        .list(assignedTA.group))))
                .orderBy(delivery.timestamp.desc())
                .transform(groupBy(delivery.group).as(list(delivery)));

        Comparator<Delivery> byState = comparing(Delivery::getState);
        Comparator<Delivery> bySubmissionDate = Comparator.<Delivery>naturalOrder();

        return deliveriesMap.values().stream().map((deliveries) ->
                deliveries.stream().max(bySubmissionDate).get())
                .sorted(byState.thenComparing(bySubmissionDate))
                .collect(Collectors.toList());
    }
}
