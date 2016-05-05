package nl.tudelft.ewi.devhub.server.database.entities.rubrics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * The skills, knowledge, and/or behavior to be demonstrated.
 */
@Data
@Entity
@Table(name = "assignment_task_characteristic")
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"task"})
public class Characteristic {

	@Id
	@Column(name = "characteristic_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

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
	@OneToMany(mappedBy = "characteristic", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Mastery> levels;

	@Column(name = "weight")
	private double weight;

	public int getMaximalNumberOfPoints() {
		return getLevels().stream()
			.mapToInt(Mastery::getPoints)
			.max().orElse(0);
	}

	public double getMaximalNumberOfPointsWithWeight() {
		return getWeight() * getMaximalNumberOfPoints();
	}

}
