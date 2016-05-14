package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.CoursesView;
import nl.tudelft.ewi.devhub.webtests.views.GroupEnrollView;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class GroupCourseEnrollTest extends WebTest {

    // It is imperative that this student is NOT enrolled in a group
    private static final String NET_ID = "student5";
    private static final String PASSWORD = "student5";
    private static final String OTHER_STUDENT_ID = "student6";
    private static final String STUDENT_NAME = "Student Five";
    private static final String OTHER_STUDENT_NAME = "Student Six";

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
                .setMember2Field(OTHER_STUDENT_ID)
                .clickNext();

        List<User> groupMembers = groupView.groupMembers();
        assertEquals(NET_ID, groupMembers.get(0).getNetId());
        assertEquals(OTHER_STUDENT_ID, groupMembers.get(1).getNetId());
        assertEquals(STUDENT_NAME, groupMembers.get(0).getName());
        assertEquals(OTHER_STUDENT_NAME, groupMembers.get(1).getName());
    }
}
