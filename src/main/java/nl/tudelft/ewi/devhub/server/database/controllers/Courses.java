package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.Course;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.List;

import static nl.tudelft.ewi.devhub.server.database.entities.QCourse.course;

/**
 * Created by Jan-Willem on 8/28/2015.
 */
public class Courses extends Controller<Course> {

	@Inject
	public Courses(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public Course find(String courseCode) {
		Preconditions.checkNotNull(courseCode);
		return ensureNotNull(query().from(course)
			.where(course.code.equalsIgnoreCase(courseCode))
			.singleResult(course), "Could not find course with code: " + courseCode);
	}


	@Transactional
	public List<Course> listAll() {
		return query().from(course).list(course);
	}

	@Transactional
	public Course ensureExists(String courseCode, String courseName) {
		Preconditions.checkNotNull(courseCode);
		Preconditions.checkNotNull(courseName);

		try {
			Course course = find(courseCode);
			if(!course.getName().equals(courseName)) {
				course.setName(courseName);
				return merge(course);
			}
			return course;
		}
		catch (EntityNotFoundException e) {
			Course course = new Course();
			course.setCode(courseCode);
			course.setName(courseName);
			return persist(course);
		}
	}
}
