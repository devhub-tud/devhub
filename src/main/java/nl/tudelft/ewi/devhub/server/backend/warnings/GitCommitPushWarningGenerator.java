package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;

/**
 * Created by LC on 30/05/15.
 */
public abstract class GitCommitPushWarningGenerator<T extends CommitWarning> implements CommitPushWarningGenerator<T> {
    @Inject
    private GitServerClient gitServerClient;

    public nl.tudelft.ewi.git.client.Commit getGitCommit(Commit commit,GitPush attachment) throws GitClientException {
        return gitServerClient.repositories()
                .retrieve(attachment.getRepository())
                .retrieveCommit(commit.getCommitId());
    }
}
