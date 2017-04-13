package nl.tudelft.ewi.devhub.server.database.entities;

import static java.util.Comparator.comparing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.Base;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Jan-Willem on 8/11/2015.
 */
@Data
@Entity
@Table(name = "course")
@ToString(of = {"id", "code"})
@EqualsAndHashCode(of = {"id"})
public class Course implements Comparable<Course>, Base {

	public static String COURSE_BASE_PATH = "/courses/";

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The course code for the course. For example: {@code TI1706}.
     * The course code should be unique, as it is part of the provisioned
     * repository path.
     */
    @NotEmpty(message = "error.course-code-empty")
    @Column(name = "code", unique = true)
    private String code;

    /**
     * Name for the course. Should not be empty.
     */
    @NotEmpty(message = "error.course-name-empty")
    @Column(name = "name")
    private String name;

    /**
     * The editions for this course.
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
    @OrderBy("timeSpan.start ASC")
    private List<CourseEdition> editions;

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
    public int compareTo(Course o) {
        return getCode().compareTo(o.getCode());
    }

	@Override
	public URI getURI() {
		return URI.create(COURSE_BASE_PATH).resolve(getCode().toLowerCase() + "/");
	}

    public List<Assignment> allAssignments() {
        return this.getEditions().stream()
                .flatMap(course -> course.getAssignments().stream())
                .collect(Collectors.toList());
    }

}
