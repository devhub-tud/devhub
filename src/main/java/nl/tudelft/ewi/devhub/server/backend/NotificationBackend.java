package nl.tudelft.ewi.devhub.server.backend;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.controllers.NotificationController;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.comments.Comment;
import nl.tudelft.ewi.devhub.server.database.entities.issues.AbstractIssue;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.*;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * Created by Arjan on 13-6-2017.
 */
public class NotificationBackend {

    private final NotificationController notificationController;
    private final Provider<User> currentUserProvider;


    @Inject
    public NotificationBackend(NotificationController notificationController, @Named("current.user") Provider<User> currentUserProvider) {
        this.notificationController = notificationController;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public <T extends Notification & HasWatchable> void createNotification(T notification) {
        createNotification(notification, notification.getWatchable().getWatchers());
    }

    @Transactional
    private void createNotification(Notification notification, Collection<User> receivers) {
        AtomicReference<User> userAtomicReference = new AtomicReference<>();
        try {
            userAtomicReference.set(currentUserProvider.get());
        }
        catch (Exception e) {
            // System
        }

        notification.setRecipients(
            receivers.stream()
                .filter(user -> ! user.equals(userAtomicReference.get()))
                .collect(Collectors.toMap(Function.identity(), a -> false))
        );

        notification.setSender(userAtomicReference.get());
        notificationController.persist(notification);
    }

    public void createIssueCreatedNotification(AbstractIssue issue) {
        IssueCreatedNotification issueCreatedNotification = new IssueCreatedNotification();
        issueCreatedNotification.setIssue(issue);
        createNotification(issueCreatedNotification);

        if ( nonNull( issue.getAssignee() )) {
            IssueAssignedNotification issueAssignedNotification = new IssueAssignedNotification();
            issueAssignedNotification.setIssue(issue);
            createNotification(issueAssignedNotification, Collections.singleton(issue.getAssignee()));
        }
    }

    public void createIssueEditedNotification(AbstractIssue issue) {
        IssueEditedNotification issueEditedNotification = new IssueEditedNotification();
        issueEditedNotification.setIssue(issue);
        createNotification(issueEditedNotification);
    }

    public void createIssueClosedNotification(AbstractIssue issue) {
        IssueClosedNotification issueClosedNotification = new IssueClosedNotification();
        issueClosedNotification.setIssue(issue);
        createNotification(issueClosedNotification);
    }

    private void createIssueNotification(AbstractIssue issue) {
        IssueCreatedNotification issueCreatedNotification = new IssueCreatedNotification();
        issueCreatedNotification.setIssue(issue);
        createNotification(issueCreatedNotification);
    }

    public void createBranchMergedNotification(PullRequest pullRequest) {
        PullRequestMergedNotification pullRequestMergedNotification = new PullRequestMergedNotification();
        pullRequestMergedNotification.setPullRequest(pullRequest);
        createNotification(pullRequestMergedNotification);
    }

    public void createCommentNotification(Comment comment) {
        CommentNotification commentNotification = new CommentNotification();
        commentNotification.setComment(comment);
        createNotification(commentNotification);
    }

}
