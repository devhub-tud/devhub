package nl.tudelft.ewi.devhub.server.database.entities.notifications;

import lombok.*;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Arjan on 7-6-2017.
 */
@Data
@Entity
@IdClass(NotificationsToUsers.NotificationsToUsersId.class)
@Table(name = "notifications_to_users")
@EqualsAndHashCode(of = {"user","notification"})
@ToString
public class NotificationsToUsers {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationsToUsersId implements Serializable {
        private long user;
        private long notification;
    }

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "notification_id")
    private Notification notification;

    @Column(name = "isRead")
    private boolean isRead;

    public boolean equalsNotificationId(long id) {
        return notification.getId() == id;
    }

}
