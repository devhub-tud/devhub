package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Arjan on 14-6-2017.
 */
public class NotificationView extends AuthenticatedView {

    private static final String NOTIFICATION1 = "/html/body/div/table/tbody/tr[1]/td";
    private static final String NOTIFICATION2 = "/html/body/div/table/tbody/tr[2]/td";
    private static final String NET_ID = "admin1";

    public NotificationView(WebDriver driver) {
        super(driver);
    }

    @Override
    public void invariant() {
        assertTrue("Wrong Path", currentPathEquals("/notifications/" + NET_ID));
        assertNotNull(By.xpath(NOTIFICATION1));
        assertNotNull(By.xpath(NOTIFICATION2));
    }
}
