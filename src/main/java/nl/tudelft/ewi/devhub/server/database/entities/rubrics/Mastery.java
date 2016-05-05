package nl.tudelft.ewi.devhub.server.database.entities.rubrics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
public class Mastery {

	@Id
	@Column(name = "mastery_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "characteristic_id", referencedColumnName = "characteristic_id")
	private Characteristic characteristic;

	/**
	 * A description of each characteristic at each level of mastery/scale.
	 */
	@Column(name = "description", length = 1024)
	private String description;

	@Column(name = "points")
	private int points;

}
