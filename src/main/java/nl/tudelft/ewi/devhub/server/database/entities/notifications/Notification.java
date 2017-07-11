package nl.tudelft.ewi.devhub.server.database.entities.notifications;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.database.Base;
import nl.tudelft.ewi.devhub.server.database.entities.Event;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;
import nl.tudelft.ewi.devhub.server.web.templating.TranslatorFactory;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Arjan on 7-6-2017.
 */
@Data
@Entity
@Table(name = "notification")
@EqualsAndHashCode(of = {"id"})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(PullRequestMergedNotification.class),
        @JsonSubTypes.Type(CommentNotification.class),
        @JsonSubTypes.Type(IssueAssignedNotification.class),
        @JsonSubTypes.Type(IssueClosedNotification.class),
        @JsonSubTypes.Type(IssueCreatedNotification.class),
        @JsonSubTypes.Type(IssueEditedNotification.class)
})
@DiscriminatorColumn(name = "type")
public abstract class Notification implements Base, Event {

    private final static TranslatorFactory TRANSLATOR_FACTORY =
            new TranslatorFactory("i18n.devhub");

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User sender;

    @CreationTimestamp
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @ElementCollection
    @CollectionTable(
            name = "notifications_to_users",
            joinColumns = @JoinColumn(name = "notification_id", referencedColumnName = "id")
    )
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "isRead")
    private Map<User, Boolean> recipients;

    @Deprecated
    public String getTitle() {
        return getTitle(TRANSLATOR_FACTORY.create(Collections.singletonList(Locale.ENGLISH)));
    }

    public String getTitle(Translator translator) {
        try {
            return translator.translate(getTitleResourceKey(), getTitleParameters());
        }
        catch (Exception e) {
            return "?" + getTitleResourceKey() + "?";
        }
    }

    protected abstract String getTitleResourceKey();

    protected Object[] getTitleParameters() {
        return new Object[0];
    }

    @Deprecated
    public String getDescription() {
        return getDescription(TRANSLATOR_FACTORY.create(Collections.singletonList(Locale.ENGLISH)));
    }

    public String getDescription(Translator translator) {
        try {
            return translator.translate(getDescriptionResourceKey(), getDescriptionParameters());
        }
        catch (Exception e) {
            return "?" + getDescriptionResourceKey() + "?";
        }
    }

    protected Object[] getDescriptionParameters() {
        return new Object[0];
    }

    protected abstract String getDescriptionResourceKey();

    public void setRead(User user) {
        getRecipients().replace(user, false, true);
    }

    public boolean isRead(User user) {
        return getRecipients().getOrDefault(user, false);
    }

}
