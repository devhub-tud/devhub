package nl.tudelft.ewi.devhub.server.database.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@SuppressWarnings("serial")
@Table(name = "course_assistants")
public class CourseAssistant implements Serializable {

	@Id
	@ManyToOne
	@JoinColumn(name = "course_id")
	private Course course;

	@Id
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

}
