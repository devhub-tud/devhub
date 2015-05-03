package nl.tudelft.ewi.devhub.server.backend;

import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

import com.google.common.collect.Lists;

import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.GroupMembership;
import nl.tudelft.ewi.devhub.server.database.entities.User;

public class BackendTest {

	private static final Random random = new Random();

	protected Course createCourse() {
		Course course = new Course();
		course.setCode(randomString().substring(0,4));
		course.setName(randomString());
		course.setStart(new Date());
		course.setMinGroupSize(2);
		course.setMaxGroupSize(2);
		return course;
	}
	
	protected User createUser() {
		User user = new User();
		user.setMemberOf(Lists.<GroupMembership> newArrayList());
		user.setNetId(randomString());
		return user;
	}

	protected String randomString() {
		return new BigInteger(130, random).toString(32);
	}

}