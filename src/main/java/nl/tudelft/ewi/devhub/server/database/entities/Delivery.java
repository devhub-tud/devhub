package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.Base;
import nl.tudelft.ewi.devhub.server.database.entities.rubrics.Characteristic;
import nl.tudelft.ewi.devhub.server.database.entities.rubrics.Mastery;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Delivery for an assignment
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@Table(name = "assignment_deliveries")
@ToString(exclude = {"notes", "attachments"})
@EqualsAndHashCode(of={"deliveryId"})
public class Delivery implements Event, Base {

	public static final Comparator<Delivery> DELIVERIES_BY_GROUP_NUMBER = (a, b) ->
		Long.compare(a.getGroup().getGroupNumber(), b.getGroup().getGroupNumber());

	/**
     * The State for the Delivery
     * @author Jan-Willem Gmelig Meyling
     */
    public enum State {
        SUBMITTED("delivery.state.submitted", "info", "delivery.state.submitted.description", "delivery.state.submitted.message"),
        REJECTED("delivery.state.rejected", "warning", "delivery.state.rejected.description", "delivery.state.rejected.message"),
        APPROVED("delivery.state.approved", "success", "delivery.state.approved.description", "delivery.state.approved.message"),
        DISAPPROVED("delivery.state.disapproved", "danger", "delivery.state.disapproved.description", "delivery.state.disapproved.message");

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

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
		@JoinColumnOrFormula(formula = @JoinFormula(value = "course_edition_id", referencedColumnName = "course_edition_id")),
		@JoinColumnOrFormula(column = @JoinColumn(name = "assignment_id", referencedColumnName = "assignment_id", nullable = false))
	})
    private Assignment assignment;

    @ManyToOne(optional = false)
    @JoinColumns({
		@JoinColumn(name = "course_edition_id", referencedColumnName = "course_edition_id", nullable = false),
		@JoinColumn(name = "group_number", referencedColumnName = "group_number", nullable = false)
	})
    private Group group;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name = "repository_id", referencedColumnName = "repository_id"),
		@JoinColumn(name = "commit_id", referencedColumnName = "commit_id")
	})
    private Commit commit;

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
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<DeliveryAttachment> attachments;

	@CreationTimestamp
	@Column(name = "created_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

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

	@ManyToMany
	@JoinTable(
		name = "delivery_rubrics",
		joinColumns = @JoinColumn(name = "delivery_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "mastery_id", referencedColumnName = "mastery_id")
	)
	@MapKeyJoinColumn(name = "characteristic_id", referencedColumnName = "characteristic_id")
	private Map<Characteristic, Mastery> rubrics;

	public double getAchievedNumberOfPoints() {
		return Math.max(0, Math.min(getRubrics().entrySet().stream()
			.mapToDouble(entry -> entry.getValue().getPoints() * entry.getKey().getWeight())
			.sum(), getAssignment().getNumberOfAchievablePoints()));
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
        return dueDate != null && getTimestamp().after(dueDate);
    }

	@Override
	public URI getURI() {
		return getGroup().getURI()
			.resolve(Assignment.ASSIGNMENTS_PATH_BASE)
			.resolve(getAssignment().getAssignmentId() + "/")
			.resolve("deliveries/")
			.resolve(getDeliveryId() + "/");
	}
}
