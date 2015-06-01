package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.GitUsageWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.client.Branch;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repositories;
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
public class PullRequestWarningGenerator implements CommitPushWarningGenerator<GitUsageWarning> {

    private final GitServerClient gitServerClient;

    @Inject
    public PullRequestWarningGenerator(GitServerClient gitServerClient) {
        this.gitServerClient = gitServerClient;
    }

    @Override
    public Set<GitUsageWarning> generateWarnings(final Commit commitEntity, final GitPush attachment) {
        final Set<GitUsageWarning> warnings = Sets.newHashSet();
        Group group = commitEntity.getRepository();

        try {
            if(commitOnMaster(group, commitEntity.getCommitId())) {
                GitUsageWarning warning = new GitUsageWarning();
                warning.setCommit(commitEntity);
                warnings.add(warning);
            }
        }
        catch (GitClientException e) {
            log.warn(e.getMessage(), e);
        }

        return warnings;
    }

    protected CommitModel getMasterCommit(final Group group) throws GitClientException {
        String reponame = group.getRepositoryName();
        Repositories repositories = gitServerClient.repositories();
        Repository repository = repositories.retrieve(reponame);
        Branch master = repository.retrieveBranch("master");
        return master.getCommit();
    }

    protected boolean commitOnMaster(final Group group, final String commitId) throws GitClientException {
        CommitModel commitModel = getMasterCommit(group);
        return headIsMaster(commitModel, commitId) && headIsNoMerge(commitModel);
    }

    protected boolean headIsMaster(final CommitModel commitModel, final String commitId) {
        return commitModel.getCommit().equals(commitId);
    }

    protected boolean headIsNoMerge(final CommitModel commitModel) {
        return commitModel.getParents().length <= 1;
    }

}
