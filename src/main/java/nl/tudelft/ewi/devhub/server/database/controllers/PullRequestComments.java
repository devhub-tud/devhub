package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.comments.PullRequestComment;

import com.google.inject.Inject;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Stream;

import static nl.tudelft.ewi.devhub.server.database.entities.comments.QPullRequestComment.pullRequestComment;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class PullRequestComments extends Controller<PullRequestComment> {

    @Inject
    public PullRequestComments(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Get the most recent pull request comments.
     * @param repositoryEntities The repository entities to include.
     * @param limit The maximal number of results.
     * @return The list of most recent pull request comments.
     */
    @Transactional
    public Stream<PullRequestComment> getMostRecentPullRequestComments(List<? extends RepositoryEntity> repositoryEntities, long limit) {
        return toStream(query().from(pullRequestComment)
            .where(pullRequestComment.pullRequest.repository.in(repositoryEntities))
            .orderBy(pullRequestComment.timestamp.desc())
            .limit(limit)
            .iterate(pullRequestComment));
    }

}
