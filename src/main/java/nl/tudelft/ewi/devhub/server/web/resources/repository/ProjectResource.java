package nl.tudelft.ewi.devhub.server.web.resources.repository;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.RepositoriesController;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.resources.Resource;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Slf4j
@RequestScoped
@Path("courses/{courseCode}/{editionCode}/groups/{groupNumber}")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class ProjectResource extends AbstractProjectResource<GroupRepository> {
	
	private final Group group;

	@Inject
	public ProjectResource(TemplateEngine templateEngine, @Named("current.user") User currentUser,
						   final @Named("current.group") Group group, CommentBackend commentBackend,
						   BuildResults buildResults, PullRequests pullRequests, RepositoriesApi repositoriesApi, BuildsBackend buildBackend,
						   CommitComments comments, CommentMailer commentMailer, Commits commits, Warnings warnings,
						   RepositoriesController repositoriesController) {
		super(templateEngine, currentUser, commentBackend, buildResults, pullRequests, repositoriesApi, buildBackend,
			comments, commentMailer, commits, warnings, repositoriesController);
		this.group = group;
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
