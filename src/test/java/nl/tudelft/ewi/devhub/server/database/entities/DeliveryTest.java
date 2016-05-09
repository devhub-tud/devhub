package nl.tudelft.ewi.devhub.server.database.entities;

import nl.tudelft.ewi.devhub.server.database.entities.rubrics.Mastery;
import nl.tudelft.ewi.devhub.server.database.entities.rubrics.Task;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by Jan-Willem on 5/5/2016.
 */
public class DeliveryTest {

	private Delivery delivery;
	private Assignment assignment;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final AtomicInteger atomicInteger = new AtomicInteger(0);

	@Before
	public void setUp() throws IOException {
		assignment = objectMapper.readValue(
			DeliveryTest.class.getResourceAsStream("/assignment-with-tasks.json"),
			Assignment.class
		);
		createCharacteristicIds();
		delivery = new Delivery();
		delivery.setAssignment(assignment);
	}

	@Test
	public void basicTest() {
		List<Mastery> masteries = Arrays.asList(
			correctSmokeTest(),
			reasonablyWellStepDefs(),
			consecutiveBuildFailures()
		);

		delivery.setRubrics(
			masteries.stream().collect(Collectors.toMap(
				Mastery::getCharacteristic,
				Function.identity()
			))
		);

		assertEquals(4*3+6*3, assignment.getNumberOfAchievablePoints(), 1e-10);
		assertEquals(4*3+6*2-3, delivery.getAchievedNumberOfPoints(), 1e-10);
	}

	public Mastery correctSmokeTest() {
		return assignment.getTasks().get(0)
			.getCharacteristics().get(0)
			.getLevels().get(2);
	}

	private Mastery reasonablyWellStepDefs() {
		return assignment.getTasks().get(1)
			.getCharacteristics().get(0)
			.getLevels().get(2);
	}

	private Mastery consecutiveBuildFailures() {
		return assignment.getTasks().get(2)
			.getCharacteristics().get(0)
			.getLevels().get(1);
	}

	/**
	 * Characteristic equality is based on id values. As we're testing against a
	 * non persisted Assignment, we generate fake unique identities in order to
	 * maintain object uniqueness.
	 */
	protected void createCharacteristicIds() {
		assignment.getTasks().stream()
			.map(Task::getCharacteristics).flatMap(Collection::stream)
			.forEach(characteristic -> characteristic.setId(atomicInteger.incrementAndGet()));
	}

}
