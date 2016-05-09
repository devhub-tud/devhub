package nl.tudelft.ewi.devhub.server.database.controllers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.PersistedBackendTest;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.IOException;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.iterableWithSize;

@Slf4j
@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class AssignmentsTest extends PersistedBackendTest {

	@Inject @Getter private Groups groups;
	@Inject @Getter private CourseEditions courses;
	@Inject @Getter private Users users;
	@Inject Assignments assignments;
	@Inject ObjectMapper objectMapper;
	@Inject EntityManager entityManager;

	@Test
	public void testPersistAssignment() throws IOException {
		CourseEdition courseEdition = courses.listActiveCourses().get(0);

		Assignment assignment = objectMapper.readValue(
			AssignmentsTest.class.getResourceAsStream("/assignment-with-tasks.json"),
			Assignment.class
		);

		assignment.setCourseEdition(courseEdition);
		assignments.persist(assignment);
		log.info("Persisted {}", assignment);
		entityManager.clear();

		assignment = assignments.find(courseEdition, assignment.getAssignmentId());
		assertThat(assignment.getTasks(), iterableWithSize(3));
	}

}
