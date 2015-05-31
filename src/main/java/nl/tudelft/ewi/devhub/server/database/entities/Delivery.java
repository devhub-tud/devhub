package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Delivery for an assignment
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@Table(name = "assignment_deliveries")
@ToString(exclude = {"notes", "attachments"})
@EqualsAndHashCode(of={"assignment", "group"})
public class Delivery implements Comparable<Delivery> {

    /**
     * The State for the Delivery
     * @author Jan-Willem Gmelig Meyling
     */
    public enum State {
        SUBMITTED("delivery.state.submitted", "info", "delivery.state.submitted.description", "delivery.state.submitted.message"),
        REJECTED("delivery.state.rejected", "warning", "delivery.state.rejected.description", "delivery.state.submitted.message"),
        APPROVED("delivery.state.approved", "success", "delivery.state.approved.description", "delivery.state.submitted.message"),
        DISAPPROVED("delivery.state.disapproved", "danger", "delivery.state.disapproved.description", "delivery.state.submitted.message");

        /**
         * The translation key used for the badges, for example "Submitted".
         */
        @Getter
        private final String translationKey;

        /**
         * The style class used for the badges, for example "warning" (= orange).
         */
        @Getter
        private final String style;

        /**
         * The translation key used for the description tooltips. These are mainly for the
         * reviewers. For example: "Submissions that should be fixed and resubmitted."
         */
        @Getter
        private final String descriptionTranslionKey;

        /**
         * The translation key used for the message tooltips. These are mainly for the
         * students. For example: "Please fix the addressed issues before resubmitting."
         */
        @Getter
        private final String messageTranslationKey;

        State(String translationKey, String style, String descriptionTranslionKey, String messageTranslationKey) {
            this.translationKey = translationKey;
            this.style = style;
            this.descriptionTranslionKey = descriptionTranslionKey;
            this.messageTranslationKey = messageTranslationKey;
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

    @Column(name = "notes")
    @Type(type = "org.hibernate.type.TextType")
    private String notes;

    @JoinColumn(name = "delivery_id")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryAttachment> attachments;

    @Data
    @Embeddable
    @EqualsAndHashCode
    @ToString(exclude = {"commentary"})
    public static class Review {

        @Column(name = "grade")
        @Basic(fetch=FetchType.LAZY)
        private Double grade;

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

        @Column(name = "commentary")
        @Type(type = "org.hibernate.type.TextType")
        private String commentary;

    }

    public State getState() {
        Review review = getReview();
        return review == null ? State.SUBMITTED : review.getState() == null ? State.SUBMITTED : review.getState();
    }

    public boolean isSubmitted() {
        return getState().equals(State.SUBMITTED);
    }

    public boolean isApproved() {
        return getState().equals(State.APPROVED);
    }

    public boolean isDisapproved() {
        return getState().equals(State.DISAPPROVED);
    }

    public boolean isRejected() {
        return getState().equals(State.REJECTED);
    }

    public boolean isLate() {
        Date dueDate = getAssignment().getDueDate();
        return dueDate != null && getCreated().after(dueDate);
    }

    @Override
    public int compareTo(Delivery other) {
        return getCreated().compareTo(other.getCreated());
    }

}
