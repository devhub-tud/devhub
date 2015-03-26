package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import lombok.Data;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequest;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.models.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.function.Function;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class PullRequestBackend {
//
//    enum EventType {
//        COMMIT, COMMENT, COMMENT_CONTEXT;
//    }
//
//    public static abstract class Event implements Comparable<Event> {
//
//        // Use an enumerated type over inheritance for freemarker checks
//        private final EventType eventType;
//
//        public Event(EventType eventType) {
//            this.eventType = eventType;
//        }
//
//        @Override
//        public int compareTo(Event other) {
//            return getDate().compareTo(other.getDate());
//        }
//
//        public abstract Date getDate();
//
//        public boolean isCommit() {
//            return eventType.equals(EventType.COMMIT);
//        }
//
//        public boolean isComment() {
//            return eventType.equals(EventType.COMMIT);
//        }
//    }
//
//    @Data
//    public static class CommitEvent extends Event {
//
//        private final CommitModel commit;
//
//        public CommitEvent(CommitModel commit) {
//            super(EventType.COMMIT);
//            this.commit = commit;
//        }
//
//        public Date getDate() {
//            return new Date(commit.getTime());
//        }
//
//        public static Function<CommitModel, CommitEvent> transformer =
//                (commitModel) -> new CommitEvent(commitModel);
//
//    }
//
//    @Data
//    public static class CommentEvent extends Event {
//
//        private final CommitComment comment;
//
//        public CommentEvent(CommitComment comment) {
//            super(EventType.COMMENT);
//            this.comment = comment;
//        }
//
//        @Override
//        public Date getDate() {
//            return comment.getTime();
//        }
//
//        public static Function<CommitComment, CommentEvent> transformer =
//                (comment) -> new CommentEvent(comment);
//
//    }
//
//    @Data
//    public static class CommentContextEvent extends Event {
//
//        private final SortedSet<CommitComment> comments;
//        private final DiffContext diffContext;
//
//        public CommentContextEvent(SortedSet<CommitComment> comments, DiffContext diffContext) {
//            super(EventType.COMMENT_CONTEXT);
//            this.comments = comments;
//            this.diffContext = diffContext;
//        }
//
//        @Override
//        public Date getDate() {
//            return comments.first().getTime();
//        }
//
//    }
//
//    class EventResolver {
//
//        private final RepositoryModel repositoryModel;
//        private final BranchModel branchModel;
//        private final DiffResponse diffResponse;
//        private final CommitModel mergebase;
//        private final List<CommitComment> comments;
//        private final Map<DiffModel, BlameModel> blames;
//
//        public EventResolver(DetailedRepositoryModel repositoryModel, BranchModel branchModel) throws ApiError {
//            BranchModel master = repositoryModel.getBranch("master");
//
//            this.repositoryModel = repositoryModel;
//            this.branchModel = branchModel;
//            this.blames = Maps.<DiffModel, BlameModel> newHashMap();
//
//            this.mergebase = gitBackend.mergeBase(repositoryModel, master.getCommit().getCommit(), branchModel.getCommit().getCommit());
//            this.diffResponse = gitBackend.fetchDiffs(repositoryModel, mergebase.getCommit(), branchModel.getCommit().getCommit());
//            List<String> commitIds = Lists.transform(diffResponse.getCommits(), CommitModel::getCommit);
//            this.comments = commentsDAO.getCommentsFor(commitIds);
//        }
//
//        @SneakyThrows
//        private BlameModel getBlameModel(DiffModel diffModel) {
//            BlameModel blame = blames.get(diffModel);
//            if(blame == null && (!diffModel.isAdded())) {
//                blame = gitBackend.blame(repositoryModel, mergebase, diffModel.getOldPath());
//                blames.put(diffModel, blame);
//            }
//            return blame;
//        }
//
//        public SortedSet<CommentContextEvent> getCommentContexts() {
//            SortedSet<CommentContextEvent> commentContextEvents = Sets.newTreeSet();
//            DiffBlameModel.BlameModelProvider provider = (diffModel) -> getBlameModel(diffModel);
//            DiffBlameModel diffBlameModel = DiffBlameModel.transform(provider, mergebase, branchModel.getCommit(), diffResponse);
//            return null;
//        }
//
//    }
//
//    private final CommitComments commentsDAO;
//    private final GitBackend gitBackend;
//
//    @Inject
//    public PullRequestBackend(CommitComments commentsDAO, GitBackend gitBackend) {
//        this.commentsDAO = commentsDAO;
//        this.gitBackend = gitBackend;
//    }
//
//    public SortedSet<Event> getEventsForPullRequest(PullRequest request) throws ApiError {
//        DetailedRepositoryModel repository = gitBackend.fetchRepositoryView(request.getGroup());
//        BranchModel branch = repository.getBranch(request.getBranchName());
//        CommitModel commit = branch.getCommit();
//        DiffResponse diffs = gitBackend.fetchDiffs(repository, branch);
//        return getEvents(repository, diffs, commit);
//    }
//
//    public SortedSet<Event> getEvents(RepositoryModel repository, DiffResponse diffs, CommitModel commit) throws ApiError {
////        DiffBlameModel model = DiffBlameModel.transform((diffModel) -> {
////            return null;
////        }, d)
////        Map<DiffModel, BlameModel> blames = Maps.newHashMap();
////        for(DiffModel diffModel : diffs.getDiffs()) {
////            if(!diffModel.isAdded()) {
////                BlameModel blame = gitBackend.blame(repository, commit, diffModel.getNewPath());
////            }
////        }
////
////        SortedSet<Event> events = Sets.newTreeSet();
////        diffs.getCommits().forEach(commitModel ->
////                events.add(new CommitEvent(commitModel)));
////
////        List<String> commitIds = Lists.transform(diffs.getCommits(), CommitModel::getCommit);
////        List<CommitComment> comments = commentsDAO.getCommentsFor(commitIds);
////
////        comments.parallelStream()
////            .collect(Collectors.groupingBy(CommitComment::getSource))
////            .entrySet().parallelStream().map(entry -> {
////                CommitComment.Source source = entry.getKey();
////                List<CommitComment> comments = entry.getValue();
////                DiffContext context =
////            })
//
//
////        for(CommitComment comment : comments)
////            events.add(new CommentEvent(comment));
//        return null;
////        return events;
//    }

}
