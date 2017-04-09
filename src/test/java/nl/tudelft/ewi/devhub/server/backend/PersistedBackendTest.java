package nl.tudelft.ewi.devhub.server.backend;

import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
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

	protected abstract CourseEditions getCourses();

	protected abstract Users getUsers();

	protected abstract Groups getGroups();

	@Override
	protected CourseEdition createCourseEdition() {
		CourseEdition courseEdition = super.createCourseEdition();
		CourseEditions courses = getCourses();
		courses.persist(courseEdition);
		courses.refresh(courseEdition);
		return courseEdition;
	}

	@Override
	protected User createUser() {
		User user = super.createUser();
		Users users = getUsers();
		users.persist(user);
		users.refresh(user);
		return user;
	}

	protected Group createGroup(CourseEdition courseEdition, User... members) {
		Groups groups = getGroups();
		Group group = new Group();
		group.setCourseEdition(courseEdition);
		group.setMembers(Sets.newHashSet(Arrays.asList(members)));
		groups.persist(group);

		GroupRepository groupRepository = new GroupRepository();
		groupRepository.setRepositoryName(courseEdition.createRepositoryName(group).toASCIIString());
		group.setRepository(groupRepository);
		groupRepository.setGroup(group);
		groups.merge(group);

		groups.refresh(group);
		return group;
	}

}
