package nl.tudelft.ewi.devhub.webtests;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.database.controllers.Issues;
import nl.tudelft.ewi.devhub.server.database.controllers.NotificationController;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.IssueCreatedNotification;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.CoursesView;
import nl.tudelft.ewi.devhub.webtests.views.NotificationView;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Arjan on 14-6-2017.
 */
public class NotificationTest extends WebTest {

    private static final String NET_ID = "student1";
    private static final String PASSWORD = "student1";

    @Inject
    private Users users;

    @Inject
    private NotificationController notificationController;

    @Inject
    private Issues issues;

    private User user;

    @Before
    public void createIssueWithNotification() {
        user = users.findByNetId(NET_ID);
        users.refresh(user);

        Issue issue = new Issue();
        issue.setTitle("test issue");
        issue.setDescription("Some description");
        issue.setOpen(true);
        issue.setRepository(user.getGroups().iterator().next().getRepository());
        issues.persist(issue);

        IssueCreatedNotification issueCreatedNotification = new IssueCreatedNotification();
        issueCreatedNotification.setSender(user);
        issueCreatedNotification.setRecipients(Maps.newHashMap(user, false));
        issueCreatedNotification.setIssue(issue);
        notificationController.persist(issueCreatedNotification);
    }


    @Test
    public void markAsRead() {
        CoursesView coursesView = openLoginScreen().login(NET_ID,PASSWORD);
        NotificationView notificationView = coursesView.toNotificationView();

        assertThatNumberOfReadAndUnreadNotificationsMatch(notificationView);

        notificationView.getUnreadNotifications().iterator().next()
                .clickAndMarkAsRead()
                .toNotificationView();

        assertThatNumberOfReadAndUnreadNotificationsMatch(notificationView);
    }

    private void assertThatNumberOfReadAndUnreadNotificationsMatch(NotificationView notificationView) {
        long unreadNotifications = getNumberOfUnreadNotifications();
        long readNotifications = getNumberOfReadNotifications();

        assertEquals(notificationView.getReadNotifications().size(), readNotifications);
        assertEquals(notificationView.getUnreadNotifications().size(), unreadNotifications);
    }

    private long getNumberOfUnreadNotifications() {
        return notificationController.getLatestNotificationsFor(user).values()
                .stream().filter(Boolean.FALSE::equals)
                .count();
    }

    private long getNumberOfReadNotifications() {
        return notificationController.getLatestNotificationsFor(user).values()
                .stream().filter(Boolean.TRUE::equals)
                .count();
    }

}
