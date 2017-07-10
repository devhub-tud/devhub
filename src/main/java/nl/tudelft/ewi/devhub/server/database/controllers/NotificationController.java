package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.Notification;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import static nl.tudelft.ewi.devhub.server.database.entities.notifications.QNotification.notification;

/**
 * Created by Arjan on 7-6-2017.
 */
public class NotificationController extends Controller<Notification>{

    public static final int DEFAULT_NOTIFICATIONS_RESULT_SET_SIZE = 25;

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
    public SortedMap<Notification, Boolean> getLatestNotificationsFor(User user) {
        return new TreeMap<>(query().from(notification)
                .orderBy(notification.timestamp.desc())
                .limit(DEFAULT_NOTIFICATIONS_RESULT_SET_SIZE)
                .map(notification, notification.recipients.get(user).isTrue()));
    }

    @Transactional
    public long getNumberOfUnreadNotificationsFor(User user) {
        return query().from(notification)
                .where(notification.recipients.get(user).isFalse())
                .orderBy(notification.timestamp.desc())
                .count();
    }

}
