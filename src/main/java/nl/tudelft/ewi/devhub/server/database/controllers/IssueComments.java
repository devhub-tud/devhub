package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.persistence.EntityManager;

import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.comments.IssueComment;

import java.util.List;
import java.util.stream.Stream;

import static nl.tudelft.ewi.devhub.server.database.entities.comments.QIssueComment.issueComment;

public class IssueComments extends Controller<IssueComment> {

	@Inject
	public IssueComments(EntityManager entityManager) {
		super(entityManager);
	}

	/**
	 * Get the most recent issue comments.
	 * @param repositoryEntities The repository entities to include.
	 * @param limit The maximal number of results.
	 * @return The list of most recent pull request comments.
	 */
	@Transactional
	public Stream<? extends IssueComment> getMostRecentIssueComments(List<? extends RepositoryEntity> repositoryEntities, int limit) {
		return toStream(query().from(issueComment)
			.where(issueComment.issue.repository.in(repositoryEntities))
			.orderBy(issueComment.timestamp.desc())
			.limit(limit)
			.iterate(issueComment));
	}
}
