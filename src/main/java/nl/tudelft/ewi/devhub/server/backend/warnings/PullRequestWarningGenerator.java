package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.GitUsageWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.client.Branch;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.CommitModel;

import java.util.Set;

/**
 * The {@code PullRequestWarningGenerator} checks for every push hookif the newest commit
 * on the master is a merge or not.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class PullRequestWarningGenerator extends AbstractCommitWarningGenerator<GitUsageWarning, GitPush>
implements CommitPushWarningGenerator<GitUsageWarning> {

    @Inject
    public PullRequestWarningGenerator(GitServerClient gitServerClient) {
        super(gitServerClient);
    }

    @Override
    public Set<GitUsageWarning> generateWarnings(final Commit commitEntity, final GitPush attachment) {
        log.debug("Started generating warnings for {} in {}", commitEntity, this);
        final Set<GitUsageWarning> warnings = Sets.newHashSet();

        try {
            if(commitOnMaster(commitEntity)) {
                GitUsageWarning warning = new GitUsageWarning();
                warning.setCommit(commitEntity);
                warnings.add(warning);
            }
        }
        catch (GitClientException e) {
            log.warn(e.getMessage(), e);
        }

        log.debug("Finished generating warnings for {} in {}", commitEntity, this);
        return warnings;
    }

    protected CommitModel getMasterCommit(final Commit commit) throws GitClientException {
        Repository repository = getRepository(commit);
        Branch master = repository.retrieveBranch("master");
        return master.getCommit();
    }

    protected boolean commitOnMaster(final Commit commit) throws GitClientException {
        CommitModel commitModel = getMasterCommit(commit);
        return headIsMaster(commitModel, commit.getCommitId()) && headIsNoMerge(commitModel);
    }

    protected boolean headIsMaster(final CommitModel commitModel, final String commitId) {
        return commitModel.getCommit().equals(commitId);
    }

    protected boolean headIsNoMerge(final CommitModel commitModel) {
        return commitModel.getParents().length <= 1;
    }

}
