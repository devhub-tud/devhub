package nl.tudelft.ewi.devhub.server.database.controllers;

import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.PersistedBackendTest;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class AssignmentsTest extends PersistedBackendTest {

	@Inject @Getter private Groups groups;
	@Inject @Getter private CourseEditions courses;
	@Inject @Getter private Users users;
	@Inject Assignments assignments;
	
	@Test
	public void testPersistAssignment() {
		CourseEdition courseEdition = courses.listActiveCourses().get(0);
		Assignment assignment = new Assignment();
		assignment.setName("Part 1: Integration Testing");
		assignment.setCourseEdition(courseEdition);
		assignments.persist(assignment);
	}

}
