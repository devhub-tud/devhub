package nl.tudelft.ewi.devhub.server.database;

import nl.tudelft.ewi.devhub.server.database.entities.AssignedTA;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.partition;
import static java.util.Collections.shuffle;

/**
 * Created by sayra on 07/06/2017.
 */
public class AssignTAs {
    public static List<AssignedTA> assignGroups(Collection<User> TAs, Collection<Group> groupList, Assignment assignment, Random random) {
        List<AssignedTA> assignedTAs = new ArrayList<>();

        List<Group> groups = new ArrayList<>();
        groups.addAll(groupList);

        List<User> teachingAssistants = new ArrayList<>();
        teachingAssistants.addAll(TAs);

        groups.sort(Comparator.comparing(Group::getGroupNumber));
        shuffle(teachingAssistants, random);

        int partitionSize = (int) Math.ceil((double) groups.size() / teachingAssistants.size());

        if(groups.isEmpty()) {
            return assignedTAs;
        }

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

        return assignedTAs;
    }


}
