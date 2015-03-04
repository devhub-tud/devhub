package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.QAssignment;

import javax.persistence.EntityManager;

/**
 * Created by jgmeligmeyling on 04/03/15.
 */
public class Assignments extends Controller<Assignment> {

    @Inject
    public Assignments(EntityManager em) {
        super(em);
    }

    public Assignment find(Course course, Long assignmentId) {
        return ensureNotNull(query().from(QAssignment.assignment)
            .where(QAssignment.assignment.course.eq(course)
                    .and(QAssignment.assignment.assignmentId.eq(assignmentId)))
            .singleResult(QAssignment.assignment),
            "Could not find assignment " + assignmentId + " for " + course.getCode());
    }

}
