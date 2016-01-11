package nl.tudelft.ewi.devhub.server.backend;

import nl.tudelft.ewi.devhub.server.database.embeddables.TimeSpan;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.mockito.Mockito;

import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

public class BackendTest {

	protected static final Random random = new Random();

	protected Course createCourse() {
		Course course = new Course();
		course.setCode(randomString().substring(0, 4));
		course.setName(randomString());
		return course;
	}

	protected CourseEdition createCourseEdition() {
		CourseEdition courseEdition = new CourseEdition();
		courseEdition.setCourse(createCourse());
		courseEdition.setCode(randomString().substring(0, 4));
		courseEdition.setTimeSpan(new TimeSpan(new Date(), null));
		courseEdition.setMinGroupSize(2);
		courseEdition.setMaxGroupSize(2);
		return courseEdition;
	}
	
	protected User createUser() {
		User user = new User();
		user.setGroups(Lists.newArrayList());
		user.setAssists(Sets.newHashSet());
		user.setNetId(randomString());
		return user;
	}

	protected String randomString() {
		return new BigInteger(130, random).toString(32);
	}

}