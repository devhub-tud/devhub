package nl.tudelft.ewi.devhub.webtests.views;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

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

//    private static final By COURSES_HEADER = By.xpath("//h2[starts-with(normalize-space(.), 'Courses')]");


    public CourseView(WebDriver driver) {
        super(driver);
    }


//    /**
//     * @return A {@link List} of all {@link Project}s in the "Courses" section when logged in as Admin.
//     */
//    public List<CourseOverview> listAvailableCoursesAdmin() {
//        WebElement availableHeader = getDriver().findElement(COURSES_HEADER);
//        WebElement table = Dom.nextSibling(availableHeader, "table");
//        return listProjectOverviewsInTable(table);
//    }

    @Override
    protected void invariant() {
        super.invariant();

        WebElement courseTable = getDriver().findElement(TABLE_XPATH);
        String prevSiblingText = Dom.prevSibling(courseTable).getText();
        assertThat(prevSiblingText, Matchers.containsString("Course Editions"));
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
