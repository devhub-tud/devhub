package nl.tudelft.ewi.devhub.webtests.views;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import org.eclipse.jgit.annotations.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

/**
 * Created by Douwe Koopmans on 4-5-16.
 */
public class AssignmentView extends ProjectSidebarView {

    public AssignmentView(WebDriver driver) {
        super(driver);
    }
    
    @Override
    protected void invariant() {
        super.invariant();
        assertTrue(currentPathContains("/assignment"));
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

        public Optional<String> getReviewer() {
            return getFooter().map(footer -> footer.split(" on ")[0].trim());
        }

        public Optional<Double> getGrade() {
            final List<WebElement> ddElements = getDriver().findElements(By.cssSelector("blockquote dl dd"));
            if (getDriver().findElement(By.cssSelector("blockquote dl dt")).getText().equalsIgnoreCase("Grade")) {
                return Optional.of(Double.parseDouble(ddElements.get(0).getText()));
            }
            return Optional.empty();
        }

        @Nullable
        public String getCommentary() {
            final List<WebElement> ddElements = getDriver().findElements(By.cssSelector("blockquote dl dd"));
            if (getDriver().findElement(By.cssSelector("blockquote dl dt")).getText().equalsIgnoreCase("Remarks")) {
                return ddElements.get(0).getText();
            }
            return null;
        }

        public Optional<Date> getReviewTime() throws ParseException {
            return getFooter().map(footer -> parseDateString(footer.split(" on ")[1].trim()));
        }

        private Optional<String> getFooter() {
            return getDriver().findElements(By.cssSelector("footer.small")).stream().findAny().map(WebElement::getText);
        }

    }

    @SneakyThrows
    private static Date parseDateString(String date) {
        return new SimpleDateFormat("EEEE dd MMMM yyyy k:m", Locale.US).parse(date);
    }

}
