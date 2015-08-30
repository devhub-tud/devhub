package nl.tudelft.ewi.devhub.server.backend;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

import org.hibernate.exception.ConstraintViolationException;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.ws.rs.NotFoundException;
import java.util.Collection;

@Slf4j
@Singleton
public class ProjectsBackend {

	private static final String ALREADY_REGISTERED_FOR_COURSE = "error.already-registered-for-course";
	private static final String COULD_NOT_CREATE_GROUP = "error.could-not-create-group";
	private static final String GIT_SERVER_UNAVAILABLE = "error.git-server-unavailable";

	private final Provider<Groups> groupsProvider;
	private final GitServerClient client;

	@Inject
	ProjectsBackend(Provider<Groups> groupsProvider, GitServerClient client) {
		this.groupsProvider = groupsProvider;
		this.client = client;
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

			Repositories repositories = client.repositories();
			Repository repo = repositories.retrieve(repositoryName);
			repo.delete();
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

	private void provisionRepository(CourseEdition courseEdition, String repoName, String templateUrl, Collection<User> members) throws GitClientException {
		log.info("Provisioning new Git repository: {}", repoName);
		nl.tudelft.ewi.git.client.Users gitUsers = client.users();

		Builder<String, Level> permissions = ImmutableMap.<String, Level> builder();
		for (User member : members) {
			gitUsers.ensureExists(member.getNetId());
			permissions.put(member.getNetId(), Level.READ_WRITE);
		}

		String groupName = gitoliteAssistantGroupName(courseEdition);
		try {
			client.groups().retrieve(groupName);
			permissions.put(groupName, Level.ADMIN);
		}
		catch (NotFoundException e) {
			log.warn("Expected group {} to be available on the git server!", groupName);
		}

		CreateRepositoryModel repoModel = new CreateRepositoryModel();
		repoModel.setName(repoName);
		repoModel.setTemplateRepository(templateUrl);
		repoModel.setPermissions(permissions.build());

		Repositories repositories = client.repositories();
		repositories.create(repoModel);
		log.info("Finished provisioning Git repository: {}", repoName);
	}

	private String gitoliteAssistantGroupName(CourseEdition courseEdition) {
		return String.format("@%s-%s",
			courseEdition.getCourse().getCode(),
			courseEdition.getCode()).toLowerCase();
	}

}
