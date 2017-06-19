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
public class ClosePullRequestsPushHandler {

    @Inject private Provider<RepositoriesController> repositoriesControllerProvider;
    @Inject private Provider<RepositoriesApi> repositoriesApiProvider;
    @Inject private Provider<PullRequestBackend> pullRequestBackendProvider;
    @Inject private Provider<PullRequests> pullRequestsProvider;


    @Subscribe
    public void buildCommits(GitPush gitPush) {
        RepositoryApi repositoryApi = repositoriesApiProvider.get().getRepository(gitPush.getRepository());
        RepositoryEntity repositoryEntity = repositoriesControllerProvider.get().find(gitPush.getRepository());

        log.info("Find open pull requests for repository {}", repositoryEntity);
        pullRequestsProvider.get().findOpenPullRequests(repositoryEntity)
            .forEach(pullRequest -> pullRequestBackendProvider.get().updatePullRequest(repositoryApi, pullRequest));
    }

}
