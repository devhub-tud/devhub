package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repository;

/**
 * @author Liam Clark
 */
public abstract class GitCommitPushWarningGenerator<T extends CommitWarning> implements CommitPushWarningGenerator<T> {

    protected final GitServerClient gitServerClient;

    @Inject
    public GitCommitPushWarningGenerator(GitServerClient gitServerClient) {
        this.gitServerClient = gitServerClient;
    }

    protected nl.tudelft.ewi.git.client.Commit getGitCommit(Commit commit, GitPush attachment) throws GitClientException {
        return gitServerClient.repositories()
                .retrieve(attachment.getRepository())
                .retrieveCommit(commit.getCommitId());
    }

    protected Repository getRepository(Commit commit) throws GitClientException {
        return  gitServerClient.repositories().retrieve(commit.getRepository().getRepositoryName());
    }
}
