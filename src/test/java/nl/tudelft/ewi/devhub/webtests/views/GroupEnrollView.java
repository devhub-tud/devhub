package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

public class GroupEnrollView extends AuthenticatedView {

    public GroupEnrollView(WebDriver driver) {
        super(driver);
    }

    @Override
    protected void invariant() {
        super.invariant();
        assertTrue(currentPathStartsWith("/courses"));
        assertTrue(currentPathContains("/enroll"));
    }
}
