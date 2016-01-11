package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

import nl.tudelft.ewi.git.web.api.GroupsApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import org.hibernate.exception.ConstraintViolationException;

import javax.persistence.PersistenceException;
import javax.ws.rs.NotFoundException;
import java.util.Collection;

@Slf4j
@Singleton
public class ProjectsBackend {

	private static final String COULD_NOT_CREATE_GROUP = "error.could-not-create-group";
	private static final String GIT_SERVER_UNAVAILABLE = "error.git-server-unavailable";

	private final Provider<Groups> groupsProvider;
	private final RepositoriesApi repositoriesApi;
	private final GroupsApi groupsApi;

	@Inject
	ProjectsBackend(Provider<Groups> groupsProvider, RepositoriesApi repositoriesApi,
	                GroupsApi groupsApi) {
		this.groupsProvider = groupsProvider;
		this.repositoriesApi = repositoriesApi;
		this.groupsApi = groupsApi;
	}

	public Group setupProject(CourseEdition course, Collection<User> members) throws ApiError {
		Preconditions.checkNotNull(course);
		Preconditions.checkNotNull(members);
		
		log.info("Setting up new project for course: {} and members: {}", course, members);

		try {
			Group group = persistRepository(course, members);
			provisionRepository(group, members);
			return group;
		}
		catch (ConstraintViolationException | PersistenceException e) {
			throw new ApiError(COULD_NOT_CREATE_GROUP, e);
		}
	}

	private void deleteRepositoryFromGit(Group group) {
		try {
			String repositoryName = group.getRepository().getRepositoryName();
			log.info("Deleting repository from Git server: {}", repositoryName);
			repositoriesApi.getRepository(repositoryName).deleteRepository();
		}
		catch (Throwable e) {
			log.warn(e.getMessage());
		}
	}

	@Transactional
	protected void deleteGroupFromDatabase(Group group) {
		log.info("Deleting group from database: {}", group);
		groupsProvider.get().delete(group);
	}

	@Transactional
	protected Group persistRepository(CourseEdition courseEdition, Collection<User> members) throws ApiError {
		Groups groups = groupsProvider.get();

		Group group = new Group();
		group.setCourseEdition(courseEdition);
		group.setMembers(Sets.newHashSet(members));

		groups.persist(group);
		log.info("Created new group in database: {}", group);

		GroupRepository groupRepository = new GroupRepository();
		groupRepository.setRepositoryName(courseEdition.createRepositoryName(group).toASCIIString());
		group.setRepository(groupRepository);
		return groups.merge(group);
	}
	
	public void  provisionRepository(Group group, Collection<User> members) throws ApiError {
		String repositoryName = group.getRepository().getRepositoryName();
		String templateRepositoryUrl = group.getCourse()
			.getTemplateRepositoryUrl();
		
		try {
			provisionRepository(group.getCourseEdition(), repositoryName, templateRepositoryUrl, members);
		}
		catch (Throwable e) {
			log.error(e.getMessage(), e);
			deleteGroupFromDatabase(group);
			deleteRepositoryFromGit(group);
			throw new ApiError(GIT_SERVER_UNAVAILABLE, e);
		}
	}

	private void provisionRepository(CourseEdition courseEdition, String repoName, String templateUrl, Collection<User> members) {
		log.info("Provisioning new Git repository: {}", repoName);

		Builder<String, Level> permissions = ImmutableMap.<String, Level> builder();

		for (User member : members) {
			permissions.put(member.getNetId(), Level.READ_WRITE);
		}

		String groupName = gitoliteAssistantGroupName(courseEdition);
		try {
			groupsApi.getGroup(groupName).getGroup();
			permissions.put(groupName, Level.ADMIN);
		}
		catch (NotFoundException e) {
			log.warn("Expected group {} to be available on the git server!", groupName);
		}

		CreateRepositoryModel repoModel = new CreateRepositoryModel();
		repoModel.setName(repoName);
		repoModel.setTemplateRepository(templateUrl);
		repoModel.setPermissions(permissions.build());

		repositoriesApi.createRepository(repoModel);
		log.info("Finished provisioning Git repository: {}", repoName);
	}

	@VisibleForTesting
	String gitoliteAssistantGroupName(CourseEdition courseEdition) {
		return String.format("@%s-%s",
			courseEdition.getCourse().getCode(),
			courseEdition.getCode()).toLowerCase();
	}

}
