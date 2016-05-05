package nl.tudelft.ewi.devhub.server.database.entities.rubrics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {

	@Id
	@Column(name = "task_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@JsonBackReference
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
	@JsonManagedReference
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Characteristic> characteristics;

	public double getTotalWeight() {
		return getCharacteristics().stream()
			.filter(Characteristic::isWeightAddsToTotalWeight)
			.mapToDouble(Characteristic::getWeight)
			.sum();
	}

	public double getMaximalNumberOfPoints() {
		return getCharacteristics().stream()
			.filter(Characteristic::isWeightAddsToTotalWeight)
			.mapToDouble(Characteristic::getMaximalNumberOfPointsWithWeight)
			.sum();
	}

}
