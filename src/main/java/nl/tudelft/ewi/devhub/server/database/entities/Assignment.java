package nl.tudelft.ewi.devhub.server.database.entities;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.sun.istack.Nullable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by jgmeligmeyling on 04/03/15.
 */
@Data
@Entity
@Table(name= "assignments")
@ToString(exclude = "summary")
@EqualsAndHashCode(of = { "assignmentId" })
public class Assignment implements Comparable<Assignment> {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long assignmentId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_edition_id")
    private CourseEdition course;

    @NotEmpty(message = "assignment.name.should-be-given")
    @Column(name = "name")
    private String name;

    @Nullable
    @Column(name = "summary")
    @Type(type = "org.hibernate.type.TextType")
    private String summary;

    @Nullable
    @Column(name="due_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dueDate;

    @Override
    public int compareTo(Assignment o) {
        return ComparisonChain.start()
            .compare(getDueDate(), o.getDueDate(), Ordering.natural().nullsFirst())
            .compare(getName(), o.getName())
			.compare(getAssignmentId(), o.getAssignmentId())
            .result();
    }
}
