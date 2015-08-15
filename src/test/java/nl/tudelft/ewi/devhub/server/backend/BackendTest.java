package nl.tudelft.ewi.devhub.server.backend;

import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

import com.google.common.collect.Lists;

import com.google.common.collect.Sets;
import nl.tudelft.ewi.devhub.server.database.embeddables.TimeSpan;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.User;

public class BackendTest {

	private static final Random random = new Random();

	protected CourseEdition createCourse() {
		CourseEdition courseEdition = new CourseEdition();
		Course course = new Course();
		course.setCode(randomString().substring(0, 4));
		course.setName(randomString());
		courseEdition.setCourse(course);
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