package nl.tudelft.ewi.devhub.server.web.resources.repository;

import java.util.Map;

import javax.ws.rs.Path;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;

import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.IssueBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.IssueComments;
import nl.tudelft.ewi.devhub.server.database.controllers.Issues;
import nl.tudelft.ewi.devhub.server.database.controllers.RepositoriesController;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

@RequestScoped
@Path("courses/{courseCode}/{editionCode}/groups/{groupNumber}")
public class ProjectIssueResource extends AbstractProjectIssueResource {

	private final Group group;
	
	@Inject
	public ProjectIssueResource(
			final TemplateEngine templateEngine, 
			@Named("current.user") final User currentUser,
			@Named("current.group") final Group group,
			final CommentBackend commentBackend,
			final CommentMailer commentMailer, 
			final RepositoriesController repositoriesController,
			final RepositoriesApi repositoriesApi, 
			final Issues issues, 
			final IssueBackend issueBackend,
			final Users users,
			final IssueComments issueComments ) {
		super(templateEngine, currentUser, commentBackend, commentMailer, repositoriesApi, repositoriesController, issues, issueBackend, users, issueComments);

		this.group = group;
		
	}

	@Override
	protected RepositoryEntity getRepositoryEntity() {
		return group.getRepository();
	}
	
	@Override
	protected Map<String, Object> getBaseParameters() {
		Map<String, Object> params = super.getBaseParameters();
		params.put("group", group);
		params.put("courseEdition", group.getCourseEdition());
		return params;
	}

}
