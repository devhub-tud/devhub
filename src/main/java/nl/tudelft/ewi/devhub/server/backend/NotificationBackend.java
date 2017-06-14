package nl.tudelft.ewi.devhub.server.backend;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.controllers.NotificationController;
import nl.tudelft.ewi.devhub.server.database.controllers.NotificationUserController;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.Notification;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.NotificationsToUsers;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Arjan on 13-6-2017.
 */
public class NotificationBackend {

    private final NotificationUserController notificationUserController;
    private final NotificationController notificationController;

    @Inject
    public NotificationBackend(NotificationController notificationController, NotificationUserController notificationUserController) {
        this.notificationController = notificationController;
        this.notificationUserController = notificationUserController;
    }

    @Transactional
    public void createNotification(Notification notification, Collection<User> receivers) {
        notificationController.persist(notification);
        for(User user: receivers) {
            NotificationsToUsers notificationToUsers = new NotificationsToUsers();
            notificationToUsers.setRead(false);
            notificationToUsers.setUser(user);
            notificationToUsers.setNotification(notification);
            notificationUserController.persist(notificationToUsers);
        }
    }

    private void setNotificationFields(Notification notification, User sender, String link, String event, String message ) {
        notification.setSender(sender);
        notification.setLink(link);
        notification.setEvent(event);
        notification.setMessage(message);
    }

    public void createNotification(Issue issue, User currentUser) {
        Notification notification = new Notification();
        setNotificationFields(notification,currentUser,issue.getURI().getHost(),"Issue Created","Message");
        createNotification(notification, Arrays.asList(currentUser,issue.getAssignee()));
    }

    public void createNotification(RepositoryApi repositoryApi, PullRequest pullRequest) {
        Notification notification = new Notification();
        setNotificationFields(notification,pullRequest.getAssignee(),pullRequest.getURI().getHost(),"Pull Request","Message");
        createNotification(notification,pullRequest.getRepository().getCollaborators());
    }
}
