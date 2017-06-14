package nl.tudelft.ewi.devhub.server.backend;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.controllers.NotificationController;
import nl.tudelft.ewi.devhub.server.database.controllers.NotificationUserController;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.comments.Comment;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.Notification;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.NotificationsToUsers;
import nl.tudelft.ewi.devhub.server.web.templating.TranslatorFactory;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Created by Arjan on 13-6-2017.
 */
public class NotificationBackend {

    private final static int MESSAGE_LENGTH = 50;
    private final NotificationUserController notificationUserController;
    private final NotificationController notificationController;
    private final TranslatorFactory translatorFactory;

    @Inject
    public NotificationBackend(NotificationController notificationController, NotificationUserController notificationUserController, TranslatorFactory translatorFactory) {
        this.notificationController = notificationController;
        this.notificationUserController = notificationUserController;
        this.translatorFactory = translatorFactory;
    }

    @Transactional
    public void createNotification(Notification notification, Collection<User> receivers) {
        notificationController.persist(notification);
        for(User user: receivers) {
            if(user != null) {
                NotificationsToUsers notificationToUsers = new NotificationsToUsers();
                notificationToUsers.setRead(false);
                notificationToUsers.setUser(user);
                notificationToUsers.setNotification(notification);
                notificationUserController.persist(notificationToUsers);
            }
        }
    }

    private Notification createNotificationObject(User sender, String link, String event, String message, String title) {
        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setLink(link);
        notification.setEvent(event);
        notification.setMessage(message);
        notification.setTitle(title);
        return notification;
    }

    public void createNotification(Issue issue, User currentUser) {
        List<Locale> locale = Arrays.asList(Locale.ENGLISH);

        String title = translatorFactory.create(locale).translate("notifications.issue.title",issue.getTitle(),currentUser.getName());
        String message = trim(issue.getDescription());
        String link = issue.getURI().getPath();
        String event = "Issue created";
        if(message.isEmpty()) {
            message = translatorFactory.create(locale).translate("notifications.issue.empty-message");
        }

        Notification notification = createNotificationObject(currentUser,link,event,message,title);
        createNotification(notification, Arrays.asList(issue.getAssignee()));
    }

    public void createNotification(RepositoryApi repositoryApi, PullRequest pullRequest) {
        Notification notification = createNotificationObject(pullRequest.getAssignee(),pullRequest.getURI().getPath(),"Pull Request","Message", "placeholderTitle");
        createNotification(notification,pullRequest.getRepository().getCollaborators());
    }

    public void createNotification(Comment comment, User currentUser) {
        Notification notification = createNotificationObject(currentUser, comment.getRepository().getURI().getPath() + "issues", "Comment", "Message", "placeholderTitle");
        createNotification(notification, comment.getRepository().getCollaborators());
    }

    private String trim(String string) {
        if(string.length() < MESSAGE_LENGTH) {
            return string;
        }
        return string.substring(0,MESSAGE_LENGTH) + "....";
    }
}
