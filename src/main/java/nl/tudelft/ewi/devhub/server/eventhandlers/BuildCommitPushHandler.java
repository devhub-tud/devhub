package nl.tudelft.ewi.devhub.server.eventhandlers;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.backend.PullRequestBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.RepositoriesController;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.util.CommitIterator;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@Singleton
public class BuildCommitPushHandler {

    @Inject private Provider<BuildsBackend> buildsBackendProvider;
    @Inject private Provider<Commits> commitsProvider;
    @Inject private Provider<RepositoriesController> repositoriesControllerProvider;
    @Inject private Provider<RepositoriesApi> repositoriesApiProvider;


    @Subscribe
    public void buildCommits(GitPush gitPush) {
        RepositoryApi repositoryApi = repositoriesApiProvider.get().getRepository(gitPush.getRepository());
        DetailedRepositoryModel repositoryModel = repositoryApi.getRepositoryModel();
        RepositoryEntity repositoryEntity = repositoriesControllerProvider.get().find(gitPush.getRepository());

        Set<Commit> commitsToBeBuilt =
            // For every branch
            repositoryModel.getBranches().stream().sequential()
                // Get the head
                .map(BranchModel::getCommit)
                // Ensure the head commit exists in the database
                .map(commitModel -> commitsProvider.get().ensureExists(repositoryEntity, commitModel.getCommit()))
                // Pick the first 3 unbuild commits using BFS
                .flatMap(commit -> CommitIterator.stream(commit, Commit::hasNoBuildResult).limit(3))
                // Limit the results
                .limit(20)
                // Get the unique commits
                .collect(Collectors.toSet());

        log.info("Building commits {}", commitsToBeBuilt);
        commitsToBeBuilt.stream()
            .forEach(buildsBackendProvider.get()::buildCommit);
    }

}
