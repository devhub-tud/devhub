package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static nl.tudelft.ewi.devhub.server.database.entities.QCommitComment.commitComment;

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
     * @param group Group
     * @param commitIds Commit id
     * @return A List of comments attached to the commit
     */
    @Transactional
    public List<CommitComment> getCommentsFor(Group group, String... commitIds) {
        return query().from(commitComment)
                .where(commitComment.source.isNull()
                        .and(commitComment.commit.repository.eq(group)
                                .and(commitComment.commit.commitId.in(commitIds))))
            .list(commitComment);
    }

    /**
     * Retrieve commit scoped comments
     * @param group Group
     * @param commitIds Commit id
     * @return A List of comments attached to the commit
     */
    @Transactional
    public List<CommitComment> getCommentsFor(Group group, List<String> commitIds) {
        return query().from(commitComment)
                .where(commitComment.source.isNull()
                        .and(commitComment.commit.repository.eq(group)
                                .and(commitComment.commit.commitId.in(commitIds))))
                .list(commitComment);
    }

    /**
     * Retrieve inline comments
     * @param group Group
     * @param commitIds Commit id
     * @return A List of comments attached to the commit
     */
    @Transactional
    public List<CommitComment> getInlineCommentsFor(Group group, Collection<String> commitIds) {
        return query().from(commitComment)
            .where(commitComment.source.isNotNull()
                    .and(commitComment.commit.repository.eq(group)
                            .and(commitComment.commit.commitId.in(commitIds))))
            .list(commitComment);
    }

    /**
     * Check which commits for a group have warnings
     * @param group {@link Group} to check for
     * @return a list of commit ids that have warnings
     */
    @Transactional
    public Map<String, Long> commentsFor(Group group, Collection<String> commitIds) {
        return query().from(commitComment)
            .where(commitComment.commit.repository.eq(group)
                    .and(commitComment.commit.commitId.in(commitIds)))
            .groupBy(commitComment.commit.commitId)
            .map(commitComment.commit.commitId, commitComment.commentId.count());
    }

}
