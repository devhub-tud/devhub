package nl.tudelft.ewi.devhub.server.backend;

import java.util.ArrayList;

import nl.tudelft.ewi.devhub.server.database.controllers.CourseAssistants;
import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseAssistant;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.GroupMembers;
import nl.tudelft.ewi.git.client.Groups;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CourseBackendTest extends BackendTest {
	
	@Mock
	private User currentUser;
	
	@Mock
	private Users currentUsers;
	
	@Mock
	private nl.tudelft.ewi.git.client.Users users;
	
	@Mock
	private Courses courses;
	
	@Mock
	private GitServerClient gitServerClient;
	
	@InjectMocks
	private CoursesBackend courseBackend;
	
	@Mock
	private Groups groups;
	
	@Mock
	private GroupMembers groupMembers;
	
	@Mock
	private CourseAssistants courseAssistantsDAO;

	private ArrayList<User> newAssistants;

	private ArrayList<CourseAssistant> oldAssistants;
	
	private Course course;
	
	@Before
	public void setUp() throws GitClientException {
		course = createCourse();
		newAssistants = Lists.newArrayList(createUsersArray());
		oldAssistants = Lists.newArrayList(createAssistantsArray());
		course.setCourseAssistants(oldAssistants);
		
		when(currentUser.isAdmin()).thenReturn(true);
		when(groups.ensureExists(Matchers.any())).thenReturn(null);
		when(gitServerClient.groups()).thenReturn(groups);
		when(gitServerClient.users()).thenReturn(users);
		when(groups.groupMembers(Matchers.any())).thenReturn(groupMembers);

		when(courseAssistantsDAO.persist(any())).thenAnswer((invocation -> {
			CourseAssistant courseAssistant = (CourseAssistant) invocation.getArguments()[0];
			oldAssistants.add(courseAssistant);
			return courseAssistant;
		}));

		when(courseAssistantsDAO.delete(any())).thenAnswer((invocation -> {
			CourseAssistant courseAssistant = (CourseAssistant) invocation.getArguments()[0];
			oldAssistants.remove(courseAssistant);
			return courseAssistant;
		}));
	}

	private User[] createUsersArray() {
		int numberOfUsers = 5;
		User[] assistants = new User[numberOfUsers];
		
		for (int i = 0; i < numberOfUsers; i++) {
			assistants[i] = createUser();
		}
		
		return assistants;
	}

	private CourseAssistant[] createAssistantsArray() {
		int numberOfAssistants = 5;
		CourseAssistant[] assistants = new CourseAssistant[numberOfAssistants];
		
		for (int i = 0; i < numberOfAssistants; i++) {
			CourseAssistant assistant = new CourseAssistant();
			assistant.setCourse(course);
			assistant.setUser(createUser());
			
			assistants[i] = assistant;
		}
		
		return assistants;
	}
	
	@Test(expected=UnauthorizedException.class)
	public void mustBeAdminToCreateGroup() throws GitClientException {
		when(currentUser.isAdmin()).thenReturn(false);
		
		courseBackend.createCourse(course);
	}
	
	@Test
	public void courseIsStored() throws GitClientException {
		courseBackend.createCourse(course);
		
		verify(courses).persist(course);
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=Exception.class)
	public void courseIsRemovedWhenFailedToStore() throws GitClientException {
		when(courses.persist(Matchers.eq(course))).thenThrow(Exception.class);
		
		courseBackend.createCourse(course);
	}
	
	@Test(expected=UnauthorizedException.class)
	public void mustBeAdminToMergeGroup() throws GitClientException {
		when(currentUser.isAdmin()).thenReturn(false);
		
		courseBackend.mergeCourse(course);
	}
	
	@Test
	public void courseIsMerged() throws GitClientException {
		courseBackend.mergeCourse(course);
		
		verify(courses).merge(course);
	}
	
	@Test(expected=UnauthorizedException.class)
	public void mustBeAdminToSetAssistants() throws GitClientException {
		when(currentUser.isAdmin()).thenReturn(false);
		
		courseBackend.setAssistants(course, new ArrayList<User>());
	}

	@Test
	public void assistantsAreAddedToCourse() throws GitClientException {
		courseBackend.setAssistants(course, newAssistants);

		assertEquals(newAssistants, course.getAssistants());
	}
}
