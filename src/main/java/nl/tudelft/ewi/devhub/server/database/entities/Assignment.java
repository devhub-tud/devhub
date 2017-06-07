package nl.tudelft.ewi.devhub.server.database.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.Base;
import nl.tudelft.ewi.devhub.server.database.entities.identity.FKSegmentedIdentifierGenerator;
import nl.tudelft.ewi.devhub.server.database.entities.rubrics.DutchGradingStrategy;
import nl.tudelft.ewi.devhub.server.database.entities.rubrics.GradingStrategy;
import nl.tudelft.ewi.devhub.server.database.entities.rubrics.Task;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.sun.istack.Nullable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by jgmeligmeyling on 04/03/15.
 */
@Data
@Entity
@Table(name = "assignments")
@ToString(exclude = {"summary", "tasks", "assignedTAS"})
@IdClass(Assignment.AssignmentId.class)
@EqualsAndHashCode(of = {"courseEdition", "assignmentId"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Assignment implements Comparable<Assignment>, Base {

    public static final String ASSIGNMENTS_PATH_BASE = "assignments/";

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentId implements Serializable {

        private long courseEdition;

        private long assignmentId;

    }

    @Id
    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "course_edition_id")
    private CourseEdition courseEdition;

    @Id
    @JsonProperty("id")
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
    @Column(name = "due_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dueDate;

    @Column(name = "released")
    private boolean gradesReleased;

    @OrderBy("id ASC")
    @JsonManagedReference
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Task> tasks;

    @OneToMany(mappedBy = "assignment")
    private List<AssignedTA> assignedTAS;

    public Optional<User> getAssignedTA(Delivery delivery) {
        Group group = delivery.getGroup();
        return assignedTAS.stream()
                .filter(a -> a.getGroup().equals(group))
                .findAny()
                .map(AssignedTA::getTeachingAssistant);
    }

    @Override
    public int compareTo(Assignment o) {
        return ComparisonChain.start()
                .compare(getDueDate(), o.getDueDate(), Ordering.natural().nullsFirst())
                .compare(getName(), o.getName())
                .compare(getCourseEdition(), o.getCourseEdition())
                .compare(getAssignmentId(), o.getAssignmentId())
                .result();
    }

    public double getNumberOfAchievablePoints() {
        return getTasks().stream()
                .mapToDouble(Task::getMaximalNumberOfPoints)
                .sum();
    }

    @JsonIgnore
    public GradingStrategy getGradingStrategy() {
        return new DutchGradingStrategy();
    }

    @Override
    public URI getURI() {
        return getCourseEdition().getURI().resolve(ASSIGNMENTS_PATH_BASE).resolve(getAssignmentId() + "/");
    }

    public List<Task> copyTasksFromOldAssignment(Assignment oldAssignment) {
        return oldAssignment.getTasks().stream().map(this::taskforNewAssginment).collect(Collectors.toList());
    }

    private Task taskforNewAssginment(Task oldTask) {
        return oldTask.copyForNextYear(this);
    }

}
