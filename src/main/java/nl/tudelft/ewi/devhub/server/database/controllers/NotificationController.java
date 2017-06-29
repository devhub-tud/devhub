package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.Notification;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static nl.tudelft.ewi.devhub.server.database.entities.notifications.QNotification.notification;

/**
 * Created by Arjan on 7-6-2017.
 */
public class NotificationController extends Controller<Notification>{

    @Inject
    public NotificationController(EntityManager entityManager) {
        super(entityManager);
    }

    public Optional<Notification> findById(long id) {
        return Optional.ofNullable(query().from(notification)
                    .where(notification.id.eq(id))
                .singleResult(notification));
    }

    @Transactional
    public List<Notification> getUnreadNotificationsFor(User user) {
        return query().from(notification)
                .where(notification.recipients.get(user).isFalse())
                .orderBy(notification.timestamp.desc())
                .list(notification);
    }

    @Transactional
    public List<Notification> getReadNotificationsFor(User user) {
        return query().from(notification)
                .where(notification.recipients.get(user).isFalse())
                .orderBy(notification.timestamp.desc())
                .list(notification);
    }


}
