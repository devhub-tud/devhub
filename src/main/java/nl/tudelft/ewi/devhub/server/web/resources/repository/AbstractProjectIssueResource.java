package nl.tudelft.ewi.devhub.server.web.resources.repository;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.IssueBackend;
import nl.tudelft.ewi.devhub.server.backend.NotificationBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.IssueComments;
import nl.tudelft.ewi.devhub.server.database.controllers.Issues;
import nl.tudelft.ewi.devhub.server.database.controllers.RepositoriesController;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.comments.IssueComment;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.resources.Resource;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 
 * @author Aron Zwaan
 *
 */
@Slf4j
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public abstract class AbstractProjectIssueResource extends AbstractIssueResource<Issue> {

	protected RepositoriesController repositoriesController;
	
	protected Issues issues;
	protected IssueBackend issueBackend;
	protected IssueComments issueComments;
	protected NotificationBackend notificationBackend;
	@Context HttpServletRequest request;
	
	public AbstractProjectIssueResource( final TemplateEngine templateEngine, 
			final User currentUser, 
			final CommentBackend commentBackend,
			final CommentMailer commentMailer, 
			final RepositoriesApi repositoriesApi,
			final RepositoriesController repositoriesController, 
			final Issues issues, 
			final IssueBackend issueBackend,
			final Users users,
			final IssueComments issueComments,
			final NotificationBackend notificationBackend) {
		
		super(templateEngine, currentUser, commentBackend, commentMailer, repositoriesApi, users, notificationBackend);
		
		this.repositoriesController = repositoriesController;
		
		this.issues = issues;
		this.issueBackend = issueBackend;
		this.issueComments = issueComments;
		this.notificationBackend = notificationBackend;
	}
	
	@GET
	@Transactional
	@Path("/issues")
	public Response getIssues() throws IOException{
		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);
		RepositoryModel repository = repositoryApi.getRepositoryModel();

		List<Issue> openIssues = issues.findOpenIssues(repositoryEntity);
		List<Issue> closedIssues = issues.findClosedIssues(repositoryEntity);

		Map<String, Object> parameters = getBaseParameters();
		
		parameters.put("repository", repository);
		parameters.put("openIssues", openIssues);
		parameters.put("closedIssues", closedIssues);
		parameters.put("repositoryEntity", repositoryEntity);
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("courses/assignments/group-issues.ftl", locales, parameters));
	
	}
	
	@GET
	@Transactional
	@Path("/issues/create")
	public Response openCreateIssuePage() throws IOException{
		
		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);
		RepositoryModel repository = repositoryApi.getRepositoryModel();
		
		Map<String, Object> parameters = getBaseParameters();		
		parameters.put("repository", repository);
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("courses/assignments/group-issue-edit.ftl", locales, parameters));
	}

	@POST
	@Transactional
	@Path("/issues/create")
	public Response createIssue(
			@FormParam("title") String title,
			@FormParam("description") String description,
			@FormParam("assignee") String assigneeNetID,
			@FormParam("labels") List<Long> labels) throws IOException{
		
		Issue issue = new Issue();
		
		issue.setTitle(title);
		issue.setDescription(description);
		issue.setOpen(true);
		
		if (!Strings.isNullOrEmpty(assigneeNetID)) {
			User assignee = users.findByNetId(assigneeNetID);
			checkCollaborator(assignee);
			issue.setAssignee(assignee);
		} else {
			issue.setAssignee(null);
		}
		
		issue.setRepository(getRepositoryEntity());

		issue.setLabels(getRepositoryEntity().getLabels().stream().filter(
				x -> labels.contains(x.getLabelId()))
				.collect(Collectors.toSet()));
		
		issueBackend.createIssue(getRepositoryApi(getRepositoryEntity()), issue);
		notificationBackend.createIssueCreatedNotification(issue);
		
		return redirect(issue.getURI().toString());
	}
	@GET
	@Transactional
	@Path("/issue/{issueId}/edit")
	public Response editIssue(@PathParam("issueId") long issueId) throws IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);
		RepositoryModel repository = repositoryApi.getRepositoryModel();
		
		Issue issue = issues.findIssueById(getRepositoryEntity(), issueId)
			.orElseThrow(NotFoundException::new);
		
		Map<String, Object> parameters = getBaseParameters();
		parameters.put("repository", repository);		
		parameters.put("issue", issue);
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("courses/assignments/group-issue-edit.ftl", locales, parameters));
	}

	@GET
	@Transactional
	@Path("/issue/{issueId}")
	public Response viewIssue(@PathParam("issueId") long issueId) throws IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);
		RepositoryModel repository = repositoryApi.getRepositoryModel();
		
		Issue issue = issues.findIssueById(getRepositoryEntity(), issueId)
			.orElseThrow(NotFoundException::new);
		
		Map<String, Object> parameters = getBaseParameters();		
		parameters.put("repository", repository);		
		parameters.put("issue", issue);
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("courses/assignments/group-issue-view.ftl", locales, parameters));
	}

	@POST
	@Transactional
	@Path("/issue/{issueId}/edit")
	public Response updateIssue(
			@PathParam("issueId") long issueId,
			@FormParam("title") String title,
			@FormParam("description") String description,
			@FormParam("assignee") String assigneeNetID,
			@FormParam("status") Boolean status,
			@FormParam("labels") List<Long> labels) throws IOException {
		
		Issue issue = issues.findIssueById(getRepositoryEntity(), issueId)
			.orElseThrow(NotFoundException::new);
		
		issue.setTitle(title);
		issue.setDescription(description);
		
		if (!Strings.isNullOrEmpty(assigneeNetID)) {
			User assignee = users.findByNetId(assigneeNetID);
			checkCollaborator(assignee);
			issue.setAssignee(assignee);
		} else {
			issue.setAssignee(null);
		}
		if(status != null && status){
			issue.setOpen(true);
		} else if (status != null && !status) {
			issue.setOpen(false);
			issue.setClosed(new Date());
		}
				
		issue.setLabels(getRepositoryEntity().getLabels().stream().filter(
				x -> labels.contains(x.getLabelId()))
				.collect(Collectors.toSet()));
		
		issues.merge(issue);

		if(issue.isClosed()) {
			notificationBackend.createIssueClosedNotification(issue);
		} else {
			notificationBackend.createIssueEditedNotification(issue);
		}

		return redirect(issue.getURI());
	}
	
	@POST
	@Transactional
	@Path("/issue/{issueId}/comment")
	public Response addComment(
			@PathParam("issueId") long issueId,
			@FormParam("content") String content) throws IOException, ApiError {
		
		Issue issue = issues.findIssueById(getRepositoryEntity(), issueId)
			.orElseThrow(NotFoundException::new);
		IssueComment comment = new IssueComment();
		comment.setContent(content);
		comment.setIssue(issue);
		comment.setUser(currentUser);

		commentBackend.post(comment);
		return redirect(issue.getURI());
	}
	

	@POST
	@Path("label")
	public Response addLabel(@FormParam("tag") String tag, @FormParam("color") String colorString) throws IOException, URISyntaxException {
		int color = Integer.parseInt(colorString, 16);		
		issueBackend.addIssueLabelToRepository(
			getRepositoryEntity(),
			tag,
			color
		);
		return redirect(new URI(request.getRequestURI()).resolve("issues"));
	}
	
	@DELETE
	@Path("label/{labelId}")
	public Response deleteLabel(@PathParam("labelId") long labelId) throws IOException, URISyntaxException {
			
		checkCollaborator(currentUser);
		
		RepositoryEntity repositoryEntity = getRepositoryEntity();
		
		// Removed label from all issues
		issues.findAllIssues(repositoryEntity).forEach(
				repo -> repo.getLabels().removeIf( label -> label.getLabelId() == labelId ));
		
		// Remove label from repository set
		repositoryEntity.getLabels().removeIf(x -> x.getLabelId() == labelId);
		repositoriesController.merge(repositoryEntity);
		
		return Response.noContent().build();
	}

	private void checkCollaborator(User user) {
		if (! getRepositoryEntity().getCollaborators().contains(user)){
			throw new UnauthorizedException();
		}
		
	}
	
	
}
