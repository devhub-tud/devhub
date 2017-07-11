package nl.tudelft.ewi.devhub.server.web.resources.repository;

import java.util.Map;

import com.google.common.collect.Maps;

import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.NotificationBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.AbstractIssue;
import nl.tudelft.ewi.devhub.server.web.resources.Resource;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

public abstract class AbstractIssueResource<IssueType extends AbstractIssue> extends Resource {

	protected final TemplateEngine templateEngine;
	protected final User currentUser;
	protected final CommentBackend commentBackend;
	protected final CommentMailer commentMailer;
	protected final RepositoriesApi repositoriesApi;
	protected final Users users;
	protected final NotificationBackend notificationBackend;
	
	
	public AbstractIssueResource(final TemplateEngine templateEngine,
								 final User currentUser,
								 final CommentBackend commentBackend,
								 final CommentMailer commentMailer,
								 final RepositoriesApi repositoriesApi,
								 final Users users,
								 final NotificationBackend notificationBackend) {
		
		super();
		this.templateEngine = templateEngine;
		this.currentUser = currentUser;
		this.commentBackend = commentBackend;
		this.commentMailer = commentMailer;
		this.repositoriesApi = repositoriesApi;
		this.users = users;
		this.notificationBackend = notificationBackend;
	}

	protected abstract RepositoryEntity getRepositoryEntity();

	protected RepositoryApi getRepositoryApi(RepositoryEntity repositoryEntity) {
		return repositoriesApi.getRepository(repositoryEntity.getRepositoryName());
	}

	protected Map<String, Object> getBaseParameters() {
		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("repositoryEntity", getRepositoryEntity());
		return parameters;
	}
	
	

}
