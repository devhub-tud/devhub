package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.persistence.EntityManager;

import com.google.inject.Inject;

import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.QPullRequest;

import java.util.List;

public class PullRequests extends Controller<PullRequest> {

	@Inject
	public PullRequests(final EntityManager entityManager) {
		super(entityManager);
	}
	
	public PullRequest findById(final long id) {
		return ensureNotNull(query().from(QPullRequest.pullRequest)
			.where(QPullRequest.pullRequest.issueId.eq(id))
			.singleResult(QPullRequest.pullRequest), "No pull request exists for id " + id);
	}
	
	public PullRequest findOpenPullRequest(final Group group, final String branchName) {
		return query().from(QPullRequest.pullRequest)
			.where(QPullRequest.pullRequest.group.eq(group))
			.where(QPullRequest.pullRequest.branchName.eq(branchName))
			.where(QPullRequest.pullRequest.open.isTrue())
			.singleResult(QPullRequest.pullRequest);
	}

	public List<PullRequest> findOpenPullRequests(final Group group) {
		return query().from(QPullRequest.pullRequest)
			.where(QPullRequest.pullRequest.group.eq(group))
			.where(QPullRequest.pullRequest.open.isTrue())
			.list(QPullRequest.pullRequest);
	}

}
