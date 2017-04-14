package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.Base;
import nl.tudelft.ewi.devhub.server.database.Configurable;
import nl.tudelft.ewi.devhub.server.database.embeddables.TimeSpan;
import nl.tudelft.ewi.devhub.server.database.entities.builds.BuildInstructionEntity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Data
@Entity
@Table(name = "course_edition")
@ToString(of = {"id", "course", "code"})
@EqualsAndHashCode(of = { "id" })
public class CourseEdition implements Comparable<CourseEdition>, Configurable, Base {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@JoinColumn(name = "course_id")
	@ManyToOne(optional = false, cascade = CascadeType.PERSIST)
	private Course course;

	/**
	 * The course code for the course edition. For example: {@code 1516}.
	 * The code should be unique, as it is part of the provisioned
	 * repository path.
	 */
	@NotEmpty(message = "error.course-code-empty")
	@Column(name = "code", unique = true, nullable = false)
	private String code;

	@NotNull
	@Embedded
	private TimeSpan timeSpan;

	@NotNull(message = "error.course-min-group-empty")
	@Column(name = "min_group_size")
	private Integer minGroupSize;

	@NotNull(message = "error.course-max-group-empty")
	@Column(name = "max_group_size")
	private Integer maxGroupSize;

	@Column(name = "template_repository_url")
	private String templateRepositoryUrl;

	@OrderBy("groupNumber ASC")
	@OneToMany(mappedBy = "courseEdition", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Group> groups;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(
		name="course_assistants",
		joinColumns={@JoinColumn(name="course_edition_id", referencedColumnName="id")},
		inverseJoinColumns={@JoinColumn(name="user_id", referencedColumnName="id")})
	private Set<User> assistants;

    @OrderBy("dueDate ASC, name ASC")
    @OneToMany(mappedBy = "courseEdition", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Assignment> assignments;

	@ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name = "build_instruction")
	private BuildInstructionEntity buildInstruction;

	@ElementCollection
	@JoinTable(name="course_properties", joinColumns=@JoinColumn(name="course_edition_id"))
	@MapKeyColumn(name="property_key")
	@Column(name="property_value")
	private Map<String, String> properties;

	public String getName() {
		return String.format("%s %s - %s", course.getCode(), getCode(), course.getName());
	}

	/**
	 * Set the course code for this course. The course code will be converted
	 * into uppercase to ensure uniqueness across database implementations.
	 *
	 * @param code
	 */
	public void setCode(String code) {
		this.code = code.toUpperCase();
	}

	@Override
	public int compareTo(CourseEdition o) {
		return ComparisonChain.start()
			.compare(getCourse(), o.getCourse())
			.compare(getTimeSpan().getStart(), o.getTimeSpan().getStart())
			.result();
	}

	@Override
	public URI getURI() {
		// courses/ti1706/1516/
		return getCourse().getURI().resolve(getCode() + "/");
	}

	public URI createRepositoryName(final Group group) {
		Preconditions.checkNotNull(group);
		URI uri = getURI().resolve("group-" + group.getGroupNumber());
		// Relativize to "/" because repository names do not start with "/"
		return URI.create("/").relativize(uri);
	}

	public String intervalString() {
		return timeSpan.intervalString();
	}
}
