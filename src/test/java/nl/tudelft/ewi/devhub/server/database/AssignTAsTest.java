package nl.tudelft.ewi.devhub.server.database;

import nl.tudelft.ewi.devhub.server.database.controllers.AssignedTAs;
import nl.tudelft.ewi.devhub.server.database.entities.AssignedTA;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by sayra on 07/06/2017.
 */
public class AssignTAsTest {

    Group group1;
    Group group2;
    Group group3;
    Group group4;
    User ta1;
    User ta2;
    List<Group> groups;
    List<User> teachingAssistants;
    Assignment assignment;
    List<AssignedTA> assignedTAS;

    public AssignedTA makeAssignedTA(Assignment assignment, User teachingAssistant, Group group) {
        AssignedTA assignedTA = new AssignedTA();
        assignedTA.setAssignment(assignment);
        assignedTA.setTeachingAssistant(teachingAssistant);
        assignedTA.setGroup(group);

        return assignedTA;
    }

    @Before
    public void setUp() {
        group1 = new Group();
        group1.setGroupNumber(5);

        group2 = new Group();
        group2.setGroupNumber(2);

        group3 = new Group();
        group3.setGroupNumber(3);

        group4 = new Group();
        group4.setGroupNumber(10);

        ta1 = new User();
        ta2 = new User();

        groups = new ArrayList<>();
        groups.add(group1);
        groups.add(group2);
        groups.add(group3);
        groups.add(group4);

        teachingAssistants = new ArrayList<>();
        teachingAssistants.add(ta1);
        teachingAssistants.add(ta2);

        assignment = new Assignment();
    }

    @Test
    public void equalGroupPartitionFirstTATest() {
        AssignedTA assignedTA1Group2 = makeAssignedTA(assignment, ta1, group2);
        AssignedTA assignedTA1Group3 = makeAssignedTA(assignment, ta1, group3);

        assignedTAS = AssignTAs.assignGroups(teachingAssistants, groups, assignment, new Random(42));

        assertThat(assignedTAS).contains(assignedTA1Group2, assignedTA1Group3);
    }

    @Test
    public void equalGroupPartitionSecondTATest() {
        AssignedTA assignedTA2Group1 = makeAssignedTA(assignment, ta2, group1);
        AssignedTA assignedTA2Group4 = makeAssignedTA(assignment, ta2, group4);

        assignedTAS = AssignTAs.assignGroups(teachingAssistants, groups, assignment, new Random(42));

        assertThat(assignedTAS).contains(assignedTA2Group1, assignedTA2Group4);
    }

    @Test
    public void singleGroupAssignedToOneTATest() {
        List<Group> singleGroup = new ArrayList<>();
        singleGroup.add(group2);

        AssignedTA assignedTA1Group2 = makeAssignedTA(assignment, ta1, group2);

        assignedTAS = AssignTAs.assignGroups(teachingAssistants, singleGroup, assignment, new Random(42));

        assertThat(assignedTAS).containsExactly(assignedTA1Group2);
    }

    @Test
    public void shuffledTATest() {
        AssignedTA assignedTA1Group1 = makeAssignedTA(assignment, ta1, group1);
        AssignedTA assignedTA1Group4 = makeAssignedTA(assignment, ta1, group4);

        assignedTAS = AssignTAs.assignGroups(teachingAssistants, groups, assignment, new Random(324452));

        assertThat(assignedTAS).contains(assignedTA1Group1, assignedTA1Group4);
    }

}
