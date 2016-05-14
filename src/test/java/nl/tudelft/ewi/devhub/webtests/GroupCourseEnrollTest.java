package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.CoursesView;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class GroupCourseEnrollTest extends WebTest {

    // It is imperative that this student is NOT enrolled in a group
    private static final String NET_ID = "student5";
    private static final String PASSWORD = "student5";

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
}
