package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Jan-Willem on 8/11/2015.
 */
@Data
@Entity
@Table(name = "course")
@ToString(of = {"id", "code"})
@EqualsAndHashCode(of = {"id"})
public class Course implements Comparable<Course> {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The course code for the course. For example: {@code TI1706}.
     * The course code should be unique, as it is part of the provisioned
     * repository path.
     */
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
    @OrderBy("start_date ASC")
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

}
