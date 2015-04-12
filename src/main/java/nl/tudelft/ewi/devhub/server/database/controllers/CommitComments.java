package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.persistence.EntityManager;

import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.QCommitComment;

import java.util.List;

public class CommitComments extends Controller<CommitComment> {

	@Inject
	public CommitComments(final EntityManager entityManager) {
        super(entityManager);
	}

    @Transactional
    public List<CommitComment> getCommentsFor(List<String> commitIds) {
        return query().from(QCommitComment.commitComment)
            .where(QCommitComment.commitComment.commit.commitId.in(commitIds))
            .list(QCommitComment.commitComment);
    }

    @Transactional
    public long amountOfComments(String commitId) {
        return query().from(QCommitComment.commitComment)
            .where(QCommitComment.commitComment.commit.commitId.eq(commitId))
            .count();
    }

}
