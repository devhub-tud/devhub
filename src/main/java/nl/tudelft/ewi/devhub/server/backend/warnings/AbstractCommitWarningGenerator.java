package nl.tudelft.ewi.devhub.server.backend.warnings;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;

import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

import javax.ws.rs.NotFoundException;

/**
 * @author Liam Clark
 */
@Slf4j
public abstract class AbstractCommitWarningGenerator<T extends CommitWarning, A> implements CommitWarningGenerator<T, A> {

    protected final RepositoriesApi repositoriesApi;

    protected AbstractCommitWarningGenerator(RepositoriesApi repositoriesApi) {
        this.repositoriesApi = repositoriesApi;
    }

    /**
     * Get CommitApi for commit.
     * @param commit Commit to interact with.
     * @return a CommitApi for the Commit.
     */
    protected CommitApi getGitCommit(Commit commit) {
        try {
            return getRepository(commit)
                .getCommit(commit.getCommitId());
        }
        catch (NotFoundException e) {
            log.warn(
                String.format(
                    "Failed to retrieve commit %s in git server, failed with %s",
                    commit,
                    e.getMessage()
                ),
                e
            );
            throw e;
        }
    }

    /**
     * Get the RepositoryApi for the repository of a Commit.
     * @param commit Commit to get the RepositoryApi for.
     * @return The RepositoryApi.
     */
    protected RepositoryApi getRepository(Commit commit) {
        try {
            return repositoriesApi
               .getRepository(commit.getRepository().getRepositoryName());
        }
        catch (NotFoundException e) {
            log.warn(
                String.format(
                    "Failed to retrieve repository %s in git server, failed with %s",
                    commit.getRepository(),
                    e.getMessage()
                ),
                e
            );
            throw e;
        }
    }

}
