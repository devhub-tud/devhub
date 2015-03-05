package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by jgmeligmeyling on 04/03/15.
 */
@Data
@Entity
@Table(name = "assignment_deliveries")
@EqualsAndHashCode(of={"assignment", "group"})
public class Delivery implements Comparable<Delivery> {

    public enum State {
        SUBMITTED("delivery.state.submitted"),
        DISAPPROVED("delivery.state.disapproved"),
        REJECTED("delivery.state.rejected"),
        APPROVED("delivery.state.approved");

        private final String translationKey;

        private State(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getTranslationKey() {
            return this.translationKey;
        }
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long deliveryId;

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

    @Basic(fetch=FetchType.LAZY)
    @Column(name = "notes")
    private String notes;

    @JoinColumn(name = "delivery_id")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryAttachment> attachments;

    @Data
    @Embeddable
    public static class Review {

        @Column(name = "grade")
        @Basic(fetch=FetchType.LAZY)
        private Integer grade;

        @Column(name="review_time")
        @Basic(fetch=FetchType.LAZY)
        @Temporal(TemporalType.TIMESTAMP)
        private Date reviewTime;

        @Column(name = "state")
        @Basic(fetch=FetchType.LAZY)
        @Enumerated(EnumType.STRING)
        private State state;

        @JoinColumn(name = "review_user")
        @ManyToOne(fetch = FetchType.LAZY)
        private User reviewUser;

        @Basic(fetch=FetchType.LAZY)
        @Column(name = "commentary")
        private String commentary;

    }

    public boolean hasState(State state) {
        return review == null ? State.SUBMITTED.equals(state) : review.getState().equals(state);
    }

    public boolean isSubmitted() {
        return hasState(State.SUBMITTED);
    }

    public boolean isApproved() {
        return hasState(State.APPROVED);
    }

    public boolean isDisapproved() {
        return hasState(State.DISAPPROVED);
    }

    public boolean isRejected() {
        return hasState(State.REJECTED);
    }

    @Override
    public int compareTo(Delivery other) {
        return getCreated().compareTo(other.getCreated());
    }

}
