package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.QAssignment;

import com.google.inject.Inject;

import javax.persistence.EntityManager;

/**
 * Created by jgmeligmeyling on 04/03/15.
 */
public class Assignments extends Controller<Assignment> {

    @Inject
    public Assignments(EntityManager em) {
        super(em);
    }

    public Assignment find(CourseEdition course, Long assignmentId) {
        return ensureNotNull(query().from(QAssignment.assignment)
            .where(QAssignment.assignment.courseEdition.eq(course)
            .and(QAssignment.assignment.assignmentId.eq(assignmentId)))
            .singleResult(QAssignment.assignment),
            "Could not find assignment " + assignmentId + " for " + course.getCode());
    }

    public boolean exists(CourseEdition course, Long assignmentId) {
        return query().from(QAssignment.assignment)
            .where(QAssignment.assignment.courseEdition.eq(course)
            .and(QAssignment.assignment.assignmentId.eq(assignmentId)))
            .exists();
    }

}
