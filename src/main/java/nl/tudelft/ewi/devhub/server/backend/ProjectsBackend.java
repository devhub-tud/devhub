package nl.tudelft.ewi.devhub.server.backend;

import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.GroupMemberships;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupMembership;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;
import nl.tudelft.ewi.git.models.UserModel;

import org.hibernate.exception.ConstraintViolationException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

@Slf4j
public class ProjectsBackend {

	private static final int USER_ID = 1;

	private static final String ALREADY_REGISTERED_FOR_COURSE = "error.already-registered-for-course";
	private static final String COULD_NOT_FIND_COURSE = "error.could-not-find-course";
	private static final String COULD_NOT_CREATE_GROUP = "error.could-not-create-group";

	private final GitServerClient client;
	private final GroupMemberships groupMemberships;
	private final Groups groups;
	private final Users users;

	private final Object groupNumberLock = new Object();

	@Inject
	ProjectsBackend(GroupMemberships groupMemberships, Groups groups, Users users, GitServerClient client) {
		this.groupMemberships = groupMemberships;
		this.groups = groups;
		this.users = users;
		this.client = client;
	}

	@Transactional
	public void processNewProjectSetup(Course course) throws ApiError {
		User requester = users.find(USER_ID);
		if (course == null) {
			throw new ApiError(COULD_NOT_FIND_COURSE);
		}

		synchronized (groupNumberLock) {

			List<Group> courseGroups = groups.find(course);
			Set<Long> groupNumbers = getGroupNumbers(courseGroups);

			// Ensure that requester has no other projects for same course.
			if (isAlreadyRegisteredForCourse(requester, courseGroups)) {
				throw new ApiError(ALREADY_REGISTERED_FOR_COURSE);
			}

			// Select first free group number.
			long newGroupNumber = 1;
			while (groupNumbers.contains(newGroupNumber)) {
				newGroupNumber++;
			}

			Group group = new Group();
			group.setCourse(course);
			group.setGroupNumber(newGroupNumber);
			group.setRepositoryName("courses/" + course.getCode().toLowerCase() + "/group-" + group.getGroupNumber());

			boolean worked = false;
			for (int attempt = 1; attempt <= 3 && !worked; attempt++) {
				try {
					groups.persist(group);
					worked = true;
				}
				catch (ConstraintViolationException e) {
					log.warn("Could not persist group: {}", group);
					group.setGroupNumber(group.getGroupNumber() + 1);
					group.setRepositoryName("courses/" + course.getCode().toLowerCase() + "/group-" + group.getGroupNumber());
				}
			}

			if (!worked) {
				throw new ApiError(COULD_NOT_CREATE_GROUP);
			}

			GroupMembership membership = new GroupMembership();
			membership.setGroup(group);
			membership.setUser(requester);
			groupMemberships.persist(membership);

			UserModel userModel = new UserModel();
			userModel.setName(requester.getNetId());
			client.users().ensureExists(userModel);

			CreateRepositoryModel repoModel = new CreateRepositoryModel();
			repoModel.setName(group.getRepositoryName());
			repoModel.setTemplateRepository(course.getTemplateRepositoryUrl());
			repoModel.setPermissions(ImmutableMap.<String, Level> builder()
					.put(userModel.getName(), Level.READ_WRITE)
					.build());

			client.repositories().create(repoModel);
		}
	}

	private boolean isAlreadyRegisteredForCourse(User requester, List<Group> courseGroups) {
		for (Group group : courseGroups) {
			Set<GroupMembership> memberships = group.getMemberships();
			for (GroupMembership membership : memberships) {
				if (membership.getUser().getId() == requester.getId()) {
					return true;
				}
			}
		}
		return false;
	}

	private Set<Long> getGroupNumbers(List<Group> groups) {
		Set<Long> groupNumbers = Sets.newHashSet();
		for (Group group : groups) {
			groupNumbers.add(group.getGroupNumber());
		}
		return groupNumbers;
	}

}
