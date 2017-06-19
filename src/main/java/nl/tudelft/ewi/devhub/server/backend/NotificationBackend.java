package nl.tudelft.ewi.devhub.server.backend;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.controllers.NotificationController;
import nl.tudelft.ewi.devhub.server.database.controllers.NotificationUserController;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.Notification;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.NotificationsToUsers;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Arjan on 13-6-2017.
 */
public class NotificationBackend {

    NotificationUserController notificationUserController;
    NotificationController notificationController;

    @Inject
    public NotificationBackend(NotificationController notificationController, NotificationUserController notificationUserController) {
        this.notificationController = notificationController;
        this.notificationUserController = notificationUserController;
    }

    @Transactional
    public void createNotification(Issue issue, User currentUser) {
        Notification notification = new Notification();
        try {
            notification.setLink(new URI("http://localhost:50001/"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        notification.setSender(currentUser);
        notification.setEvent("Issue Event");
        notification.setMessage(issue.getTitle());
        createNotification(notification);
    }

    @Transactional
    public void createNotification(Notification notification) {
        NotificationsToUsers notificationToUsers = new NotificationsToUsers();
        notificationToUsers.setRead(false);
        notificationToUsers.setUser(notification.getSender());
        notificationToUsers.setNotification(notification);
        notificationController.persist(notification);
        notificationUserController.persist(notificationToUsers);
    }
}
