package nl.tudelft.ewi.devhub.webtests.views;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Douwe Koopmans on 4-5-16.
 */
public class AssignmentView extends ProjectSidebarView {

    private static final By RECENT_ASSIGNMENTS_HEADER = By.xpath("//h4[starts-with(normalize-space(.), 'Assignments')]");
    public AssignmentView(WebDriver driver) {
        super(driver);
    }
    
    @Override
    protected void invariant() {
        super.invariant();
        assertNotNull(getDriver().findElement(RECENT_ASSIGNMENTS_HEADER));
        assertTrue(currentPathStartsWith("/courses/ti1705/TI1705/groups/"));
    }

    public Assignment getAssignment() throws ParseException {
        WebElement submit = getDriver().findElement(By.cssSelector("a[href^=\"deliveries\"]"));

        final List<WebElement> elements = getDriver().findElements(By.tagName("dd"));
        final String author = elements.get(0).getText();
        final Date date = new SimpleDateFormat("EEEE dd MMMM yyyy k:m", Locale.US).parse(elements.get(1).getText());
        final String name = getDriver().findElement(By.cssSelector("div .col-md-offset-2.col-md-10")).getText();
        final String status = getDriver().findElement(By.cssSelector("span.label")).getText();

        return new Assignment(author, name, status, date, submit);
    }

    @Data
    public class Assignment {
        private final String author, name, status;
        private final Date date;
        @Getter(AccessLevel.NONE)
        private final WebElement anchor;
        public DiffInCommitView click() {
            anchor.click();
            return new DiffInCommitView(getDriver());
        }
    }
}
