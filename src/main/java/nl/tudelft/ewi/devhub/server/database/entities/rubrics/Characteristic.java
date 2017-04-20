package nl.tudelft.ewi.devhub.server.database.entities.rubrics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.common.collect.ComparisonChain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * The skills, knowledge, and/or behavior to be demonstrated.
 */
@Data
@Entity
@Table(name = "assignment_task_characteristic")
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"task"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Characteristic implements Comparable<Characteristic> {

	@Id
	@Column(name = "characteristic_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@JsonBackReference
	@ManyToOne(optional = false)
	@JoinColumn(name = "task_id", referencedColumnName = "task_id")
	private Task task;

	/**
	 * The skills, knowledge, and/or behavior to be demonstrated.
	 */
	@Column(name = "description", length = 1024)
	private String description;

	/**
	 * Labels used to describe the levels of mastery should be tactful and clear.
	 * Commonly	used labels include:
	 * <ul>
	 * 	<li>Not meeting, approaching, meeting, exceeding</li>
	 * 	<li>Exemplary, proficient, marginal, unacceptable</li>
	 * 	<li>Advanced, intermediate high, intermediate, novice</li>
	 * 	<li>1, 2, 3, 4</li>
	 * </ul>
	 */
	@OrderBy("points ASC, id ASC")
	@JsonManagedReference
	@OneToMany(mappedBy = "characteristic", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Mastery> levels;

	@Column(name = "weight")
	private double weight;

	/**
	 * Hook that allows you to ignore this weight when computing the
	 * total weight for a {@link Task}, allowing you to create
	 * {@code Characteristics} for bonus points and penalties.
	 *
	 * @deprecated This is probably not necessary anymore, or not a
	 * really favorable implementation. We have to think about having
	 * something more clear for bonus points. Penalties are also supported
	 * by making a characteristic with masteries with negative
	 * points.
	 *
	 * @see Characteristic#getMaximalNumberOfPoints()
	 * @see Characteristic#getMaximalNumberOfPointsWithWeight()
	 */
	@Deprecated
	@Column(name = "weight_adds_to_total_weight")
	private boolean weightAddsToTotalWeight = true;

	public double getMaximalNumberOfPoints() {
		return Math.max(0, getLevels().stream()
			.mapToDouble(Mastery::getPoints)
			.max().orElse(0));
	}

	public double getMaximalNumberOfPointsWithWeight() {
		return getWeight() * getMaximalNumberOfPoints();
	}

	@Override
	public int compareTo(Characteristic o) {
		return Long.compare(getId(), o.getId());
	}

	public Characteristic copyForNextYear(Task task) {
		Characteristic newCharacteristic = new Characteristic();
		newCharacteristic.setDescription(this.description);
		newCharacteristic.setTask(task);
		newCharacteristic.setWeight(this.weight);

		List<Mastery> newMasteries = this.getLevels().stream().map(newCharacteristic::copyOfMastery).collect(toList());
		newCharacteristic.setLevels(newMasteries);

		return newCharacteristic;
	}

	private Mastery copyOfMastery(Mastery mastery) {
		return mastery.copyForNextYear(this);
	}
}
