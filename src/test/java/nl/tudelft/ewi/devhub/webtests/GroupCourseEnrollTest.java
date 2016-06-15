package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.ContributorsView;
import nl.tudelft.ewi.devhub.webtests.views.CoursesView;
import nl.tudelft.ewi.devhub.webtests.views.GroupEnrollView;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GroupCourseEnrollTest extends WebTest {

    // It is imperative that this student is NOT enrolled in a group
    private static final String NET_ID = "student5";
    private static final String PASSWORD = "student5";
    private static final String OTHER_STUDENT_ID = "student6";
    private static final String STUDENT_NAME = "Student Five";
    private static final String OTHER_STUDENT_NAME = "Student Six";
    private static final String STUDENT_EMAIL = "student-5@student.tudelft.nl";
    private static final String OTHER_STUDENT_EMAIL = "student-6@student.tudelft.nl";

    // Web elements
    private static final By NEXT_BUTTON = By.name("next");

    /**
     * <h1>Scenario: Enrolling as a group</h1>
     *
     * Given that:
     * <ol>
     *     <li>I am successfully logged in.</li>
     *     <li>There is a course available to me.</li>
     * </ol>
     * When:
     * <ol>
     *     <li>I click enroll for that course</li>
     * </ol>
     * Then:
     * <ol>
     *     <li>I am redirected to the group creation page</li>
     * </ol>
     */
    @Test
    public void testClickEnrollInCourse() {
        CoursesView view = openLoginScreen()
                .login(NET_ID, PASSWORD)
                .toCoursesView();

        List<CoursesView.CourseOverview> courses = view.listAvailableCourses();
        assertEquals(1, courses.size());

        courses.get(0).clickEnroll();
    }

    /**
     * <h1>Scenario: Enrolling as a group</h1>
     *
     * Given that:
     * <ol>
     *     <li>I am successfully logged in.</li>
     *     <li>I am setting up a group for a new course.</li>
     * </ol>
     * When:
     * <ol>
     *     <li>I entered a correct student id for my partner.</li>
     * </ol>
     * Then:
     * <ol>
     *     <li>I can click on next to enroll.</li>
     * </ol>
     */
    @Test
    public void testCorrectStudentNumber() {
        CoursesView view = openLoginScreen()
                .login(NET_ID, PASSWORD)
                .toCoursesView();

        List<CoursesView.CourseOverview> courses = view.listAvailableCourses();
        assertEquals(1, courses.size());

        GroupEnrollView groupView = courses.get(0)
                .clickEnroll()
                .setMemberField(2, OTHER_STUDENT_ID);

        this.waitForCondition(5000, webDriver -> {
            WebElement nextBtn = getDriver().findElement(NEXT_BUTTON);
            return nextBtn.isEnabled();
        });
        groupView.clickNext();

        List<User> groupMembers = groupView.groupMembers();
        assertEquals(NET_ID, groupMembers.get(0).getNetId());
        assertEquals(OTHER_STUDENT_ID, groupMembers.get(1).getNetId());
        assertEquals(STUDENT_NAME, groupMembers.get(0).getName());
        assertEquals(OTHER_STUDENT_NAME, groupMembers.get(1).getName());
    }

    /**
     * <h1>Scenario: Enrolling as a group</h1>
     *
     * Given that:
     * <ol>
     *     <li>I am successfully logged in.</li>
     *     <li>I am setting up a group for a new course.</li>
     *     <li>I entered a correct student id for my partner.</li>
     * </ol>
     * When:
     * <ol>
     *     <li>I click next</li>
     * </ol>
     * Then:
     * <ol>
     *     <li>I am in a group with the other student.</li>
     *     <li>I enrolled for the course.</li>
     * </ol>
     */
    @Test
    public void testCorrectStudentNumberFullyEnroll() {
        CoursesView view = openLoginScreen()
                .login(NET_ID, PASSWORD)
                .toCoursesView();

        List<CoursesView.CourseOverview> courses = view.listAvailableCourses();
        assertEquals(1, courses.size());

        CoursesView.CourseOverview course = courses.get(0);
        String courseName = course.getCourseName();
        GroupEnrollView groupView = course.clickEnroll()
                .setMemberField(2, OTHER_STUDENT_ID);

        this.waitForCondition(5, webDriver -> {
            WebElement nextBtn = getDriver().findElement(NEXT_BUTTON);
            return nextBtn.isEnabled();
        });
        ContributorsView contributorsView = groupView.clickNext()
                .clickCreateGroup()
                .toContributorsView();

        List<ContributorsView.Contributor> contributors = contributorsView.listContributors();
        assertEquals(STUDENT_NAME, contributors.get(0).getName());
        assertEquals(OTHER_STUDENT_NAME, contributors.get(1).getName());
        assertEquals(NET_ID, contributors.get(0).getNetID());
        assertEquals(OTHER_STUDENT_ID, contributors.get(1).getNetID());
        assertEquals(STUDENT_EMAIL, contributors.get(0).getEmail());
        assertEquals(OTHER_STUDENT_EMAIL, contributors.get(1).getEmail());

        assertTrue(contributorsView.toCoursesView().listMyProjects().get(0).getName().contains(courseName));
    }

    /**
     * Test if the group will not be created with an invalid student number.
     */
    @Test
    public void testWrongStudentNumber() {
        CoursesView view = openLoginScreen()
                .login(NET_ID, PASSWORD)
                .toCoursesView();

        List<CoursesView.CourseOverview> courses = view.listAvailableCourses();
        assertEquals(1, courses.size());
        GroupEnrollView groupView = courses.get(0)
                .clickEnroll()
                .setMemberField(1, "coffee")
                .setMemberField(2, "is life");

        assertTrue(groupView.memberFieldContainsError(1));
        assertTrue(groupView.memberFieldContainsError(2));

        CoursesView coursesView = groupView.clickNext()
                .toCoursesView();

        assertEquals(0, coursesView.listMyProjects().size());
    }
}
