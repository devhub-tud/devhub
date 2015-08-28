package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.entities.identity.FKSegmentedIdentifierGenerator;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.sun.istack.Nullable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by jgmeligmeyling on 04/03/15.
 */
@Data
@Entity
@Table(name= "assignments")
@ToString(exclude = "summary")
@IdClass(Assignment.AssignmentId.class)
@EqualsAndHashCode(of = {"courseEdition", "assignmentId" })
public class Assignment implements Comparable<Assignment> {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AssignmentId implements Serializable {

		private long courseEdition;

		private long assignmentId;

	}

	@Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "course_edition_id")
    private CourseEdition courseEdition;

    @Id
    @Column(name = "assignment_id")
	@GeneratedValue(generator = "seq_group_number", strategy = GenerationType.AUTO)
	@GenericGenerator(name = "seq_group_number", strategy = "nl.tudelft.ewi.devhub.server.database.entities.identity.FKSegmentedIdentifierGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = FKSegmentedIdentifierGenerator.TABLE_PARAM, value = "seq_assignment_id"),
		@org.hibernate.annotations.Parameter(name = FKSegmentedIdentifierGenerator.CLUSER_COLUMN_PARAM, value = "course_edition_id"),
		@org.hibernate.annotations.Parameter(name = FKSegmentedIdentifierGenerator.PROPERTY_PARAM, value = "courseEdition")
	})
    private long assignmentId;

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
			.compare(getCourseEdition(), o.getCourseEdition())
			.compare(getAssignmentId(), o.getAssignmentId())
            .result();
    }
}
