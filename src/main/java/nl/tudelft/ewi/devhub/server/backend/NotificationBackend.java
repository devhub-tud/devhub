package nl.tudelft.ewi.devhub.server.backend;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.controllers.NotificationController;
import nl.tudelft.ewi.devhub.server.database.controllers.NotificationUserController;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.comments.Comment;
import nl.tudelft.ewi.devhub.server.database.entities.comments.IssueComment;
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

import java.net.URI;
import java.net.URISyntaxException;

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
        try {
            notification.setLink(new URI(link));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        notification.setEvent(event);
        notification.setMessage(message);
        notification.setTitle(title);
        return notification;
    }


    public void createIssueCreatedNotification(Issue issue,User currentUser) {
        List<Locale> locale = Arrays.asList(Locale.ENGLISH);

        String assignTitle = translatorFactory.create(locale).translate("notifications.issueAssign.title",issue.getTitle(),currentUser.getName());
        String creationTitle = translatorFactory.create(locale).translate("notifications.issueCreate.title",issue.getTitle(),currentUser.getName());
        String event = "Issue created";

        Notification assignNotification = createIssueNotification(issue, currentUser, event, assignTitle);
        Notification creationNotification = createIssueNotification(issue, currentUser, event, creationTitle);

        createNotification(assignNotification, Arrays.asList(issue.getAssignee()));
        createNotification(creationNotification, issue.getRepository().getCollaborators());
    }

    public void createIssueEditedNotification(Issue issue,User currentUser) {
        List<Locale> locale = Arrays.asList(Locale.ENGLISH);

        String title = translatorFactory.create(locale).translate("notifications.issueEdit.title",issue.getTitle(),currentUser.getName());
        String event = "Issue Edited";

        Notification notification = createIssueNotification(issue, currentUser, event, title);
        createNotification(notification, issue.getRepository().getCollaborators());
    }

    public void createIssueClosedNotification(Issue issue,User currentUser) {
        List<Locale> locale = Arrays.asList(Locale.ENGLISH);

        String title = translatorFactory.create(locale).translate("notifications.issueClose.title",issue.getTitle(),currentUser.getName());
        String event = "Issue Closed";

        Notification notification = createIssueNotification(issue, currentUser, event, title);
        createNotification(notification, issue.getRepository().getCollaborators());
    }

    private Notification createIssueNotification(Issue issue, User currentUser, String event, String title) {
        List<Locale> locale = Arrays.asList(Locale.ENGLISH);
        String message = trim(issue.getDescription());
        String link = issue.getURI().getPath();
        if(message.isEmpty()) {
            message = translatorFactory.create(locale).translate("notifications.issue.empty-message");
        }
        return createNotificationObject(currentUser,link,event,message,title);
    }


    public void createPullRequestCreatedNotification(PullRequest pullRequest, User currentUser) {
        List<Locale> locale = Arrays.asList(Locale.ENGLISH);
        String title = translatorFactory.create(locale).translate("notifications.pull-request.create.title",currentUser.getName(), pullRequest.getTitle());
        String event = "Created Pull Request";
        Notification notification = createPullRequestNotification(pullRequest, currentUser, event, title);
        createNotification(notification,pullRequest.getRepository().getCollaborators());
    }

    public void createPullRequestClosedNotification(PullRequest pullRequest, User currentUser) {
        List<Locale> locale = Arrays.asList(Locale.ENGLISH);
        String title = translatorFactory.create(locale).translate("notifications.pull-request.closed.title",currentUser.getName(), pullRequest.getTitle());
        String event = "Closed Pull Request";
        Notification notification = createPullRequestNotification(pullRequest, currentUser, event, title);
        createNotification(notification,pullRequest.getRepository().getCollaborators());
    }

    public void createBranchDeletedNotification(PullRequest pullRequest, User currentUser) {
        List<Locale> locale = Arrays.asList(Locale.ENGLISH);
        String title = translatorFactory.create(locale).translate("notifications.branch.deleted.title",currentUser.getName(), pullRequest.getTitle());
        String event = "Branch deleted";
        Notification notification = createPullRequestNotification(pullRequest, currentUser, event, title);
        createNotification(notification,pullRequest.getRepository().getCollaborators());
    }

    public void createBranchMergedNotification(PullRequest pullRequest, User currentUser) {
        List<Locale> locale = Arrays.asList(Locale.ENGLISH);
        String title = translatorFactory.create(locale).translate("notifications.branch.merged.title",currentUser.getName(), pullRequest.getTitle());
        String event = "Branch Merged";
        Notification notification = createPullRequestNotification(pullRequest, currentUser, event, title);
        createNotification(notification,pullRequest.getRepository().getCollaborators());
    }

    private Notification createPullRequestNotification(PullRequest pullRequest, User currentUser, String event, String title) {
        List<Locale> locale = Arrays.asList(Locale.ENGLISH);
        String message = trim(pullRequest.getDescription());
        String link = pullRequest.getURI().getPath();
        if(message.isEmpty()) {
            message = translatorFactory.create(locale).translate("notifications.pull-request.empty-message");
        }
        return createNotificationObject(currentUser,link,event,message,title);
    }


    public void createIssueCommentNotification(IssueComment comment, User currentUser) {
        List<Locale> locale = Arrays.asList(Locale.ENGLISH);
        String title = translatorFactory.create(locale).translate("notifications.commentOnIssue.title",currentUser.getName(), comment.getIssue().getTitle());
        String event = "Commented on Issue";
        Notification notification = createCommentNotification(comment, currentUser, event, title);
        createNotification(notification,comment.getRepository().getCollaborators());
    }

    public void createPullRequestCommentNotification(IssueComment comment, User currentUser) {
        List<Locale> locale = Arrays.asList(Locale.ENGLISH);
        String title = translatorFactory.create(locale).translate("notifications.commentOnPullRequest.title",comment.getUser(), comment.getIssue().getTitle());
        String event = "Commented on Pull Request";
        Notification notification = createCommentNotification(comment, currentUser, event, title);
        createNotification(notification,comment.getRepository().getCollaborators());
    }

    private Notification createCommentNotification(Comment comment, User currentUser, String event, String title) {
        List<Locale> locale = Arrays.asList(Locale.ENGLISH);
        String message = trim(comment.getContent());
        String link = comment.getURI().getPath();
        if(message.isEmpty()) {
            message = translatorFactory.create(locale).translate("notifications.comment.empty-message");
        }
        return createNotificationObject(currentUser,link,event,message,title);
    }


    private String trim(String string) {
        if(string.length() < MESSAGE_LENGTH) {
            return string;
        }
        return string.substring(0,MESSAGE_LENGTH) + "....";
    }
}
