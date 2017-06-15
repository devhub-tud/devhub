package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.Inject;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.types.query.ListSubQuery;
import nl.tudelft.ewi.devhub.server.database.entities.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.partition;
import static com.mysema.query.group.GroupBy.groupBy;
import static com.mysema.query.group.GroupBy.list;
import static java.util.Collections.shuffle;
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
                        .and(delivery.group.groupNumber.in(getLastDeliveriesSubQuery(assignment, user))))
                .orderBy(delivery.timestamp.desc())
                .transform(groupBy(delivery.group).as(list(delivery)));

        Comparator<Delivery> byState = comparing(Delivery::getState);
        Comparator<Delivery> bySubmissionDate = Comparator.<Delivery>naturalOrder();

        return deliveriesMap.values().stream().map((deliveries) ->
                deliveries.stream().max(bySubmissionDate).get())
                .sorted(byState.thenComparing(bySubmissionDate))
                .collect(Collectors.toList());
    }

    private static ListSubQuery<Long> getLastDeliveriesSubQuery(Assignment assignment, User user) {
        return new JPASubQuery().from(assignedTA)
            .where(assignedTA.assignment.eq(assignment)
                    .and(assignedTA.teachingAssistant.eq(user)))
            .list(assignedTA.group.groupNumber);
    }

    public static List<AssignedTA> assignGroups(Collection<User> TAs, Collection<Group> groupList, Assignment assignment, Random random) {
        List<AssignedTA> assignedTAs = new ArrayList<>();
        List<Group> groups = new ArrayList<>(groupList);
        List<User> teachingAssistants = new ArrayList<>(TAs);
        int partitionSize = (int) Math.ceil((double) groups.size() / teachingAssistants.size());

        groups.sort(Comparator.comparing(Group::getGroupNumber));
        shuffle(teachingAssistants, random);

        if(! groups.isEmpty()) {
            List<List<Group>> groupPartitions = partition(groups, partitionSize);

            for(int i = 0; i < teachingAssistants.size(); i++) {
                if(i < groupPartitions.size()) {
                    for (Group group : groupPartitions.get(i)) {
                        AssignedTA ta = new AssignedTA();
                        ta.setTeachingAssistant(teachingAssistants.get(i));
                        ta.setGroup(group);
                        ta.setAssignment(assignment);
                        assignedTAs.add(ta);
                    }
                }
            }
        }

        return assignedTAs;
    }

}
