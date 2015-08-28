package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static nl.tudelft.ewi.devhub.server.database.entities.comments.QCommitComment.commitComment;

/**
 * Data access object for comments attached to a commit
 */
public class CommitComments extends Controller<CommitComment> {

	@Inject
	public CommitComments(final EntityManager entityManager) {
        super(entityManager);
	}

    /**
     * Retrieve commit scoped comments
     * @param repositoryEntity repositoryEntity
     * @param commitIds Commit id
     * @return A List of comments attached to the commit
     */
    @Transactional
    public List<CommitComment> getCommentsFor(RepositoryEntity repositoryEntity, String... commitIds) {
        return query().from(commitComment)
                .where(commitComment.source.sourceFilePath.isNull()
                        .and(commitComment.commit.repository.eq(repositoryEntity)
                                .and(commitComment.commit.commitId.in(commitIds))))
            .list(commitComment);
    }

    /**
     * Retrieve commit scoped comments
     * @param repositoryEntity repositoryEntity
     * @param commitIds Commit id
     * @return A List of comments attached to the commit
     */
    @Transactional
    public List<CommitComment> getCommentsFor(RepositoryEntity repositoryEntity, List<String> commitIds) {
        return query().from(commitComment)
                .where(commitComment.source.sourceFilePath.isNull()
                        .and(commitComment.commit.repository.eq(repositoryEntity)
                                .and(commitComment.commit.commitId.in(commitIds))))
                .list(commitComment);
    }

    /**
     * Retrieve inline comments
     * @param repositoryEntity Group
     * @param commitIds Commit id
     * @return A List of comments attached to the commit
     */
    @Transactional
    public List<CommitComment> getInlineCommentsFor(RepositoryEntity repositoryEntity, Collection<String> commitIds) {
        return query().from(commitComment)
            .where(commitComment.source.sourceFilePath.isNotNull()
                    .and(commitComment.commit.repository.eq(repositoryEntity)
                            .and(commitComment.commit.commitId.in(commitIds))))
            .list(commitComment);
    }

    /**
     * Check which commits for a repositoryEntity have warnings
     * @param repositoryEntity {@link Group} to check for
     * @return a list of commit ids that have warnings
     */
    @Transactional
    public Map<String, Long> commentsFor(RepositoryEntity repositoryEntity, Collection<String> commitIds) {
        return query().from(commitComment)
            .where(commitComment.commit.repository.eq(repositoryEntity)
                    .and(commitComment.commit.commitId.in(commitIds)))
            .groupBy(commitComment.commit.commitId)
            .map(commitComment.commit.commitId, commitComment.commentId.count());
    }

}
