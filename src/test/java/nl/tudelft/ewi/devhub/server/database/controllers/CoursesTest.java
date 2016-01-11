package nl.tudelft.ewi.devhub.server.database.controllers;

import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.PersistedBackendTest;
import nl.tudelft.ewi.devhub.server.database.embeddables.TimeSpan;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import java.util.Date;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class CoursesTest extends PersistedBackendTest {


	@Inject @Getter private Groups groups;
	@Inject @Getter private CourseEditions courses;
	@Inject @Getter private Users users;
	
	@Test(expected=ConstraintViolationException.class)
	public void testCourseShouldHaveCode() {
		CourseEdition courseEdition = new CourseEdition();
		courseEdition.setCourse(createCourse());
		courseEdition.setTimeSpan(new TimeSpan(new Date(), null));
		courseEdition.setMinGroupSize(2);
		courseEdition.setMaxGroupSize(2);
		courses.persist(courseEdition);
	}

	@Test(expected=PersistenceException.class)
	public void testCourseShouldHaveStart() {
		CourseEdition courseEdition = new CourseEdition();
		courseEdition.setCode(randomString().substring(0,4));
		courseEdition.setCourse(createCourse());
		courseEdition.setTimeSpan(new TimeSpan(null, null));
		courseEdition.setMinGroupSize(2);
		courseEdition.setMaxGroupSize(2);
		courses.persist(courseEdition);
	}

	@Test(expected=ConstraintViolationException.class)
	public void testCourseShouldHaveMinSize() {
		CourseEdition courseEdition = new CourseEdition();
		courseEdition.setCode(randomString().substring(0, 4));
		courseEdition.setCourse(createCourse());
		courseEdition.setTimeSpan(new TimeSpan(new Date(), null));
		courseEdition.setMaxGroupSize(2);
		courses.persist(courseEdition);
	}

	@Test(expected=ConstraintViolationException.class)
	public void testCourseShouldHaveMaxSize() {
		CourseEdition courseEdition = new CourseEdition();
		courseEdition.setCode(randomString().substring(0,4));
		courseEdition.setCourse(createCourse());
		courseEdition.setTimeSpan(new TimeSpan(new Date(), null));
		courseEdition.setMinGroupSize(2);
		courses.persist(courseEdition);
	}
	
	@Test
	public void testCreateCourse() {
		createCourseEdition();
	}
	
	@Test
	public void testFindCourseById() {
		CourseEdition course = createCourseEdition();
		assertEquals(course, courses.find(course.getId()));
	}
	
	@Test
	public void testFindCourseByCode() {
		CourseEdition course = createCourseEdition();
		assertEquals(course, courses.find(course.getCourse().getCode(), course.getCode()));
	}

	@Test(expected=PersistenceException.class)
	public void testInsertSameCodeTwice() {
		CourseEdition course = createCourseEdition();
		courses.persist(course);

		CourseEdition other = createCourseEdition();
		other.setCode(course.getCode());
		courses.persist(other);
	}

	@Test(expected=PersistenceException.class)
	public void testCourseCodeCaseInsensitive() {
		CourseEdition course = createCourseEdition();
		course.setCode(course.getCode().toLowerCase());
		courses.persist(course);

		CourseEdition other = createCourseEdition();
		other.setCode(course.getCode().toUpperCase());
		courses.persist(other);
	}

	@Test
	public void testListActiveCourses() {
		CourseEdition course = createCourseEdition();
		assertThat(courses.listActiveCourses(), hasItem(course));
	}

}
