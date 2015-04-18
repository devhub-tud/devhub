package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.persistence.EntityManager;

import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.QPullRequest;

import java.util.List;

/**
 * PullRequest data access object
 */
public class PullRequests extends Controller<PullRequest> {

	@Inject
	public PullRequests(final EntityManager entityManager) {
		super(entityManager);
	}

	/**
	 * Find a pull request by Group and issue id
	 * @param group Group
	 * @param id Issue id
	 * @return the PullRequest
	 */
	public PullRequest findById(final Group group, final long id) {
		return ensureNotNull(query().from(QPullRequest.pullRequest)
			.where(QPullRequest.pullRequest.issueId.eq(id)
			.and(QPullRequest.pullRequest.group.eq(group)))
			.singleResult(QPullRequest.pullRequest), "No pull request exists for id " + id);
	}

	/**
	 * Find an open pull request for a branch name
	 * @param group group
	 * @param branchName the branch name
	 * @return the PullRequest or null
	 */
	public PullRequest findOpenPullRequest(final Group group, final String branchName) {
		return query().from(QPullRequest.pullRequest)
			.where(QPullRequest.pullRequest.group.eq(group))
			.where(QPullRequest.pullRequest.branchName.eq(branchName))
			.where(QPullRequest.pullRequest.open.isTrue())
			.singleResult(QPullRequest.pullRequest);
	}

	/**
	 * List all open pull requests for this repository
	 * @param group group
	 * @return List of all open pull requests
	 */
	public List<PullRequest> findOpenPullRequests(final Group group) {
		return query().from(QPullRequest.pullRequest)
			.where(QPullRequest.pullRequest.group.eq(group))
			.where(QPullRequest.pullRequest.open.isTrue())
			.list(QPullRequest.pullRequest);
	}

	/**
	 * List all closed pull requests for this repository
	 * @param group group
	 * @return List of all closed pull requests
	 */
	public List<PullRequest> findClosedPullRequests(final Group group) {
		return query().from(QPullRequest.pullRequest)
			.where(QPullRequest.pullRequest.group.eq(group))
			.where(QPullRequest.pullRequest.open.isFalse())
			.list(QPullRequest.pullRequest);
	}

	/**
	 * Check if an open pull request exists for this repository
	 * @param group the group
	 * @param branchName the branch name
	 * @return true if exists
	 */
	public boolean openPullRequestExists(final Group group, final String branchName) {
		return query().from(QPullRequest.pullRequest)
			.where(QPullRequest.pullRequest.group.eq(group))
			.where(QPullRequest.pullRequest.branchName.eq(branchName))
			.where(QPullRequest.pullRequest.open.isTrue())
			.exists();
	}

	/**
	 * @param group Group
	 * @return the next pull request number
	 */
	public long getNextPullRequestNumber(final Group group) {
		Long val = query().from(QPullRequest.pullRequest)
			.where(QPullRequest.pullRequest.group.eq(group))
			.orderBy(QPullRequest.pullRequest.issueId.desc())
			.singleResult(QPullRequest.pullRequest.issueId);
		return val == null ? 1l : val.longValue() + 1;
	}

	@Override
	@Transactional
	public PullRequest persist(PullRequest entity) {
		Group group = entity.getGroup();
		entity.setIssueId(getNextPullRequestNumber(group));
		return super.persist(entity);
	}
}
