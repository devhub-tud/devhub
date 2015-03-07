package nl.tudelft.ewi.devhub.server.web.view.models;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import liquibase.precondition.Precondition;
import lombok.Data;
import lombok.experimental.Accessors;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.GitBackend;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.models.*;

import java.util.List;
import java.util.Map;

/**
 * The DiffViewModel contains all the required data to create a diff page
 */
@Data
public class DiffViewModel {

    // The CommentChecker can be used to retrieve any comments for a line
    private final CommentBackend.CommentChecker commentChecker;

    // The new commit
    private final CommitModel newCommit;

    // The old commit, may be null (in case of initial commit)
    // For branches, this is the merge base (first common commit between branches)
    private final CommitModel oldCommit;

    // The actual diff response
    private final DiffResponse diffResponse;

    // Blame models for the changed files
    private final Map<DiffModel, BlameModel> blames;

    public BlameModel getBlameModel(DiffModel diffModel) {
        return blames.get(diffModel);
    }

    public static DiffViewModelBuilder builder(GitBackend gitBackend, CommentBackend commentBackend) {
        return new DiffViewModelBuilder(gitBackend, commentBackend);
    }

    @Data
    @Accessors(chain = true)
    public static class DiffViewModelBuilder {

        private final GitBackend gitBackend;

        private final CommentBackend commentBackend;

        private RepositoryModel repository;

        private CommitModel newCommit;

        private CommitModel oldCommit;

        /**
         * Build the DiffViewModel
         *  This method invokes the required GitBackend calls to retrieve required data
         * @return
         *  the DiffViewModel
         * @throws ApiError
         *  If an ApiError occurs
         */
        public DiffViewModel build() throws ApiError {
            Preconditions.checkNotNull(repository);
            Preconditions.checkNotNull(newCommit);

            String oldCommitId = oldCommit != null ? oldCommit.getCommit() : null;
            DiffResponse diffResponse = gitBackend.fetchDiffs(repository, oldCommitId, newCommit.getCommit());
            Map<DiffModel, BlameModel> blames = fetchBlames(diffResponse.getDiffs());
            List<String> commits = Lists.transform(diffResponse.getCommits(), (commitModel) -> commitModel.getCommit());
            CommentBackend.CommentChecker commentChecker = commentBackend.getCommentChecker(commits);
            return new DiffViewModel(commentChecker, newCommit, oldCommit, diffResponse, blames);
        }

        private Map<DiffModel, BlameModel> fetchBlames(List<DiffModel> diffModels) throws ApiError {
            Map<DiffModel, BlameModel> blames = Maps.newHashMap();
            for(DiffModel diffModel : diffModels) {
                // Skip added files as we already know in which commit their lines are introduced
                if(!diffModel.isAdded()) {
                    // Fetch the blame model
                    BlameModel blame = gitBackend.blame(repository, oldCommit, diffModel.getOldPath());
                    blames.put(diffModel, blame);
                }
            }
            return blames;
        }

    }

}
