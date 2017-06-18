package nl.tudelft.ewi.devhub.server.web.resources.repository;

import com.google.common.collect.Maps;
import com.google.common.eventbus.AsyncEventBus;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.PrivateRepositories;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.entities.PrivateRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.util.MarkDownParser;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RequestScoped
@Path("projects/{netId}/{repoTitle}")
public class PrivateRepositoryResource extends AbstractProjectResource<PrivateRepository> {

	private final PrivateRepositories privateRepositories;

	@Context @Getter private UriInfo uriInfo;

	@Inject
	public PrivateRepositoryResource(TemplateEngine templateEngine, @Named("current.user") User currentUser,
									 CommentBackend commentBackend, BuildResults buildResults, PullRequests pullRequests,
									 RepositoriesApi repositoriesApi, BuildsBackend buildBackend, CommitComments comments,
									 CommentMailer commentMailer, Commits commits, Warnings warnings,
									 PrivateRepositories privateRepositories, EditContributorsState editContributorsState,
									 Users users, MarkDownParser markDownParser, final AsyncEventBus asyncEventBus) {
		super(templateEngine, currentUser, commentBackend, buildResults, pullRequests, repositoriesApi, buildBackend,
			comments, commentMailer, commits, warnings, privateRepositories, editContributorsState, users, markDownParser, asyncEventBus);
		this.privateRepositories = privateRepositories;
	}

	@Override
	@Transactional
	protected void updateCollaborators(Collection<User> members) {
		PrivateRepository privateRepository = getRepositoryEntity();
		privateRepository.setCollaborators(
			members.stream().map(User::getId).map(users::find).collect(Collectors.toList())
		);
		privateRepositories.merge(privateRepository);

		RepositoryApi repositoryApi = repositoriesApi.getRepository(getRepositoryEntity().getRepositoryName());

		RepositoryModel repositoryModel = repositoryApi.getRepositoryModel();
		Map<String, Level> permissions = Maps.newHashMap(repositoryModel.getPermissions());
		permissions.entrySet().removeIf(entry -> entry.getValue().equals(Level.ADMIN));
		permissions.putAll(
			members.stream().collect(Collectors.toMap(User::getNetId, a -> Level.ADMIN))
		);
		repositoryModel.setPermissions(permissions);
		repositoryApi.updateRepository(repositoryModel);
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
