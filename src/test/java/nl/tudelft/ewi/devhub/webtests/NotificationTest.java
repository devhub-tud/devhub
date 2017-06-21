package nl.tudelft.ewi.devhub.webtests;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.Config;
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

    private static final By MARK_READ_BUTTON = By.xpath("/html/body/div/table/tbody/tr[2]/td/form/button");
    private static final By NOTIFICATION_INDICATOR = By.xpath("//*[@id=\"bs-example-navbar-collapse-1\"]/ul/li[1]/a/span");
    private static final By NOTIFICATIONS_READ = By.xpath("//*[@class=\"notification read\"]");
    private static final By NOTIFICATIONS_UNREAD = By.xpath("//*[@class=\"notification unread\"]");


    @Test
    public void markAsRead() throws InterruptedException {
        CoursesView coursesView = openLoginScreen().login(NET_ID,PASSWORD);
        NotificationView notificationView = coursesView.toNotificationView();

        assertEquals(notificationView.getUnreadNotifications().size(),1);
        assertEquals(notificationView.getReadNotifacations().size(),1);
        assertTrue(false);
        /*getDriver().navigate().to("http://localhost:"  + config.getHttpPort() + "/notifications/" + NET_ID);

        //String url = getDriver().getCurrentUrl();
        //notificationView.waitUntilCurrentUrlDiffersFrom(url);

        LoginView loginView = new LoginView(getDriver());
        loginView.setPasswordField(PASSWORD);
        loginView.setUsernameField(NET_ID);

        String url = getDriver().getCurrentUrl();

        loginView.clickLoginButton();

        loginView.waitUntilCurrentUrlDiffersFrom(url);

        NotificationView notificationView  = new NotificationView(getDriver());
        notificationView.invariant();

        WebElement button = getDriver().findElement(MARK_READ_BUTTON);
        assertNotNull(button);


        // Test if there is a read message and a unread message

        assertEquals(1,getDriver().findElements(NOTIFICATIONS_READ).size());
        assertEquals(1,getDriver().findElements(NOTIFICATIONS_UNREAD).size());
        assertNotNull(getDriver().findElement(NOTIFICATION_INDICATOR));

        button.click();

        notificationView = new NotificationView(getDriver());
        notificationView.invariant();

        // Test if now both messages are marked as read.
        assertEquals(2,getDriver().findElements(NOTIFICATIONS_READ).size());
        assertEquals(0,getDriver().findElements(NOTIFICATIONS_UNREAD).size());*/
    }
}
