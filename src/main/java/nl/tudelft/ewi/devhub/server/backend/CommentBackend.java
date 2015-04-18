package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.entities.Comment;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jgmeligmeyling on 05/03/15.
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class CommentBackend {

    @Inject
    @Named("current.user")
    private User currentUser;

    @Inject
    @Named("current.group")
    private Group group;

    @Inject
    private CommitComments commentsDAO;

    @Inject
    private EntityManager entityManager;

    /**
     * Post a new comment
     * @param comment Comment to post
     * @throws UnauthorizedException If the user is not authorized to post a comment into this repository
     * @throws ApiError
     */
    public void post(Comment comment) throws UnauthorizedException, ApiError {
        Preconditions.checkNotNull(comment);

        if(!(currentUser.isAdmin() || currentUser.isAssisting(group.getCourse()) ||
                group.getMembers().contains(currentUser))) {
            throw new UnauthorizedException();
        }

        comment.setTime(new Date());
        comment.setUser(currentUser);

        try {
            entityManager.persist(comment);
            log.info("Persisted comment: {}", comment);
        }
        catch (Exception e) {
            throw new ApiError("error.could-not-comment", e);
        }
    }

    /**
     * A commit checker can be used in a Freemarker template to find the
     * commits for a line
     * @param commitIds commits to look for
     * @return a CommitChecker
     */
    public CommentChecker getCommentChecker(List<String> commitIds) {
        return new CommentChecker(commitIds);
    }

    /**
     * A commit checker can be used in a Freemarker template to find the
     * commits for a line
     */
    public class CommentChecker {

        public final List<CommitComment> comments;

        public CommentChecker(List<String> commitIds) {
            comments = commentsDAO.getInlineCommentsFor(group, commitIds);
        }

        /**
         * Get all the comments for a specific line
         * @param sourceCommitId the source commit id
         * @param sourcePath the source path
         * @param sourceLineNumber the source line number
         * @return a list of all commits for this line
         */
        public List<CommitComment> getCommentsForLine(final String sourceCommitId,
                                                      final String sourcePath,
                                                      final Integer sourceLineNumber) {
            return comments.stream().filter((comment) -> {
                CommitComment.Source source = comment.getSource();
                return source.getSourceCommit().getCommitId().equals(sourceCommitId) &&
                        source.getSourceFilePath().equals(sourcePath) &&
                        source.getSourceLineNumber().equals(sourceLineNumber);
            }).sorted().collect(Collectors.toList());
        }

    }

}
