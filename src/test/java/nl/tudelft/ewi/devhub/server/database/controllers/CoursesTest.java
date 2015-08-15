package nl.tudelft.ewi.devhub.server.database.controllers;

import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import nl.tudelft.ewi.devhub.server.database.embeddables.TimeSpan;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class CoursesTest {
	
	@Inject
	private Random random;
	
	@Inject
	private Courses courses;
	
//	@Test(expected=ConstraintViolationException.class)
//	public void testCourseShouldHaveCode() {
//		CourseEdition course = new CourseEdition();
//		course.setName(randomString());
//		course.setStart(new Date());
//		course.setMinGroupSize(2);
//		course.setMaxGroupSize(2);
//		courses.persist(course);
//	}
//
//	@Test(expected=ConstraintViolationException.class)
//	public void testCourseShouldHaveName() {
//		CourseEdition course = new CourseEdition();
//		course.setCode(randomString().substring(0,4));
//		course.setStart(new Date());
//		course.setMinGroupSize(2);
//		course.setMaxGroupSize(2);
//		courses.persist(course);
//	}
//
//	@Test(expected=ConstraintViolationException.class)
//	public void testCourseShouldHaveStart() {
//		CourseEdition course = new CourseEdition();
//		course.setCode(randomString().substring(0,4));
//		course.setName(randomString());
//		course.setMinGroupSize(2);
//		course.setMaxGroupSize(2);
//		courses.persist(course);
//	}
//
//	@Test(expected=ConstraintViolationException.class)
//	public void testCourseShouldHaveMinSize() {
//		CourseEdition course = new CourseEdition();
//		course.setCode(randomString().substring(0,4));
//		course.setName(randomString());
//		course.setStart(new Date());
//		course.setMaxGroupSize(2);
//		courses.persist(course);
//	}
//
//	@Test(expected=ConstraintViolationException.class)
//	public void testCourseShouldHaveMaxSize() {
//		CourseEdition course = new CourseEdition();
//		course.setCode(randomString().substring(0,4));
//		course.setName(randomString());
//		course.setStart(new Date());
//		course.setMinGroupSize(2);
//		courses.persist(course);
//	}
	
	@Test
	public void testCreateCourse() {
		CourseEdition course = createCourse();
		courses.persist(course);
	}
	
	@Test
	public void testFindCourseById() {
		CourseEdition course = createCourse();
		courses.persist(course);
		assertEquals(course, courses.find(course.getId()));
	}
	
	@Test
	public void testFindCourseByCode() {
		CourseEdition course = createCourse();
		courses.persist(course);
		assertEquals(course, courses.find(course.getCode()));
	}

//	@Test(expected=PersistenceException.class)
//	public void testInsertSameCodeTwice() {
//		CourseEdition course = createCourse();
//		courses.persist(course);
//
//		CourseEdition other = createCourse();
//		other.setCode(course.getCode());
//		courses.persist(other);
//	}
//
//	@Test(expected=PersistenceException.class)
//	public void testCourseCodeCaseInsensitive() {
//		CourseEdition course = createCourse();
//		course.setCode(course.getCode().toLowerCase());
//		courses.persist(course);
//
//		CourseEdition other = createCourse();
//		other.setCode(course.getCode().toUpperCase());
//		courses.persist(other);
//	}

	@Test
	public void testListActiveCourses() {
		CourseEdition course = createCourse();
		courses.persist(course);
		assertThat(courses.listActiveCourses(), hasItem(course));
	}
	
	protected CourseEdition createCourse() {
		CourseEdition courseEdition = new CourseEdition();
		Course course = new Course();
		course.setCode(randomString().substring(0, 4));
		course.setName(randomString());
		courseEdition.setCourse(course);
		courseEdition.setTimeSpan(new TimeSpan(new Date(), null));
		courseEdition.setMinGroupSize(2);
		courseEdition.setMaxGroupSize(2);
		courseEdition.setBuildTimeout(600);
		return courseEdition;
	}
	
	protected String randomString() {
		return new BigInteger(130, random).toString(32);
	}

}
