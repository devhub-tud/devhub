package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static nl.tudelft.ewi.devhub.server.database.entities.issues.QPullRequest.pullRequest;

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
	 * @param repositoryEntity Group
	 * @param id Issue id
	 * @return the PullRequest
	 */
	@Transactional
	public PullRequest findById(final RepositoryEntity repositoryEntity, final long id) {
		return ensureNotNull(query().from(pullRequest)
				.where(pullRequest.issueId.eq(id)
						.and(pullRequest.repository.eq(repositoryEntity)))
				.singleResult(pullRequest), "No pull request exists for id " + id);
	}

	/**
	 * Find an open pull request for a branch name
	 * @param repositoryEntity repositoryEntity
	 * @param branchName the branch name
	 * @return the PullRequest or null
	 */
	@Transactional
	public PullRequest findOpenPullRequest(final RepositoryEntity repositoryEntity, final String branchName) {
		return query().from(pullRequest)
			.where(pullRequest.repository.eq(repositoryEntity))
			.where(pullRequest.branchName.eq(branchName))
			.where(pullRequest.open.isTrue())
			.singleResult(pullRequest);
	}

	/**
	 * List all open pull requests for this repository
	 * @param repositoryEntity repositoryEntity
	 * @return List of all open pull requests
	 */
	@Transactional
	public List<PullRequest> findOpenPullRequests(final RepositoryEntity repositoryEntity) {
		return query().from(pullRequest)
			.where(pullRequest.repository.eq(repositoryEntity))
			.where(pullRequest.open.isTrue())
			.orderBy(pullRequest.issueId.desc())
			.list(pullRequest);
	}

	/**
	 * List all closed pull requests for this repository
	 * @param repositoryEntity repositoryEntity
	 * @return List of all closed pull requests
	 */
	@Transactional
	public List<PullRequest> findClosedPullRequests(final RepositoryEntity repositoryEntity) {
		return query().from(pullRequest)
			.where(pullRequest.repository.eq(repositoryEntity))
			.where(pullRequest.open.isFalse())
			.orderBy(pullRequest.issueId.desc())
			.list(pullRequest);
	}

	/**
	 * Check if an open pull request exists for this repository
	 * @param repositoryEntity the repositoryEntity
	 * @param branchName the branch name
	 * @return true if exists
	 */
	@Transactional
	public boolean openPullRequestExists(final RepositoryEntity repositoryEntity, final String branchName) {
		return query().from(pullRequest)
			.where(pullRequest.repository.eq(repositoryEntity))
			.where(pullRequest.branchName.eq(branchName))
			.where(pullRequest.open.isTrue())
			.exists();
	}

}
