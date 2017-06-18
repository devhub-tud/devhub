package nl.tudelft.ewi.devhub.server.web.resources.repository;

import com.google.common.eventbus.AsyncEventBus;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.RepositoriesController;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.util.MarkDownParser;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.resources.Resource;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;

import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequestScoped
@Path("courses/{courseCode}/{editionCode}/groups/{groupNumber}")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class ProjectResource extends AbstractProjectResource<GroupRepository> {

	private final static int MIN_GROUP_SIZE = 1;

	private final Group group;
	private final Groups groups;

	@Inject
	public ProjectResource(TemplateEngine templateEngine, @Named("current.user") User currentUser,
						   final @Named("current.group") Group group, CommentBackend commentBackend,
						   BuildResults buildResults, PullRequests pullRequests, RepositoriesApi repositoriesApi, BuildsBackend buildBackend,
						   CommitComments comments, CommentMailer commentMailer, Commits commits, Warnings warnings,
						   RepositoriesController repositoriesController, EditContributorsState editContributorsState,
						   Users users, Groups groups, MarkDownParser markDownParser, final AsyncEventBus asyncEventBus) {
		super(templateEngine, currentUser, commentBackend, buildResults, pullRequests, repositoriesApi, buildBackend,
			comments, commentMailer, commits, warnings, repositoriesController, editContributorsState, users, markDownParser, asyncEventBus);
		this.group = group;
		this.groups = groups;
	}

	@Override
	protected GroupRepository getRepositoryEntity() {
		return group.getRepository();
	}

	@Override
	protected Map<String, Object> getBaseParameters() {
		Map<String, Object> params = super.getBaseParameters();
		params.put("group", group);
		params.put("courseEdition", group.getCourseEdition());
		return params;
	}

	@Override
	protected void editContributorsAllowedCheck() {
		CourseEdition course = group.getCourseEdition();
		if (!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
			throw new ForbiddenException(
				String.format("User %s is not allowed to update groups in %s", currentUser, course)
			);
		}
	}

	@Override
	protected void validateCollaborators(Collection<User> groupMembers) throws ApiError {
		CourseEdition course = group.getCourseEdition();
		if (groupMembers.size() < getMinGroupSize() || groupMembers.size() > getMaxGroupSize()) {
			throw new ApiError("error.invalid-group-size");
		}

		for (User user : groupMembers) {
			if (!group.getMembers().contains(user) && user.isParticipatingInCourse(course)) {
				throw new ApiError("error.already-registered-for-course");
			}
		}
	}

	@Override
	protected void updateCollaborators(Collection<User> members) {
		group.setMembers(members.stream().map(User::getId).map(users::find).collect(Collectors.toSet()));
		groups.merge(group);

		RepositoryApi repositoryApi = repositoriesApi.getRepository(getRepositoryEntity().getRepositoryName());

		RepositoryModel repositoryModel = repositoryApi.getRepositoryModel();
		Map<String, RepositoryModel.Level> permissions = Maps.newHashMap(repositoryModel.getPermissions());
		permissions.entrySet().removeIf(entry -> entry.getValue().equals(RepositoryModel.Level.READ_WRITE));
		permissions.putAll(
			members.stream().collect(Collectors.toMap(User::getNetId, a -> RepositoryModel.Level.READ_WRITE))
		);
		repositoryModel.setPermissions(permissions);
		repositoryApi.updateRepository(repositoryModel);
	}

	@Override
	protected int getMaxGroupSize() {
		return group.getCourseEdition().getMaxGroupSize();
	}

	@Override
	protected int getMinGroupSize() {
		CourseEdition course = group.getCourseEdition();
		if (currentUser.isAdmin() || currentUser.isAssisting(course)) {
			return MIN_GROUP_SIZE;
		}
		return course.getMinGroupSize();
	}

	@Override
	public void deleteRepository() {
		if (!(currentUser.isAdmin() || currentUser.isAssisting(group.getCourseEdition()))) {
			throw new ForbiddenException(
				String.format(
					"User %s is not allowed to remove repository %s that is managed by the course %s",
					currentUser.getNetId(),
					getRepositoryEntity().getRepositoryName(),
					group.getCourseEdition().getName()
				)
			);
		}

		super.deleteRepository();
	}

}
