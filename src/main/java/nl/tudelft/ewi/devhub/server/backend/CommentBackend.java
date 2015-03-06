package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;

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
    private Commits commits;

    @Inject
    private CommitComments commentsDAO;

    /**
     * Helper for posting comments
     */
    @Data
    @Accessors(chain = true)
    public class CommentBuilder {

        private String commitId;
        private String sourceCommitId;
        private String sourceFilePath;
        private Integer sourceLineNumber;
        private String message;

        /**
         * Persist a comment
         * @throws ApiError if the comment could not be persisted
         * @throws UnauthorizedException if the user may not post to this group
         */
        public void persist() throws ApiError, UnauthorizedException {
            comment(commitId, sourceCommitId, sourceFilePath, sourceLineNumber, message);
        }
    }

    /**
     * @return a new CommentBuilder
     */
    public CommentBuilder commentBuilder() {
        return new CommentBuilder();
    }

    /**
     * Post a comment
     * @param commitId commit to attach
     * @param sourceCommitId commit source
     * @param sourceFilePath source path
     * @param sourceLineNumber source number
     * @param message message
     * @throws UnauthorizedException if the user may not post to this group
     * @throws ApiError if the comment could not be persisted
     */
    public void comment(String commitId, String sourceCommitId, String sourceFilePath,
                        Integer sourceLineNumber, String message) throws UnauthorizedException, ApiError {
        Preconditions.checkNotNull(commitId);
        Preconditions.checkNotNull(sourceCommitId);
        Preconditions.checkNotNull(sourceFilePath);
        Preconditions.checkNotNull(sourceLineNumber);
        Preconditions.checkNotNull(message);

        if(!(currentUser.isAdmin() || currentUser.isAssisting(group.getCourse()) ||
                group.getMembers().contains(currentUser))) {
            throw new UnauthorizedException();
        }

        Commit link = commits.ensureExists(group, commitId);
        Commit sourceCommit = commits.ensureExists(group, sourceCommitId);

        CommitComment comment = new CommitComment();

        CommitComment.Source source = new CommitComment.Source();
        source.setSourceCommit(sourceCommit);
        source.setSourceFilePath(sourceFilePath);
        source.setSourceLineNumber(sourceLineNumber);
        comment.setSource(source);

        comment.setCommit(link);
        comment.setContent(message);
        comment.setTime(new Date());
        comment.setUser(currentUser);

        try {
            commentsDAO.persist(comment);
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
            comments = commentsDAO.getCommentsFor(commitIds);
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
