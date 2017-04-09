package nl.tudelft.ewi.devhub.server.database.entities;

import static nl.tudelft.ewi.devhub.server.database.entities.rubrics.TaskTest.taskWithOneCharacteristic;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.util.Lists;
import org.junit.Test;

public class AssignmentUnitTest {

	@Test
	public void testAssignmentCopy() {
		final Assignment assignment = assignmentWithOneTask();
		CourseEdition courseEdition = new CourseEdition();
		final Assignment newAssignment = assignment.copyForNextYear(courseEdition, 1);

		assertThat(newAssignment.getAssignmentId()).isEqualTo(1);
		assertThat(newAssignment.getCourseEdition()).isSameAs(courseEdition);
		assertThat(newAssignment.isGradesReleased()).isFalse();
	}

	private Assignment assignmentWithOneTask() {
		final Assignment assignment = new Assignment();

		assignment.setTasks(Lists.newArrayList(taskWithOneCharacteristic()));
		return assignment;
	}
}
