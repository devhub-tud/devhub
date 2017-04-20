package nl.tudelft.ewi.devhub.server.database.entities.rubrics;

import static nl.tudelft.ewi.devhub.server.database.entities.rubrics.CharacteristicTest.characteristicWithOneMastery;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Condition;
import org.assertj.core.util.Lists;
import org.junit.Test;

public class TaskTest {

	@Test
	public void testCopyOfTask() {
		final Task task = taskWithOneCharacteristic();
		final Task newTask = task.copyForNextYear(null);

		assertThat(newTask.getDescription()).isEqualTo("A task");
		assertThat(newTask.getId()).isEqualTo(0);
		assertThat(newTask.getCharacteristics().stream().map(Characteristic::getTask)).are(forTheTask(newTask));
	}

	 public static Task taskWithOneCharacteristic() {
		final Task task = new Task();

		task.setDescription("A task");
		task.setId(4);
		task.setCharacteristics(Lists.newArrayList(characteristicWithOneMastery()));
		return task;
	}

	private static Condition<Task> forTheTask(Task newTask) {
		return new Condition<>(t -> {
			assertThat(t).isSameAs(newTask);
			return true;
		},"It should be the same instance as the newTask");
	}
}
