package nl.tudelft.ewi.devhub.webtests.views;

import lombok.Data;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery.State;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Created by Douwe Koopmans on 9-5-16.
 */
public class DeliveryReviewView extends ProjectSidebarView {

    private static final By STATE_SELECTOR = By.cssSelector("select#state");
    private static final By GRADE_SELECTOR = By.cssSelector("input#grade");
    private static final By COMMENTARY_SELECTOR = By.cssSelector("textarea#commentary");

    public DeliveryReviewView(WebDriver driver) {
        super(driver);
    }

    @Override
    public void invariant() {
        super.invariant();
        Assert.assertTrue(currentPathContains("deliveries"));
    }

    public Assignment getAssignment() {
        return new Assignment();
    }

    public DeliveryForm getDelivery() {
        return new DeliveryForm(getDriver().findElement(By.cssSelector("button[type=\"submit\"]")));
    }

    @Data
    public class Assignment {

        private final Review review = new Review();

        public String getAuthor() {
            return getDriver().findElements(By.tagName("dd")).get(0).getText();
        }

        public String getName() {
            return getDriver().findElement(By.cssSelector("div .col-md-offset-2.col-md-10 h4")).getText()
                    .replaceFirst("^Assignment ", "");
        }

        public Delivery.State getStatus() {
            return Delivery.State.valueOf(getDriver().findElement(By.cssSelector("span.label")).getText().toUpperCase());
        }

        public Date getDueDate() throws ParseException {
            return new SimpleDateFormat("EEEE dd MMMM yyyy k:m", Locale.US).parse(
                    getDriver().findElements(By.tagName("dd")).get(1).getText());
        }
    }

    @Data
    public class Review {

        public Optional<String> getReviewer() {
            return getFooter().map(footer -> footer.split(" on ")[0].trim());
        }

        public Double getGrade() {
            final List<WebElement> ddElements = getDriver().findElements(By.cssSelector("blockquote dl dd"));
            if (ddElements.size() == 2) {
                ddElements.get(0).getText();
            } else if (getDriver().findElement(By.cssSelector("blockquote dl dt")).getText().equalsIgnoreCase("Grade")) {
                return Double.parseDouble(ddElements.get(0).getText());
            }

            return null;
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
            final Optional<String> footer = getFooter();
            if (!footer.isPresent()) {
                return null;
            }

            return new SimpleDateFormat("EEEE dd MMMM yyyy k:m", Locale.US).parse(footer.get().split(" on ")[1].trim());
        }

        private Optional<String> getFooter() {
            return getDriver().findElements(By.cssSelector("footer.small")).stream().findAny().map(WebElement::getText);
        }

    }


    public class DeliveryForm {

        private final WebElement anchor;

        DeliveryForm(WebElement anchor) {
            this.anchor = anchor;
        }

        public State getSelectedState() {
            final WebElement element = getDriver().findElement(STATE_SELECTOR);
            Select select = new Select(element);
            String selected = select.getFirstSelectedOption().getText().replaceAll("\\s+", "");

            return State.valueOf(selected.toUpperCase());
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        public void setState(Delivery.State state) throws NoSuchElementException {
            invariant();
            final WebElement element = getDriver().findElement(STATE_SELECTOR);
            final Select select = new Select(element);

            select.getOptions()
                .stream()
                .filter(webElement -> 
                    webElement.getText()
                	    .replaceAll("\\s+", "")
                	    .equalsIgnoreCase(state.toString()))
                .findAny().get().click();
        }

        public double getGrade() {
            return Double.parseDouble(getDriver().findElement(GRADE_SELECTOR).getAttribute("value"));
        }

        public void setGrade(double newGrade) throws IllegalArgumentException {
            invariant();
            final WebElement element = getDriver().findElement(GRADE_SELECTOR);
            element.clear();
            element.sendKeys(Objects.toString(newGrade));
        }

        public String getCommentary() {
            return getDriver().findElement(COMMENTARY_SELECTOR).getAttribute("value");
        }

        public void setCommentary(String commentary) {
            final WebElement element = getDriver().findElement(COMMENTARY_SELECTOR);

            element.clear();
            element.sendKeys(commentary);
        }

        public DeliveryReviewView click() {
            anchor.click();
            return new DeliveryReviewView(getDriver());
        }

        public List<WebElement> getFirstMasteryForEachCharacteristic() {
            return getDriver().findElements(By.cssSelector(".characteristic"))
                    .stream()
                    .map((e) -> e.findElement(By.cssSelector("[type='radio']")))
                    .collect(Collectors.toList());
        }

        public boolean isSubmitButtonDisabled() {
            return "true".equals(anchor.getAttribute("disabled"));
        }

    }
}
