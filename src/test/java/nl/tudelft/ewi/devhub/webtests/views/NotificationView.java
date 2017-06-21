package nl.tudelft.ewi.devhub.webtests.views;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.management.openmbean.TabularData;

import java.util.List;

import static nl.tudelft.ewi.devhub.webtests.utils.WebTest.NET_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Arjan on 14-6-2017.
 */
public class NotificationView extends AuthenticatedView {

    private static final By MARK_READ_BUTTON = By.xpath("//*[@class=\"notification unread\"]/a/form/button");
    private static final By NOTIFICATION_INDICATOR = By.xpath("//*[@id=\"bs-example-navbar-collapse-1\"]/ul/li[1]/a/span");
    private static final By NOTIFICATIONS_READ = By.xpath("//*[@class=\"notification read\"]");
    private static final By NOTIFICATIONS_UNREAD = By.xpath("//*[@class=\"notification unread\"]");

    public NotificationView(WebDriver driver) {
        super(driver);
    }

    @Override
    public void invariant() {
        assertTrue("Wrong Path", currentPathEquals("/notifications/admin1"));
        assertEquals(getDriver().findElements(NOTIFICATIONS_UNREAD).size(),1);

    }

    public NotificationView markRead(int i) {
        List<WebElement> buttons = getDriver().findElements(MARK_READ_BUTTON);
        if(i < buttons.size()) {
            WebElement button = buttons.get(i);
            button.click();
        }
        return this;
    }

    public List<WebElement> getReadNotifications() {
        return getDriver().findElements(NOTIFICATIONS_READ);
    }

    public List<WebElement> getUnreadNotifications() {
        return getDriver().findElements(NOTIFICATIONS_UNREAD);
    }
}
