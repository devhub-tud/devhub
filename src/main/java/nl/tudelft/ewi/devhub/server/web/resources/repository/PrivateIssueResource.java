package nl.tudelft.ewi.devhub.server.web.resources.repository;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;

import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.IssueBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.Issues;
import nl.tudelft.ewi.devhub.server.database.controllers.PrivateRepositories;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.PrivateRepository;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

@RequestScoped
@Path("projects/{netId}/{repoTitle}")
public class PrivateIssueResource extends AbstractProjectIssueResource {

	private final PrivateRepositories privateRepositories;
	@Context @Getter private UriInfo uriInfo;
	
	@Inject
	public PrivateIssueResource(final TemplateEngine templateEngine, 
			@Named("current.user") final User currentUser, 
			final CommentBackend commentBackend,
			final CommentMailer commentMailer, 
			final RepositoriesApi repositoriesApi, 
			final Issues issues, 
			final IssueBackend issueBackend,
			final Users users,
			final PrivateRepositories privateRepositories) {
		
		super(templateEngine, currentUser, commentBackend, commentMailer, repositoriesApi, issues, issueBackend, users);
		
		this.privateRepositories = privateRepositories;
	}

	@Override
	protected RepositoryEntity getRepositoryEntity() {
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
