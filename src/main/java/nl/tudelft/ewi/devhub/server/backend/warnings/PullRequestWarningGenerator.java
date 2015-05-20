package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.common.collect.Lists;

import com.google.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.GitUsageWarning;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.models.CommitModel;

import java.util.List;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class PullRequestWarningGenerator implements CommitWarningGenerator<GitUsageWarning> {

    private final GitServerClient gitServerClient;

    @Inject
    public PullRequestWarningGenerator(GitServerClient gitServerClient) {
        this.gitServerClient = gitServerClient;
    }

    @Override
    public List<GitUsageWarning> generateWarnings(final Commit commitEntity) {
        final List<GitUsageWarning> warnings = Lists.newArrayList();
        val group = commitEntity.getRepository();

        try {
            if(commitOnMaster(group, commitEntity.getCommitId())) {
                warnings.add(new GitUsageWarning());
            }
        }
        catch (GitClientException e) {
            log.warn(e.getMessage(), e);
        }

        return warnings;
    }

    protected CommitModel getMasterCommit(final Group group) throws GitClientException {
        val repository = gitServerClient.repositories().retrieve(group.getRepositoryName());
        val master = repository.retrieveBranch("master");
        return master.getCommit();
    }

    protected boolean commitOnMaster(final Group group, final String commitId) throws GitClientException {
        val commitModel = getMasterCommit(group);
        return headIsMaster(commitModel, commitId) && headIsNoMerge(commitModel);
    }

    protected boolean headIsMaster(final CommitModel commitModel, final String commitId) {
        return commitModel.getCommit().equals(commitId);
    }

    protected boolean headIsNoMerge(final CommitModel commitModel) {
        return commitModel.getParents().length <= 1;
    }

}
