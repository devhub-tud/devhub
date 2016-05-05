package nl.tudelft.ewi.devhub.server.database.entities.rubrics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * A subtask of an {@link Assignment}.
 */
@Data
@Entity
@Table(name = "assignment_task")
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"assignment"})
public class Task {

	@Id
	@Column(name = "task_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(optional = false)
	@JoinColumns({
		@JoinColumn(name = "course_edition_id", referencedColumnName = "course_edition_id"),
		@JoinColumn(name = "assignment_id", referencedColumnName = "assignment_id")
	})
	private Assignment assignment;

	/**
	 * The outcome being assessed or instructions students received for an assignment.
	 */
	@Column(name = "description", length = 1024)
	private String description;

	/**
	 * The skills, knowledge, and/or behavior to be demonstrated.
	 */
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Characteristic> characteristics;

	public double getTotalWeight() {
		return getCharacteristics().stream()
			.mapToDouble(Characteristic::getWeight)
			.sum();
	}

	public double getMaximalNumberOfPoints() {
		return getCharacteristics().stream()
			.mapToDouble(Characteristic::getMaximalNumberOfPointsWithWeight)
			.sum();
	}

}
