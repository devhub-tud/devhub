package nl.tudelft.ewi.devhub.server.backend.warnings;

import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;

import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

/**
 * @author Liam Clark
 */
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
        return getRepository(commit)
            .getCommit(commit.getCommitId());
    }

    /**
     * Get the RepositoryApi for the repository of a Commit.
     * @param commit Commit to get the RepositoryApi for.
     * @return The RepositoryApi.
     */
    protected RepositoryApi getRepository(Commit commit) {
        return repositoriesApi
           .getRepository(commit.getRepository().getRepositoryName());
    }

}
