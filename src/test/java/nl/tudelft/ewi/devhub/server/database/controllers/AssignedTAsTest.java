package nl.tudelft.ewi.devhub.server.database.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.PersistedBackendTest;
import nl.tudelft.ewi.devhub.server.database.entities.AssignedTA;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class AssignedTAsTest extends PersistedBackendTest {

    @Inject @Getter private Groups groups;
    @Inject @Getter private CourseEditions courses;
    @Inject @Getter private Users users;
    @Inject Assignments assignments;
    @Inject AssignedTAs assignedTAs;
    @Inject Deliveries deliveries;
    @Inject ObjectMapper objectMapper;
    Assignment assignment;
    Delivery delivery;
    User assistant;

    @Before
    public void setUp() throws Exception {
        CourseEdition courseEdition = courses.listActiveCourses().get(0);

        User student = createUser();
        assistant = createUser();
        Group group = createGroup(courseEdition, student);

        assignment = objectMapper.readValue(
            AssignmentsTest.class.getResourceAsStream("/assignment-with-tasks.json"),
            Assignment.class
        );
        assignment.setCourseEdition(courseEdition);
        assignments.persist(assignment);
        assignments.refresh(assignment);

        createDelivery(student, group, assignment);
        Thread.sleep(100);
        delivery = createDelivery(student, group, assignment);
        assignTA(group, assignment, assistant);
    }

    private void assignTA(Group group, Assignment assignment, User assistant) {
        AssignedTA assignedTA = new AssignedTA();
        assignedTA.setTeachingAssistant(assistant);
        assignedTA.setAssignment(assignment);
        assignedTA.setGroup(group);
        assignedTAs.persist(assignedTA);
        assignedTAs.refresh(assignedTA);
    }

    private Delivery createDelivery(User user, Group group, Assignment assignment) {
        Delivery delivery = new Delivery();
        delivery.setAssignment(assignment);
        delivery.setGroup(group);
        delivery.setCreatedUser(user);
        delivery.setStudents(Sets.newHashSet(Lists.newArrayList(user)));
        deliveries.persist(delivery);
        deliveries.refresh(delivery);
        return delivery;
    }

    @Test
    public void getLastDeliveriesTest() throws IOException {
        assertThat(
            assignedTAs.getLastDeliveries(assignment, assistant),
            contains(delivery)
        );
    }


}
