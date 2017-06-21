package nl.tudelft.ewi.devhub.webtests.views;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.webtests.utils.Dom;

import org.hamcrest.Matchers;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;


/**
 * Created by Pilmus on 14-6-2017.
 */

public class CourseView extends AuthenticatedView {

    private static final By TABLE_XPATH = By.xpath("//table");

    public CourseView(WebDriver driver) {
        super(driver);
    }

    @Override
    protected void invariant() {
        super.invariant();

        WebElement courseTable = getDriver().findElement(TABLE_XPATH);
        String prevSiblingText = Dom.prevSibling(courseTable).getText();
        assertThat(prevSiblingText, Matchers.containsString("Course Editions"));
    }

    public List<CourseEditionLink> listCourseEditions() {
        invariant();
        WebElement courseTable = getDriver().findElement(TABLE_XPATH);
        return listCourseEditionsInTable(courseTable);
    }

    private List<CourseEditionLink> listCourseEditionsInTable(WebElement table) {
        List<WebElement> entries = table.findElements(By.tagName("td"));
        List<CourseEditionLink> courseEditions = Lists.newArrayList();
        for (WebElement entry : entries) {
            WebElement courseEditionLink = entry.findElement(By.tagName("a"));
            CourseEditionLink courseEdition = new CourseEditionLink(courseEditionLink.getText(), courseEditionLink);
            courseEditions.add(courseEdition);
        }
        return courseEditions;
    }

    @Data
    public class CourseEditionLink {
        private final String name;

        @Getter(AccessLevel.NONE)
        private final WebElement anchor;

        public CourseEditionView click() {
            anchor.click();
            return new CourseEditionView(getDriver());
        }
    }
}
