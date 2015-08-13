package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.inject.Inject;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repository;

/**
 * @author Liam Clark
 */
public abstract class AbstractCommitWarningGenerator<T extends CommitWarning, A> implements CommitWarningGenerator<T, A> {

    protected final GitServerClient gitServerClient;

    @Inject
    public AbstractCommitWarningGenerator(GitServerClient gitServerClient) {
        this.gitServerClient = gitServerClient;
    }

    @SneakyThrows
    protected nl.tudelft.ewi.git.client.Commit getGitCommit(Commit commit) {
        return gitServerClient.repositories()
            .retrieve(commit.getRepository().getRepositoryName())
            .retrieveCommit(commit.getCommitId());
    }

    @SneakyThrows
    protected Repository getRepository(Commit commit) {
        return  gitServerClient.repositories()
            .retrieve(commit.getRepository().getRepositoryName());
    }

}
