package nl.tudelft.ewi.devhub.server.eventhandlers;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.RepositoriesController;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.events.CreateCommitEvent;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@Singleton
public class CommitEnhancer {

    @Inject
    Provider<RepositoriesApi> repositoriesApiProvider;

    @Inject
    Provider<Commits> commitsProvider;

    @Inject
    Provider<RepositoriesController> repositoriesControllerProvider;

    @Subscribe
    @SuppressWarnings("unused")
    public void enhanceCommitSafely(CreateCommitEvent createCommitEvent) {
        try {
            enhanceCommitWithinTransaction(createCommitEvent);
        }
        catch (Exception e) {
            log.warn("Failed to retrieve commit details: " + e.getMessage(), e);
        }
    }

    @Transactional
    protected void enhanceCommitWithinTransaction(CreateCommitEvent createCommitEvent) {
        RepositoryEntity repositoryEntity = repositoriesControllerProvider.get().find(createCommitEvent.getRepositoryName());
        Commit commit = commitsProvider.get().ensureExists(repositoryEntity, createCommitEvent.getCommitId());

        log.info("Enhance {} {}", commit.getRepository().getRepositoryName(), commit.getCommitId());

        final CommitModel gitCommit = retrieveCommit(repositoryEntity, createCommitEvent.getCommitId());
        final DiffModel diffModel = retrieveDiffModel(repositoryEntity, createCommitEvent.getCommitId());
        commit.setLinesAdded(diffModel.getLinesAdded());
        commit.setLinesRemoved(diffModel.getLinesRemoved());
        commit.setCommitTime(new Date(gitCommit.getTime() * 1000));
        commit.setAuthor(gitCommit.getAuthor());
        commit.setParents(
            Stream.of(gitCommit.getParents()).sequential()
                .map(c -> commitsProvider.get().ensureExists(repositoryEntity, c))
                .collect(Collectors.toList())
        );
    }

    @SneakyThrows
    protected CommitModel retrieveCommit(RepositoryEntity repositoryEntity, String commitId) {
        return repositoriesApiProvider.get().getRepository(repositoryEntity.getRepositoryName())
            .getCommit(commitId)
            .get();
    }

    @SneakyThrows
    protected DiffModel retrieveDiffModel(RepositoryEntity repositoryEntity, String commitId) {
        return repositoriesApiProvider.get().getRepository(repositoryEntity.getRepositoryName())
            .getCommit(commitId)
            .diff();
    }


}
