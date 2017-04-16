package nl.tudelft.ewi.devhub.server.web.resources;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProjectAssignmentsResourceTest {
    @Mock
    private CourseEdition edition;

    @Mock
    private Group group;

    @Mock
    private User user;

    @Mock
    private Assignment assignment;

    private ProjectAssignmentsResource resource;

    @Before
    public void setUp() {
        resource = new ProjectAssignmentsResource(null, user, group, null, null, null, null, null, null);
        Mockito.when(group.getCourseEdition()).thenReturn(edition);
    }

    @Test
    public void testCanSeeGrade() {
        Mockito.when(assignment.isGradesReleased()).thenReturn(true);
        assertTrue(resource.canSeeGrade(assignment));
    }

    @Test
    public void testNotReleasedStudent() {
        Mockito.when(assignment.isGradesReleased()).thenReturn(false);
        assertFalse(resource.canSeeGrade(assignment));
    }

    @Test
    public void testNotReleasedAdmin() {
        Mockito.when(user.isAdmin()).thenReturn(true);
        assertTrue(resource.canSeeGrade(assignment));
    }

    @Test
    public void testNotReleasedTA() {
        Mockito.when(user.isAssisting(edition)).thenReturn(true);
        assertTrue(resource.canSeeGrade(assignment));
    }
}