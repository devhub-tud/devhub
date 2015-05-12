package nl.tudelft.ewi.devhub.server.database.entities;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by jgmeligmeyling on 04/03/15.
 */
@Data
@Entity
@Table(name= "assignments")
@IdClass(Assignment.AssignmentId.class)
@ToString(exclude = "summary")
@EqualsAndHashCode(of={"course", "assignmentId"})
public class Assignment implements Comparable<Assignment> {

    @Data
    @EqualsAndHashCode
    public static class AssignmentId implements Serializable {
        private Course course;
        private Long assignmentId;
    }

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Id
    @NotNull(message = "assignment.number.should-be-given")
    @Column(name = "assignment_id")
    private Long assignmentId;

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
            .result();
    }
}
