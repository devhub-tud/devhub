package nl.tudelft.ewi.devhub.webtests.views;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

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
        return new Assignment(getDriver().findElement(By.cssSelector("a[href^=\"deliveries\"]")));
    }

    @Data
    public class Assignment {
        @Getter(AccessLevel.NONE)
        private final WebElement anchor;
        private final Review review = new Review();

        public String getAuthor() {
            return getDriver().findElements(By.tagName("dd")).get(0).getText();
        }

        public String getName() {
            return getDriver().findElement(By.cssSelector("div .col-md-offset-2.col-md-10 h4")).getText();
        }

        public Delivery.State getStatus() {
            return Delivery.State.valueOf(getDriver().findElement(By.cssSelector("span.label")).getText().toUpperCase());
        }

        public Date getDueDate() throws ParseException {
            return new SimpleDateFormat("EEEE dd MMMM yyyy k:m", Locale.US).parse(
                    getDriver().findElements(By.tagName("dd")).get(1).getText());
        }

        public DeliveryReviewView click() {
            anchor.click();
            return new DeliveryReviewView(getDriver());
        }
    }

    @Data
    public class Review {
        public String getReviewer() {
            final String footer = getFooter();
            if (footer == null) {
                return null;
            }

            return footer.split(" on ")[0].trim();
        }

        public double getGrade() {
            final List<WebElement> ddElements = getDriver().findElements(By.cssSelector("blockquote dl dd"));
            if (ddElements.size() == 2) {
                ddElements.get(0).getText();
            } else if (getDriver().findElement(By.cssSelector("blockquote dl dt")).getText().equalsIgnoreCase("Grade")) {
                return Double.parseDouble(ddElements.get(0).getText());
            }

            return 0D;
        }

        @Nullable
        public String getCommentary() {
            final List<WebElement> ddElements = getDriver().findElements(By.cssSelector("blockquote dl dd"));
            if (ddElements.size() == 2) {
                ddElements.get(1).getText();
            } else if (getDriver().findElement(By.cssSelector("blockquote dl dt")).getText().equalsIgnoreCase("Remarks")) {
                    return ddElements.get(0).getText();
            }

            return null;
        }


        @Nullable
        public Date getReviewTime() throws ParseException {
            final String footer = getFooter();
            if (footer == null) {
                return null;
            }

            return new SimpleDateFormat("EEEE dd MMMM yyyy k:m", Locale.US).parse(footer.split(" on ")[1].trim());
        }

        @Nullable
        private String getFooter() {
            try {
                return getDriver().findElement(By.cssSelector("footer.small")).getText();
            } catch (Exception e) {
                return null;
            }
        }
    }
}
