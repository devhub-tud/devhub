package nl.tudelft.ewi.devhub.server.database.entities;

import static nl.tudelft.ewi.devhub.server.database.entities.rubrics.TaskTest.taskWithOneCharacteristic;
import static org.assertj.core.api.Assertions.assertThat;

import nl.tudelft.ewi.devhub.server.database.entities.rubrics.Task;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.List;

public class AssignmentUnitTest {

	@Test
	public void testAssignmentCopy() {
		final Assignment assignment = assignmentWithOneTask();
		CourseEdition courseEdition = new CourseEdition();
		Assignment newAssignment = new Assignment();

		List<Task> tasks = newAssignment.copyTasksFromOldAssignment(assignment);

		assertThat(tasks).allMatch(t -> t.getAssignment().equals(newAssignment));
	}

	private Assignment assignmentWithOneTask() {
		final Assignment assignment = new Assignment();

		assignment.setTasks(Lists.newArrayList(taskWithOneCharacteristic()));
		return assignment;
	}
}
