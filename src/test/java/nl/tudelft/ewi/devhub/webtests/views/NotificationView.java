package nl.tudelft.ewi.devhub.webtests.views;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Collection;
import java.util.List;

/**
 * Created by Arjan on 14-6-2017.
 */
public class NotificationView extends AuthenticatedView {

    private static final By NOTIFICATION_ELEMENT = By.xpath("//td[contains(@class, 'notification')]");

    public NotificationView(WebDriver driver) {
        super(driver);
    }

    public List<NotificationListElement> getNotifications() {
        return Lists.transform(getDriver().findElements(NOTIFICATION_ELEMENT), NotificationListElement::new);
    }

    public Collection<NotificationListElement> getReadNotifications() {
        return Collections2.filter(getNotifications(), NotificationListElement::isRead);
    }

    public Collection<NotificationListElement> getUnreadNotifications() {
        return Collections2.filter(getNotifications(), NotificationListElement::isUnread);
    }

    @Data public class NotificationListElement {

        private final WebElement webElement;

        public boolean isRead() {
            return !isUnread();
        }

        public boolean isUnread() {
            return webElement.getAttribute("class").contains("unread");
        }

        public AuthenticatedView clickAndMarkAsRead() {
            webElement.findElement(By.tagName("a")).click();
            return new AuthenticatedView(getDriver());
        }

    }

}
