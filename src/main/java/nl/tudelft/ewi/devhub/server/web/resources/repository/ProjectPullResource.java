package nl.tudelft.ewi.devhub.server.web.resources.repository;

import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.PullRequestBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.backend.mail.PullRequestMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequestComments;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.resources.HooksResource;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.client.GitServerClient;

import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;

import javax.inject.Inject;
import javax.ws.rs.Path;
import java.util.Map;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RequestScoped
@Path("courses/{courseCode}/{editionCode}/groups/{groupNumber}")
public class ProjectPullResource extends AbstractProjectPullResource {

    private final Group group;

	@Inject
	public ProjectPullResource(TemplateEngine templateEngine, @Named("current.user") User currentUser,
				final @Named("current.group") Group group,
			   CommentBackend commentBackend, BuildResults buildResults, PullRequests pullRequests,
			   PullRequestBackend pullRequestBackend, GitServerClient gitClient,
			   CommentMailer commentMailer, PullRequestMailer pullRequestMailer,
			   PullRequestComments pullRequestComments, HooksResource hooksResource, Warnings warnings) {
		super(templateEngine, currentUser, commentBackend, buildResults, pullRequests, pullRequestBackend,
			gitClient, commentMailer, pullRequestMailer, pullRequestComments, hooksResource, warnings);
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
		params.put("course", group.getCourseEdition());
		return params;
	}

}
