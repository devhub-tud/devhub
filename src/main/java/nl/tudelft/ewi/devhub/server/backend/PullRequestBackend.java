package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import lombok.Data;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequestComment;
import nl.tudelft.ewi.git.client.Branch;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;

import javax.ws.rs.NotFoundException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class PullRequestBackend {

    private final PullRequests pullRequests;
    private final CommitComments commentsDAO;
    private final Group group;

    @Inject
    public PullRequestBackend(final CommitComments commentsDAO,
                              final PullRequests pullRequests,
                              final @Named("current.group") Group group) {
        this.commentsDAO = commentsDAO;
        this.pullRequests = pullRequests;
        this.group = group;
    }

    /**
     * Persist a new PullRequest
     * @param repository Repository that contains the pull request
     * @param pullRequest PullRequest to update
     * @throws GitClientException if a GitClientException occurs
     */
    @Transactional
    public void createPullRequest(Repository repository, PullRequest pullRequest) throws GitClientException {
        updateCommitPointers(repository, pullRequest);
        pullRequests.persist(pullRequest);
    }

    /**
     * Update the pull request
     *
     * <ul>
     *     <li>Close the branch if it is not ahead of the master anymore</li>
     *     <li>Reset the pointers for the destination commit and merge-base commits</li>
     * </ul>
     *
     * @param repository Repository that contains the pull request
     * @param pullRequest PullRequest to update
     * @throws GitClientException if a GitClientException occurs
     */
    @Transactional
    public void updatePullRequest(Repository repository, PullRequest pullRequest) throws GitClientException {
        if(pullRequest.isClosed()) {
            // No-op for closed pull requests
            return;
        }

        updateCommitPointers(repository, pullRequest);
        pullRequests.merge(pullRequest);
    }

    private void updateCommitPointers(Repository repository, PullRequest pullRequest) throws GitClientException {
        Branch branch;

        try {
            branch = repository.retrieveBranch(pullRequest.getBranchName());
        }
        catch (NotFoundException e) {
            pullRequest.setOpen(false);
            return;
        }

        pullRequest.setAhead(branch.getAhead());
        pullRequest.setBehind(branch.getBehind());
        updateDestinationCommit(pullRequest, branch);

        if(!branch.isAhead()) {
            // If the branch is not ahead of the master, it's merged.
            pullRequest.setOpen(false);
            pullRequest.setMerged(true);
        }
        else {
            /**
             * If the branch has been fast-forwarded or some of the branches
             * commits have already been merged into the master, the merge
             * base might have changed.
             *
             * For merged branches we can't determine the merge base anymore
             * (obviously because the branch isn't ahead of the master anymore).
             * Therefore we rely on frequent calls to the update function,
             * for example triggered by git pushes.
             */
            updateMergeBase(pullRequest, branch);
        }
    }

    private void updateMergeBase(PullRequest pullRequest, Branch branch) throws GitClientException {
        CommitModel mergeBase = branch.mergeBase();
        pullRequest.setMergeBase(mergeBase.getCommit());
    }

    private void updateDestinationCommit(PullRequest pullRequest, Branch branch) {
        CommitModel destination = branch.getCommit();
        pullRequest.setDestination(destination.getCommit());
    }

    /**
     * Get the events for a PullRequest
     * @param repository Repository that contains the pull request
     * @param pullRequest PullRequest to update
     * @return a List of events
     * @throws GitClientException if a GitClientException occurs
     */
    public SortedSet<Event> getEventsForPullRequest(Repository repository, PullRequest pullRequest) throws GitClientException {
        DiffBlameModel diffBlameModel = getDiffBlameModelForPull(pullRequest, repository);
        return new EventResolver(pullRequest, diffBlameModel).getEvents();
    }

    private static DiffBlameModel getDiffBlameModelForPull(PullRequest pullRequest, Repository repository) throws GitClientException {
        String destinationId = pullRequest.getDestination();
        String mergeBaseId = pullRequest.getMergeBase();
        return repository.retrieveCommit(destinationId).diffBlame(mergeBaseId);
    }

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

        public boolean isCommentEvent() { return eventType.equals(EventType.COMMENT); }

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

    }

    @Data
    public static class CommentEvent extends Event {

        private final PullRequestComment comment;

        public CommentEvent(PullRequestComment comment) {
            super(EventType.COMMENT);
            this.comment = comment;
        }

        @Override
        public Date getDate() {
            return comment.getTime();
        }

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
        private final List<CommitComment> inlineComments;
        private final PullRequest pullRequest;

        public EventResolver(PullRequest pullRequest, DiffBlameModel diffModel) {
            this.diffModel = diffModel;
            this.pullRequest = pullRequest;
            List<String> commitIds = Lists.transform(diffModel.getCommits(), CommitModel::getCommit);
            this.inlineComments = commentsDAO.getCommentsFor(group, commitIds);
        }

        public SortedSet<Event> getEvents() {
            SortedSet<Event> result = Sets.newTreeSet();
            result.addAll(getCommitEvents());
            result.addAll(getCommentEvents());
            result.addAll(getInlineCommentEvents());
            return result;
        }

        private Collection<CommentEvent> getCommentEvents() {
            return pullRequest.getComments()
                    .stream()
                    .map(CommentEvent::new)
                    .collect(Collectors.toList());
        }

        private Collection<CommitEvent> getCommitEvents() {
            return diffModel.getCommits().stream()
                .map(CommitEvent::new)
                .collect(Collectors.toList());
        }

        private Collection<CommentContextEvent> getInlineCommentEvents() {
            // Do not use parallel streams as the source objects can only be accessed
            // through the current (thread local) transaction!
            return inlineComments.stream()
                .collect(Collectors.groupingBy(CommitComment::getSource))
                .entrySet().stream().map(entry -> {
                    CommitComment.Source source = entry.getKey();
                    SortedSet<CommitComment> comments = Sets.newTreeSet(entry.getValue());
                    DiffBlameModel.DiffBlameFile subModel = findSubModel(source);
                    return new CommentContextEvent(comments, subModel);
                })
                .collect(Collectors.toList());
        }

        private DiffBlameModel.DiffBlameFile findSubModel(CommitComment.Source source) {
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

}
