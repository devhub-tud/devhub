package nl.tudelft.ewi.devhub.server.database.entities.rubrics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ComparisonChain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
@Data
@Entity
@Table(name = "characteristic_mastery")
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"characteristic"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mastery implements Comparable<Mastery> {

	@Id
	@Column(name = "mastery_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(optional = false)
	@JsonBackReference
	@JoinColumn(name = "characteristic_id", referencedColumnName = "characteristic_id")
	private Characteristic characteristic;

	/**
	 * A description of each characteristic at each level of mastery/scale.
	 */
	@Column(name = "description", length = 1024)
	private String description;

	@Column(name = "points")
	private double points;

	@Override
	public int compareTo(Mastery o) {
		return ComparisonChain.start()
			.compare(getPoints(), o.getPoints())
			.compare(getId(), o.getId())
			.result();
	}

	public static Mastery build(Characteristic characteristic, String description, double points) {
		final Mastery mastery = new Mastery();
		mastery.setCharacteristic(characteristic);
		mastery.setDescription(description);
		mastery.setPoints(points);
		return mastery;
	}

}
