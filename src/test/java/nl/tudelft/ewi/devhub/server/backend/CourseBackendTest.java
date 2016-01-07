package nl.tudelft.ewi.devhub.server.backend;

import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;

import com.google.common.collect.Sets;

import nl.tudelft.ewi.git.web.api.GroupApi;
import nl.tudelft.ewi.git.web.api.GroupsApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CourseBackendTest extends BackendTest {
	
	@Mock
	private User currentUser;
	
	@Mock
	private Users currentUsers;
	
	@Mock
	private CourseEditions courses;

	
	@InjectMocks
	private CoursesBackend courseBackend;
	
	@Mock
	private GroupsApi groups;

	@Mock
	private GroupApi groupApi;

	private Set<User> newAssistants;

	private Set<User> oldAssistants;

	private CourseEdition course;
	
	@Before
	public void setUp() {
		course = createCourseEdition();
		oldAssistants = Sets.newHashSet(createUser(), createUser());
		newAssistants = Sets.newHashSet(createUser(), createUser());
		course.setAssistants(oldAssistants);
		
		when(currentUser.isAdmin()).thenReturn(true);
		when(groups.getGroup(Matchers.any())).thenReturn(groupApi);

	}

	private User[] createUsersArray() {
		int numberOfUsers = 5;
		User[] assistants = new User[numberOfUsers];
		
		for (int i = 0; i < numberOfUsers; i++) {
			assistants[i] = createUser();
		}
		
		return assistants;
	}
	
	@Test(expected=UnauthorizedException.class)
	public void mustBeAdminToCreateGroup() {
		when(currentUser.isAdmin()).thenReturn(false);
		
		courseBackend.createCourse(course);
	}
	
	@Test
	public void courseIsStored() {
		courseBackend.createCourse(course);
		
		verify(courses).persist(course);
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=Exception.class)
	public void courseIsRemovedWhenFailedToStore() {
		when(courses.persist(Matchers.eq(course))).thenThrow(Exception.class);
		
		courseBackend.createCourse(course);
	}
	
	@Test(expected=UnauthorizedException.class)
	public void mustBeAdminToMergeGroup() {
		when(currentUser.isAdmin()).thenReturn(false);
		
		courseBackend.mergeCourse(course);
	}
	
	@Test
	public void courseIsMerged() {
		courseBackend.mergeCourse(course);
		
		verify(courses).merge(course);
	}
	
	@Test(expected=UnauthorizedException.class)
	public void mustBeAdminToSetAssistants() {
		when(currentUser.isAdmin()).thenReturn(false);
		
		courseBackend.setAssistants(course, new ArrayList<User>());
	}

	@Test
	public void assistantsAreAddedToCourse() {
		courseBackend.setAssistants(course, newAssistants);

		assertEquals(newAssistants, course.getAssistants());
	}
	
	@Test
	public void assistantsAreRemovedFromCourseWhenNotInNewList() {
		courseBackend.setAssistants(course, newAssistants);

		User removedAssistant = newAssistants.iterator().next();
		newAssistants.remove(removedAssistant);

		courseBackend.setAssistants(course, newAssistants);

		assertFalse(course.getAssistants().contains(removedAssistant));
	}
}
