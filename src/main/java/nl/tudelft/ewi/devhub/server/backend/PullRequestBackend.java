package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import lombok.Data;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequest;
import nl.tudelft.ewi.git.models.CommitModel;

import java.util.Date;
import java.util.SortedSet;
import java.util.function.Function;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class PullRequestBackend {

    @Data
    public static class Event implements Comparable<Event> {

        private final Date date;

        @Override
        public int compareTo(Event other) {
            return getDate().compareTo(other.getDate());
        }

    }

    @Data
    public static class CommitEvent extends Event {

        private final CommitModel commit;

        public CommitEvent(CommitModel commit) {
            super(new Date(commit.getTime()));
            this.commit = commit;
        }

        public static Function<CommitModel, CommitEvent> transformer =
                (commitModel) -> new CommitEvent(commitModel);

    }

    @Data
    public static class CommentEvent extends Event {

        private final CommitComment comment;

        public CommentEvent(CommitComment comment) {
            super(comment.getTime());
            this.comment = comment;
        }

        public static Function<CommitComment, CommentEvent> transformer =
                (comment) -> new CommentEvent(comment);

    }

    private final CommitComments commentsDAO;

    @Inject
    public PullRequestBackend(CommitComments commentsDAO) {
        this.commentsDAO = commentsDAO;
    }

    public SortedSet<Event> getEventsForPullRequest(PullRequest request) {
        SortedSet<Event> events = Sets.newTreeSet();

        return null;
    }

}
