package nl.tudelft.ewi.devhub.server.backend;

import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.User;

/**
 * Created by Jan-Willem on 8/27/2015.
 */
public abstract class PersistedBackendTest extends BackendTest {

	protected abstract Courses getCourses();

	protected abstract Users getUsers();

	@Override
	protected CourseEdition createCourse() {
		CourseEdition courseEdition = super.createCourse();
		return getCourses().persist(courseEdition);
	}

	@Override
	protected User createUser() {
		User user = super.createUser();
		return getUsers().persist(user);
	}

}
