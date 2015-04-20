package nl.tudelft.ewi.devhub.server.database.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;

@Data
@Entity
@Table(name = "courses")
@ToString(of = { "code" })
@EqualsAndHashCode(of = { "id" })
public class Course {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotEmpty(message = "error.course-name-empty")
	@Column(name = "name")
	private String name;

	@NotEmpty(message = "course-code-empty")
	@Column(name = "code", unique=true)
	private String code;

	@NotNull
	@Column(name = "start_date")
	private Date start;

	@Column(name = "end_date")
	private Date end;

	@NotNull(message = "error.course-min-group-empty")
	@Column(name = "min_group_size")
	private Integer minGroupSize;

	@NotNull(message = "error.course-max-group-empty")
	@Column(name = "max_group_size")
	private Integer maxGroupSize;

	@Column(name = "template_repository_url")
	private String templateRepositoryUrl;

    @NotNull(message = "error.course-timeout")
	@Column(name = "build_timeout")
	private Integer buildTimeout;

	@OrderBy("groupNumber ASC")
	@OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
	private List<Group> groups;

	@OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
	private List<CourseAssistant> courseAssistants;

    @OrderBy("assignmentId ASC")
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private List<Assignment> assignments;

	public void setCode(String code) {
		this.code = code.toUpperCase();
	}

	public List<User> getAssistants() {
		List<User> assistants = Lists.newArrayList();
		for (CourseAssistant assistant : courseAssistants) {
			assistants.add(assistant.getUser());
		}
		return assistants;
	}

}
