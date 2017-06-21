package nl.tudelft.ewi.devhub.webtests;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.database.controllers.NotificationUserController;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.NotificationsToUsers;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.CourseView;
import nl.tudelft.ewi.devhub.webtests.views.CoursesView;
import nl.tudelft.ewi.devhub.webtests.views.LoginView;
import nl.tudelft.ewi.devhub.webtests.views.NotificationView;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Arjan on 14-6-2017.
 */
public class NotificationTest extends WebTest {

    private static final String NET_ID = "admin1";
    private static final String PASSWORD = "admin1";

    @Inject
    private Config config;
    @Inject
    private Users users;
    @Inject
    private NotificationUserController notificationUserController;

    private static final By MARK_READ_BUTTON = By.xpath("/html/body/div/table/tbody/tr[2]/td/form/button");
    private static final By NOTIFICATION_INDICATOR = By.xpath("//*[@id=\"bs-example-navbar-collapse-1\"]/ul/li[1]/a/span");
    private static final By NOTIFICATIONS_READ = By.xpath("//*[@class=\"notification read\"]");
    private static final By NOTIFICATIONS_UNREAD = By.xpath("//*[@class=\"notification unread\"]");


    @Test
    public void markAsRead() {
        CoursesView coursesView = openLoginScreen().login(NET_ID,PASSWORD);
        NotificationView notificationView = coursesView.toNotificationView();
        User user = users.findByNetId(NET_ID);
        users.refresh(user);


        int unreadNotifications = user.unreadNotifications();
        int readNotifcitions = user.readNotifications();

        assertEquals(notificationView.getReadNotifications().size(),readNotifcitions);
        assertEquals(notificationView.getUnreadNotifications().size(),unreadNotifications);

        notificationView = notificationView.markRead(0);

        for(NotificationsToUsers notificationsToUsers: user.getNotificationsToUsersList()) {
            notificationUserController.refresh(notificationsToUsers);
        }
        
        assertEquals(notificationView.getUnreadNotifications().size(),user.unreadNotifications());
        assertEquals(notificationView.getReadNotifications().size(),user.readNotifications());

        assertTrue(notificationView.getReadNotifications().size() == readNotifcitions + 1|| unreadNotifications == 0);
        assertTrue(notificationView.getUnreadNotifications().size() == unreadNotifications - 1  || unreadNotifications == 0);
    }
}
