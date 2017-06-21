package nl.tudelft.ewi.devhub.server.database.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by Pilmus on 31-5-2017.
 */
@Data
@Entity
@Table(name = "assigned_ta", uniqueConstraints = {
        @UniqueConstraint(name = "unique_ta_per_group_per_course",
                columnNames = {"course_edition_id", "group_number", "assignment_id"})
})
public class AssignedTA {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(name = "course_edition_id", referencedColumnName = "course_edition_id", nullable = false),
            @JoinColumn(name = "group_number", referencedColumnName = "group_number", nullable = false)
    })
    private Group group;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "ta_id")
    private User teachingAssistant;

    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(name = "assignment_course_edition_id", referencedColumnName = "course_edition_id", nullable = false),
            @JoinColumn(name = "assignment_id", referencedColumnName = "assignment_id", nullable = false)
    })
    private Assignment assignment;
}
