package nl.tudelft.ewi.devhub.server.eventhandlers;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.warnings.CommitPushWarningGenerator;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.RepositoriesController;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;
import nl.tudelft.ewi.devhub.server.events.CreateCommitEvent;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@Singleton
public class CommitWarningGenerator {

    @Inject
    private Provider<RepositoriesApi> repositoriesApiProvider;

    @Inject
    private Provider<Commits> commitsProvider;

    @Inject
    private Provider<RepositoriesController> repositoriesControllerProvider;

    @Inject
    private Provider<Set<CommitPushWarningGenerator>> commitWarningGeneratorProvider;

    @Inject
    private Provider<Warnings> warningsProvider;

    @Subscribe
    public void enhanceCommitWithinTransaction(CreateCommitEvent createCommitEvent) {
        RepositoryEntity repositoryEntity = repositoriesControllerProvider.get().find(createCommitEvent.getRepositoryName());
        Commit commit = commitsProvider.get().ensureExists(repositoryEntity, createCommitEvent.getCommitId());

        GitPush gitPush = new GitPush();
        gitPush.setRepository(createCommitEvent.getRepositoryName());

        log.info("Generating warnings for {} {}", commit.getRepository().getRepositoryName(), commit.getCommitId());
        triggerWarnings(repositoryEntity, commit, gitPush);
    }

    @Transactional
    protected void triggerWarnings(final RepositoryEntity repositoryEntity, final Commit commit, final GitPush gitPush) {
        Set<? extends CommitWarning> pushWarnings = commitWarningGeneratorProvider.get().stream().sequential()
            .flatMap(generator -> {
                try {
                    Set<? extends CommitWarning> commitWarningList = generator.generateWarnings(commit, gitPush);
                    return commitWarningList.stream();
                }
                catch (Exception e) {
                    log.warn("Failed to generate warnings with {} for {} ", generator, commit);
                    log.warn(e.getMessage(), e);
                    return Stream.empty();
                }
            })
            .collect(Collectors.toSet());

        Set<? extends CommitWarning> persistedWarnings = warningsProvider.get().persist(repositoryEntity, pushWarnings);
        log.info("Persisted {} of {} push warnings for {}", persistedWarnings.size(),
            pushWarnings.size(), repositoryEntity);
    }

}
