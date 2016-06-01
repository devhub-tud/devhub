package nl.tudelft.ewi.devhub.server.web.resources.repository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.IssueBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.Issues;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.devhub.server.web.resources.Resource;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

/**
 * 
 * @author Aron Zwaan
 *
 */
@Slf4j
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public abstract class AbstractProjectIssueResource extends AbstractIssueResource<Issue> {

	protected Issues issues;
	protected IssueBackend issueBackend;	
	
	public AbstractProjectIssueResource( final TemplateEngine templateEngine, 
			final User currentUser, 
			final CommentBackend commentBackend,
			final CommentMailer commentMailer, 
			final RepositoriesApi repositoriesApi, 
			final Issues issues, 
			final IssueBackend issueBackend) {
		
		super(templateEngine, currentUser, commentBackend, commentMailer, repositoriesApi);
		
		this.issues = issues;
		this.issueBackend = issueBackend;
	}
	
	@GET
	@Transactional
	@Path("/issues")
	public Response getIssues(@Context HttpServletRequest request) throws IOException{
		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);
		RepositoryModel repository = repositoryApi.getRepositoryModel();

		List<Issue> openIssues = issues.findOpenIssues(repositoryEntity);
		List<Issue> closedIssues = issues.findClosedIssues(repositoryEntity);

		Map<String, Object> parameters = getBaseParameters();
		
		parameters.put("repository", repository);
		parameters.put("openPullRequests", openIssues);
		parameters.put("closedPullRequests", closedIssues);

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("courses/assignments/group-issues.ftl", locales, parameters));
	
	}
	
	
}
