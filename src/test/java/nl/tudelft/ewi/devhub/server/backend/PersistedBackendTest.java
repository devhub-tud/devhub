package nl.tudelft.ewi.devhub.server.backend;

import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import com.google.common.collect.Sets;

import java.util.Arrays;

/**
 * Created by Jan-Willem on 8/27/2015.
 */
public abstract class PersistedBackendTest extends BackendTest {

	protected abstract Courses getCourses();

	protected abstract Users getUsers();

	protected abstract Groups getGroups();

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

	protected Group createGroup(CourseEdition courseEdition, User... members) {
		Group group = new Group();
		group.setCourseEdition(courseEdition);
		group.setMembers(Sets.newHashSet(Arrays.asList(members)));
		getGroups().persist(group);

		GroupRepository groupRepository = new GroupRepository();
		groupRepository.setRepositoryName(courseEdition.createRepositoryName(group).toASCIIString());
		group.setRepository(groupRepository);
		return getGroups().merge(group);
	}

}
