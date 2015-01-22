package nl.tudelft.ewi.devhub.server.backend;

import java.util.Collection;
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
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;

import org.hibernate.exception.ConstraintViolationException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Slf4j
@Singleton
public class ProjectsBackend {

	private static final String ALREADY_REGISTERED_FOR_COURSE = "error.already-registered-for-course";
	private static final String COULD_NOT_CREATE_GROUP = "error.could-not-create-group";
	private static final String GIT_SERVER_UNAVAILABLE = "error.git-server-unavailable";

	private final Provider<GroupMemberships> groupMembershipsProvider;
	private final Provider<Groups> groupsProvider;
	private final GitServerClient client;

	private final Object groupNumberLock = new Object();

	@Inject
	ProjectsBackend(Provider<GroupMemberships> groupMembershipsProvider, Provider<Groups> groupsProvider,
			Provider<Users> usersProvider, GitServerClient client) {

		this.groupMembershipsProvider = groupMembershipsProvider;
		this.groupsProvider = groupsProvider;
		this.client = client;
	}

	public void setupProject(Course course, Collection<User> members) throws ApiError {
		Preconditions.checkNotNull(course);
		Preconditions.checkNotNull(members);
		
		log.info("Setting up new project for course: {} and members: {}", course, members);
		Group group = persistRepository(course, members);

		String repositoryName = group.getRepositoryName();
		String templateRepositoryUrl = group.getCourse()
			.getTemplateRepositoryUrl();

		try {
			provisionRepository(course.getCode(), repositoryName, templateRepositoryUrl, members);
		}
		catch (Throwable e) {
			log.error(e.getMessage(), e);
			deleteGroupFromDatabase(group);
			deleteRepositoryFromGit(group);
			throw new ApiError(GIT_SERVER_UNAVAILABLE);
		}
	}

	private void deleteRepositoryFromGit(Group group) {
		try {
			String repositoryName = group.getRepositoryName();
			log.info("Deleting repository from Git server: {}", repositoryName);

			Repositories repositories = client.repositories();
			DetailedRepositoryModel repo = repositories.retrieve(repositoryName);
			repositories.delete(repo);
		}
		catch (Throwable e) {
			log.warn(e.getMessage());
		}
	}

	@Transactional
	protected void deleteGroupFromDatabase(Group group) {
		log.info("Deleting group from database: {}", group);
		GroupMemberships groupMemberships = groupMembershipsProvider.get();
		Groups groups = groupsProvider.get();

		List<GroupMembership> memberships = groupMemberships.ofGroup(group);
		for (GroupMembership membership : memberships) {
			groupMemberships.delete(membership);
		}
		groups.delete(groups.find(group.getGroupId()));
	}

	@Transactional
	protected Group persistRepository(Course course, Collection<User> members) throws ApiError {
		GroupMemberships groupMemberships = groupMembershipsProvider.get();
		Groups groups = groupsProvider.get();

		synchronized (groupNumberLock) {
			List<Group> courseGroups = groups.find(course);
			Set<Long> groupNumbers = getGroupNumbers(courseGroups);

			// Ensure that requester has no other projects for same course.
			for (User member : members) {
				if (member.isParticipatingInCourse(course)) {
					throw new ApiError(ALREADY_REGISTERED_FOR_COURSE);
				}
			}

			// Select first free group number.
			long newGroupNumber = 1;
			while (groupNumbers.contains(newGroupNumber)) {
				newGroupNumber++;
			}

			Group group = new Group();
			group.setCourse(course);
			group.setGroupNumber(newGroupNumber);
			group.setBuildTimeout(course.getBuildTimeout());
			group.setRepositoryName("courses/" + course.getCode()
				.toLowerCase() + "/group-" + group.getGroupNumber());

			boolean worked = false;
			for (int attempt = 1; attempt <= 3 && !worked; attempt++) {
				try {
					groups.persist(group);
					worked = true;
				}
				catch (ConstraintViolationException e) {
					log.warn("Could not persist group: {}", group);
					group.setGroupNumber(group.getGroupNumber() + 1);
					group.setRepositoryName("courses/" + course.getCode()
						.toLowerCase() + "/group-" + group.getGroupNumber());
				}
			}

			if (!worked) {
				throw new ApiError(COULD_NOT_CREATE_GROUP);
			}

			for (User member : members) {
				GroupMembership membership = new GroupMembership();
				membership.setGroup(group);
				membership.setUser(member);
				groupMemberships.persist(membership);
			}

			log.info("Created new group in database: {}", group);
			return group;
		}
	}

	private void provisionRepository(String courseCode, String repoName, String templateUrl, Collection<User> members) {
		log.info("Provisioning new Git repository: {}", repoName);
		nl.tudelft.ewi.git.client.Users gitUsers = client.users();

		Builder<String, Level> permissions = ImmutableMap.<String, Level> builder();
		for (User member : members) {
			gitUsers.ensureExists(member.getNetId());
			permissions.put(member.getNetId(), Level.READ_WRITE);
		}

		permissions.put("@" + courseCode.toLowerCase(), Level.ADMIN);

		CreateRepositoryModel repoModel = new CreateRepositoryModel();
		repoModel.setName(repoName);
		repoModel.setTemplateRepository(templateUrl);
		repoModel.setPermissions(permissions.build());

		Repositories repositories = client.repositories();
		repositories.create(repoModel);
		log.info("Finished provisioning Git repository: {}", repoName);
	}

	private Set<Long> getGroupNumbers(Collection<Group> groups) {
		Set<Long> groupNumbers = Sets.newTreeSet();
		for (Group group : groups) {
			groupNumbers.add(group.getGroupNumber());
		}
		return groupNumbers;
	}

}
