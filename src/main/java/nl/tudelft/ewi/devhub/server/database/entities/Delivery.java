package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by jgmeligmeyling on 04/03/15.
 */
@Data
@Entity
@Table(name = "assignment_deliveries")
@EqualsAndHashCode(of={"assignment", "group"})
public class Delivery implements Comparable<Delivery> {

    public enum State {
        SUBMITTED, DISAPPROVED, REJECTED, APPROVED;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long groupId;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "course_id", referencedColumnName = "course_id"),
        @JoinColumn(name = "assignment_id", referencedColumnName = "assignment_id")
    })
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "commit_id")
    private String commitId;

    @NotNull
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User createdUser;

    @Embedded
    private Review review;

    @Column(name = "notes")
    private String notes;

    @Data
    @Embeddable
    public static class Review {

        @Column(name = "grade")
        private Integer grade;

        @Column(name="review_time")
        @Temporal(TemporalType.TIMESTAMP)
        private Date reviewTime;

        @Enumerated(EnumType.STRING)
        @Column(name = "state")
        private State state;

        @ManyToOne
        @JoinColumn(name = "review_user")
        private User reviewUser;

        @Column(name = "commentary")
        private String commentary;

    }

    public boolean isSubmitted() {
        return getReview() != null &&
            State.SUBMITTED.equals(getReview().getState());
    }

    public boolean isApproved() {
        return getReview() != null &&
            State.APPROVED.equals(getReview().getState());
    }

    public boolean isDisapproved() {
        return getReview() != null &&
            State.DISAPPROVED.equals(getReview().getState());
    }

    public boolean isRejected() {
        return getReview() != null &&
            State.REJECTED.equals(getReview().getState());
    }

    @Override
    public int compareTo(Delivery other) {
        return getCreated().compareTo(other.getCreated());
    }

}
