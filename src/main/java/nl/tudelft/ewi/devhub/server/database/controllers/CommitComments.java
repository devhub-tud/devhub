package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.persistence.EntityManager;

import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.QCommitComment;

import java.util.List;

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
        return query().from(QCommitComment.commitComment)
                .where(QCommitComment.commitComment.source.isNull()
                        .and(QCommitComment.commitComment.commit.repository.eq(group)
                                .and(QCommitComment.commitComment.commit.commitId.in(commitIds))))
            .list(QCommitComment.commitComment);
    }

    /**
     * Retrieve commit scoped comments
     * @param group Group
     * @param commitIds Commit id
     * @return A List of comments attached to the commit
     */
    @Transactional
    public List<CommitComment> getCommentsFor(Group group, List<String> commitIds) {
        return query().from(QCommitComment.commitComment)
                .where(QCommitComment.commitComment.source.isNull()
                        .and(QCommitComment.commitComment.commit.repository.eq(group)
                                .and(QCommitComment.commitComment.commit.commitId.in(commitIds))))
                .list(QCommitComment.commitComment);
    }

    /**
     * Retrieve inline comments
     * @param group Group
     * @param commitIds Commit id
     * @return A List of comments attached to the commit
     */
    @Transactional
    public List<CommitComment> getInlineCommentsFor(Group group, List<String> commitIds) {
        return query().from(QCommitComment.commitComment)
            .where(QCommitComment.commitComment.source.isNotNull()
                    .and(QCommitComment.commitComment.commit.repository.eq(group)
                            .and(QCommitComment.commitComment.commit.commitId.in(commitIds))))
            .list(QCommitComment.commitComment);
    }

    /**
     * @param group Group
     * @param commitId Commit id
     * @return the amount of comments attached to this commit
     */
    @Transactional
    public long amountOfComments(Group group, String commitId) {
        return query().from(QCommitComment.commitComment)
            .where(QCommitComment.commitComment.commit.repository.eq(group)
            .and(QCommitComment.commitComment.commit.commitId.eq(commitId)))
            .count();
    }

}
