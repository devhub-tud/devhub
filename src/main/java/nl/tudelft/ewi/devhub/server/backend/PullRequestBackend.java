package nl.tudelft.ewi.devhub.server.backend;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.comments.Comment;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffBlameModel.DiffBlameLine;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffFile;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffContext;
import nl.tudelft.ewi.git.models.CommitModel;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

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
@Slf4j
public class PullRequestBackend {

    private final Commits commits;
    private final PullRequests pullRequests;
    private final CommitComments commentsDAO;

    @Inject
    public PullRequestBackend(final Commits commits,
                              final CommitComments commentsDAO,
                              final PullRequests pullRequests) {
        this.commits = commits;
        this.commentsDAO = commentsDAO;
        this.pullRequests = pullRequests;
    }

    /**
     * Persist a new PullRequest
     * @param repository Repository that contains the pull request
     * @param pullRequest PullRequest to update
     */
    @Transactional
    public void createPullRequest(RepositoryApi repository, PullRequest pullRequest) {
        updateCommitPointers(repository, pullRequest);
        log.info("Persisisting pull-request {}", pullRequest);
        // Set a dummy title to avoid exceptions because of the @NotEmpy annotations
        pullRequest.setTitle(pullRequest.getBranchName());
        pullRequests.persist(pullRequest);
        pullRequest.setTitle(String.format("Pull request #%d: %s", 
        		pullRequest.getIssueId(),
        		pullRequest.getBranchName()));
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
     */
    @Transactional
    public void updatePullRequest(RepositoryApi repository, PullRequest pullRequest) {
        if(pullRequest.isClosed()) {
            // No-op for closed pull requests
            return;
        }

        updateCommitPointers(repository, pullRequest);
        pullRequests.merge(pullRequest);
    }

    private void updateCommitPointers(RepositoryApi repository, PullRequest pullRequest) {
        BranchApi branchApi;
        BranchModel branch;

        try {
            branchApi = repository.getBranch(pullRequest.getBranchName());
            branch = branchApi.get();
        }
        catch (NotFoundException e) {
            pullRequest.setOpen(false);
            log.info("Closing pull request {} as the branch has been removed", pullRequest);
            return;
        }

        pullRequest.setAhead(branch.getAhead());
        pullRequest.setBehind(branch.getBehind());
        updateDestinationCommit(pullRequest, branch);

        if(!branch.isAhead()) {
            // If the branch is not ahead of the master, it's merged.
            pullRequest.setOpen(false);
            pullRequest.setMerged(true);
            log.info("Closing pull request {} as the branch is not ahead of the master", pullRequest);
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
            updateMergeBase(pullRequest, branchApi);
        }
    }

    private void updateMergeBase(PullRequest pullRequest, BranchApi branch) {
        CommitModel mergeBase = branch.mergeBase().get();
        Commit mergeBaseCommit = commits.ensureExists(pullRequest.getRepository(), mergeBase.getCommit());
        if(!mergeBaseCommit.equals(pullRequest.getMergeBase())) {
            pullRequest.setMergeBase(mergeBaseCommit);
            log.info("Merge-base set to {} for {}", mergeBaseCommit, pullRequest);
        }
    }

    private void updateDestinationCommit(PullRequest pullRequest, BranchModel branch) {
        CommitModel destination = branch.getCommit();
        Commit destinationCommit = commits.ensureExists(pullRequest.getRepository(), destination.getCommit());
        if(!destinationCommit.equals(pullRequest.getDestination())) {
            pullRequest.setDestination(destinationCommit);
            log.info("Destination set to {} for {}", destinationCommit, pullRequest);
        }
    }

    /**
     *
     * @param pullRequest  PullRequest to update
     * @param diffModel DiffBlameModel for the pull request
     * @param repositoryEntity the current repository
     * @return  a List of events
     */
    public EventResolver getEventResolver(final PullRequest pullRequest, final DiffBlameModel diffModel, final RepositoryEntity repositoryEntity) {
        return new EventResolver(pullRequest, diffModel, repositoryEntity);
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
    @EqualsAndHashCode(callSuper = true)
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
    @EqualsAndHashCode(callSuper = true)
    public static class CommentEvent extends Event {

        private final Comment comment;

        public CommentEvent(Comment comment) {
            super(EventType.COMMENT);
            this.comment = comment;
        }

        @Override
        public Date getDate() {
            return comment.getTimestamp();
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CommentContextEvent extends Event {

        private final SortedSet<CommitComment> comments;
        private final DiffFile<DiffContext<DiffBlameLine>> diffBlameFile;

        public CommentContextEvent(SortedSet<CommitComment> comments, DiffFile<DiffContext<DiffBlameLine>> diffBlameFile) {
            super(EventType.COMMENT_CONTEXT);
            this.comments = comments;
            this.diffBlameFile = diffBlameFile;
        }

        @Override
        public Date getDate() {
            return comments.first().getTimestamp();
        }

    }

    public class EventResolver {

        private final DiffBlameModel diffModel;
        private final List<CommitComment> inlineComments;
        private final PullRequest pullRequest;
        private final List<String> commitIds;
        private final RepositoryEntity repositoryEntity;

        public EventResolver(PullRequest pullRequest, DiffBlameModel diffModel, RepositoryEntity repositoryEntity) {
            this.diffModel = diffModel;
            this.pullRequest = pullRequest;
            this.commitIds = Lists.transform(diffModel.getCommits(), CommitModel::getCommit);
            this.inlineComments = commentsDAO.getInlineCommentsFor(repositoryEntity, commitIds);
            this.repositoryEntity = repositoryEntity;
        }

        public SortedSet<Event> getEvents() {
            SortedSet<Event> result = Sets.newTreeSet();
            result.addAll(getCommitEvents());
            result.addAll(getCommentEvents());
            result.addAll(getCommentsForCommits());
            result.addAll(getInlineCommentEvents());
            return result;
        }

        private Collection<CommentEvent> getCommentsForCommits() {
            return commentsDAO.getCommentsFor(repositoryEntity, commitIds)
                    .stream()
                    .map(CommentEvent::new)
                    .collect(Collectors.toList());
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
                    Source source = entry.getKey();
                    SortedSet<CommitComment> comments = Sets.newTreeSet(entry.getValue());
                    DiffFile<DiffContext<DiffBlameLine>> subModel = findSubModel(source);
                    return new CommentContextEvent(comments, subModel);
                })
                .collect(Collectors.toList());
        }

        private DiffFile<DiffContext<DiffBlameLine>>  findSubModel(Source source) {
            for(DiffFile<DiffContext<DiffBlameLine>>  diffFile : diffModel.getDiffs()) {
                for(DiffContext<DiffBlameLine> diffContext : diffFile.getContexts()) {
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

        private DiffFile<DiffContext<DiffBlameLine>> createSubModel(DiffFile<DiffContext<DiffBlameLine>> diffFile,
                                                                    DiffContext<DiffBlameLine> diffContext,
                                                                    int lineOfInterest) {
            List<DiffBlameModel.DiffBlameLine> sublist =
                    diffContext.getLines().subList(Math.max(0, lineOfInterest - 4), lineOfInterest + 1);

            DiffContext<DiffBlameLine> contextCopy = new DiffContext<>();
            contextCopy.setLines(sublist);

            DiffFile<DiffContext<DiffBlameLine>> fileCopy = new DiffFile<>();
            fileCopy.setContexts(Lists.newArrayList(contextCopy));
            fileCopy.setNewPath(diffFile.getNewPath());
            fileCopy.setOldPath(diffFile.getOldPath());
            fileCopy.setType(diffFile.getType());
            return fileCopy;
        }

    }

}
