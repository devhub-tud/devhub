package nl.tudelft.ewi.devhub.server.web.resources.repository;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.NotificationBackend;
import nl.tudelft.ewi.devhub.server.backend.PullRequestBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.backend.mail.PullRequestMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.IssueComments;
import nl.tudelft.ewi.devhub.server.database.controllers.PrivateRepositories;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.entities.PrivateRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.util.MarkDownParser;
import nl.tudelft.ewi.devhub.server.web.resources.HooksResource;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RequestScoped
@Path("projects/{netId}/{repoTitle}")
public class PrivatePullResource extends AbstractProjectPullResource {

	private final PrivateRepositories privateRepositories;
	@Context @Getter private UriInfo uriInfo;

	@Inject
	public PrivatePullResource(TemplateEngine templateEngine, @Named("current.user") User currentUser, CommentBackend commentBackend,
							   BuildResults buildResults, PullRequests pullRequests, PullRequestBackend pullRequestBackend,
							   RepositoriesApi repositoriesApi, CommentMailer commentMailer, PullRequestMailer pullRequestMailer,
							   IssueComments issueComments, HooksResource hooksResource, Warnings warnings,
							   PrivateRepositories privateRepositories, MarkDownParser markDownParser, Users users,
							   NotificationBackend notificationBackend) {
		super(templateEngine, currentUser, commentBackend, buildResults, pullRequests, pullRequestBackend,
			repositoriesApi, commentMailer, pullRequestMailer, issueComments, hooksResource, warnings, markDownParser, users, notificationBackend);
		this.privateRepositories = privateRepositories;
	}

	@Override
	protected PrivateRepository getRepositoryEntity() {
		MultivaluedMap<String, String> params = getUriInfo().getPathParameters();
		String netId = params.getFirst("netId");
		String repoTitle = params.getFirst("repoTitle");
		PrivateRepository privateRepository = privateRepositories.find(netId, repoTitle);
		if(!(privateRepository.getOwner().equals(currentUser) ||
			privateRepository.getCollaborators().contains(currentUser))) {
			throw new NotAuthorizedException("Not authorized");
		}
		return privateRepository;
	}

}
