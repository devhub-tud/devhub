package nl.tudelft.ewi.devhub.server.backend;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.comments.Comment;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.persistence.EntityManager;
import java.util.Collection;
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
    private CommitComments commentsDAO;

    @Inject
    private EntityManager entityManager;

    @Inject
    private NotificationBackend notificationBackend;

    /**
     * Post a new comment. Must be admin or assistant of the course
     * or a member of the group to comment on.
     * 
     * @param comment
     * 		Comment to post
     * @throws UnauthorizedException
     * 		If the user is not authorized to post a comment into this repository
     * @throws ApiError
     * 		If the comment could not be saved in the database
     */
    public void post(Comment comment) throws UnauthorizedException, ApiError {
        Preconditions.checkNotNull(comment);

        comment.setUser(currentUser);

        try {
            entityManager.persist(comment);
            log.info("Persisted comment: {}", comment);
        }
        catch (Exception e) {
            throw new ApiError("error.could-not-comment", e);
        }

        notificationBackend.createCommentNotification(comment);
    }

    /**
     * A commit checker can be used in a Freemarker template to find the
     * commits for a line.
     * 
     * @param commitIds
     * 		commits to look for
     * @return a CommitChecker
     */
    public CommentChecker getCommentChecker(RepositoryEntity repositoryEntity, Collection<String> commitIds) {
        return new CommentChecker(repositoryEntity, commitIds);
    }

    /**
     * A commit checker can be used in a Freemarker template to find the
     * commits for a line.
     */
    public class CommentChecker {

    	/**
    	 * All the comments for all commits for the {@link RepositoryEntity}.
    	 */
        public final List<CommitComment> comments;

        public CommentChecker(RepositoryEntity repositoryEntity, Collection<String> commitIds) {
            comments = commentsDAO.getInlineCommentsFor(repositoryEntity, commitIds);
        }

        /**
         * Get all the comments for a specific line.
         * 
         * @param sourceCommitId
         * 		The source commit id
         * @param sourcePath
         * 		The source path
         * @param sourceLineNumber
         * 		The source line number
         * @return a list of all commits for this line
         */
        public List<CommitComment> getCommentsForLine(final String sourceCommitId,
                                                      final String sourcePath,
                                                      final Integer sourceLineNumber) {
            return comments.stream()
                .filter((comment) -> comment.getSource().equals(sourceCommitId, sourcePath, sourceLineNumber))
                .sorted()
                .collect(Collectors.toList());
        }

    }

}
