package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import lombok.Data;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequest;
import nl.tudelft.ewi.git.client.Branch;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class PullRequestBackend {

    enum EventType {
        COMMIT,
        COMMENT,
        COMMENT_CONTEXT;
    }

    public static abstract class Event implements Comparable<Event> {

        // Use an enumerated type over inheritance for freemarker checks
        private final EventType eventType;

        public Event(EventType eventType) {
            this.eventType = eventType;
        }

        @Override
        public int compareTo(Event other) {
            return getDate().compareTo(other.getDate());
        }

        public abstract Date getDate();

        public boolean isCommitEvent() {
            return eventType.equals(EventType.COMMIT);
        }

        public boolean isCommentContextEvent() {
            return eventType.equals(EventType.COMMENT_CONTEXT);
        }
    }

    @Data
    public static class CommitEvent extends Event {

        private final CommitModel commit;

        public CommitEvent(CommitModel commit) {
            super(EventType.COMMIT);
            this.commit = commit;
        }

        public Date getDate() {
            return new Date(commit.getTime() * 1000);
        }

        public static Function<CommitModel, CommitEvent> transformer =
                (commitModel) -> new CommitEvent(commitModel);

    }

    @Data
    public static class CommentEvent extends Event {

        private final CommitComment comment;

        public CommentEvent(CommitComment comment) {
            super(EventType.COMMENT);
            this.comment = comment;
        }

        @Override
        public Date getDate() {
            return comment.getTime();
        }

        public static Function<CommitComment, CommentEvent> transformer =
                (comment) -> new CommentEvent(comment);

    }

    @Data
    public static class CommentContextEvent extends Event {

        private final SortedSet<CommitComment> comments;
        private final DiffBlameModel.DiffBlameFile diffBlameFile;

        public CommentContextEvent(SortedSet<CommitComment> comments, DiffBlameModel.DiffBlameFile diffBlameFile) {
            super(EventType.COMMENT_CONTEXT);
            this.comments = comments;
            this.diffBlameFile = diffBlameFile;
        }

        @Override
        public Date getDate() {
            return comments.first().getTime();
        }

    }

    public class EventResolver {

        private final DiffBlameModel diffModel;
        private final List<CommitComment> comments;

        public EventResolver(DiffBlameModel diffModel) {
            this.diffModel = diffModel;
            List<String> commitIds = Lists.transform(diffModel.getCommits(), CommitModel::getCommit);
            this.comments = commentsDAO.getCommentsFor(commitIds);
        }

        public SortedSet<Event> getEvents() {
            SortedSet<Event> result = Sets.newTreeSet();
            result.addAll(getCommitEvents());
            result.addAll(getCommentEvents());
            return result;
        }

        private Collection<CommitEvent> getCommitEvents() {
            return diffModel.getCommits().stream()
                .map(CommitEvent::new)
                .collect(Collectors.toList());
        }

        private Collection<CommentContextEvent> getCommentEvents() {
            // Do not use parallel streams as the source objects can only be accessed
            // through the current (thread local) transaction!
            return comments.stream()
                .collect(Collectors.groupingBy(CommitComment::getSource))
                .entrySet().stream().map(entry -> {
                    CommitComment.Source source = entry.getKey();
                    SortedSet<CommitComment> comments = Sets.newTreeSet(entry.getValue());
                    DiffBlameModel.DiffBlameFile subModel = findSubModel(source, comments);
                    return new CommentContextEvent(comments, subModel);
                })
                .collect(Collectors.toList());
        }

        private DiffBlameModel.DiffBlameFile findSubModel(CommitComment.Source source, Collection<CommitComment> comments) {
//            diffModel.getDiffs().stream()
//                    .map(DiffBlameModel.DiffBlameFile::getContexts)
//                    .flatMap(List::stream)
//                    .reduce((diffBlameContext, diffBlameContext2) -> {
//
//                    })
            for(DiffBlameModel.DiffBlameFile diffFile : diffModel.getDiffs()) {
                for(DiffBlameModel.DiffBlameContext diffContext : diffFile.getContexts()) {
                    List<DiffBlameModel.DiffBlameLine> lines = diffContext.getLines();
                    for(int i = 0, s = lines.size(); i < s; i++) {
                        DiffBlameModel.DiffBlameLine line = lines.get(i);
                        if(line.getSourceCommitId().equals(source.getSourceCommit().getCommitId()) &&
                                line.getSourceFilePath().equals(source.getSourceFilePath()) &&
                                line.getSourceLineNumber() == source.getSourceLineNumber()) {
                            return createSubModel(diffFile, diffContext, i);
                        }
                    }
                }
            }
            return null;
        }

        private DiffBlameModel.DiffBlameFile createSubModel(DiffBlameModel.DiffBlameFile diffFile,
                                              DiffBlameModel.DiffBlameContext diffContext,
                                              int lineOfInterest) {
            List<DiffBlameModel.DiffBlameLine> sublist =
                    diffContext.getLines().subList(Math.max(0, lineOfInterest - 4), lineOfInterest + 1);

            DiffBlameModel.DiffBlameContext contextCopy = new DiffBlameModel.DiffBlameContext();
            contextCopy.setLines(sublist);

            DiffBlameModel.DiffBlameFile fileCopy = new DiffBlameModel.DiffBlameFile();
            fileCopy.setContexts(Lists.newArrayList(contextCopy));
            fileCopy.setNewPath(diffFile.getNewPath());
            fileCopy.setOldPath(diffFile.getOldPath());
            fileCopy.setType(diffFile.getType());
            return fileCopy;
        }

    }

    private final CommitComments commentsDAO;

    @Inject
    public PullRequestBackend(CommitComments commentsDAO) {
        this.commentsDAO = commentsDAO;
    }

    public SortedSet<Event> getEventsForPullRequest(Repository repository, PullRequest pullRequest) throws GitClientException {
        Branch branch = repository.retrieveBranch(pullRequest.getBranchName());
        return new EventResolver(branch.diffBlame()).getEvents();
    }

}
