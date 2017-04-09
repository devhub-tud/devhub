package nl.tudelft.ewi.devhub.server.database.entities.rubrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Condition;
import org.assertj.core.util.Lists;
import org.junit.Test;

public class CharacteristicTest {

	@Test
	public void testCopyOfCharacteristic() {
		Task task = new Task();

		Characteristic characteristic = characteristicWithOneMastery();
		final Characteristic newCharacteristic = characteristic.copyForNextYear(task);

		assertThat(newCharacteristic).isNotSameAs(characteristic);
		assertThat(newCharacteristic.getDescription()).isEqualTo("A characteristic");
		assertThat(newCharacteristic.getWeight()).isEqualTo(10);
		assertThat(newCharacteristic.getId()).isEqualTo(0);
		assertThat(newCharacteristic.getLevels().stream().map(Mastery::getCharacteristic))
				.are(forTheNewCharacteristic(newCharacteristic));
	}

	private static Condition<Characteristic> forTheNewCharacteristic(Characteristic newCharacteristic) {
		return new Condition<>(c -> {
			assertThat(c).isSameAs(newCharacteristic);
			return true;
		},"It should be the same instance as the newCharacteristic");
	}

	static Characteristic characteristicWithOneMastery() {
		Characteristic characteristic = new Characteristic();

		characteristic.setLevels(Lists.newArrayList(new Mastery()));
		characteristic.setDescription("A characteristic");
		characteristic.setWeight(10);

		return characteristic;
	 }
}
