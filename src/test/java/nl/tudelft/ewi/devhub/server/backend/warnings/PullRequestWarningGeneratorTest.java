package nl.tudelft.ewi.devhub.server.backend.warnings;

import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.GitUsageWarning;
import nl.tudelft.ewi.git.client.Branch;
import nl.tudelft.ewi.git.client.Commit;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.CommitModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(MockitoJUnitRunner.class)
public class PullRequestWarningGeneratorTest {

    private final static String COMMIT_ID = "abcd";
    private final static String OTHER_COMMIT_ID = "dcad";
    private final static String MASTER = "master";

    @Mock private nl.tudelft.ewi.devhub.server.database.entities.Commit commitEntity;
    @Mock private Group group;
    @Mock private Commit commit;
    @Mock private Repository repository;
    @Mock private Repositories repositories;
    @Mock private GitServerClient gitServerClient;
    @Mock private Branch branch;
    @Mock private CommitModel commitModel;
    @InjectMocks  private PullRequestWarningGenerator generator;

    private GitUsageWarning warning;

    @Before
    public void beforeTest() throws Exception {
        when(commitEntity.getCommitId()).thenReturn(COMMIT_ID);
        when(commitEntity.getRepository()).thenReturn(group);
        when(group.getRepositoryName()).thenReturn("abc");
        when(gitServerClient.repositories()).thenReturn(repositories);
        when(repositories.retrieve(anyString())).thenReturn(repository);
        when(repository.retrieveCommit(COMMIT_ID)).thenReturn(commit);
        when(repository.retrieveBranch(MASTER)).thenReturn(branch);
        when(branch.getCommit()).thenReturn(commitModel);

        warning = new GitUsageWarning();
        warning.setCommit(commitEntity);
    }

    @Test
    public void testCommitWithSingleParent() {
        when(commitModel.getCommit()).thenReturn(COMMIT_ID);
        when(commitModel.getParents()).thenReturn(new String[] { "fe30124" });
        Set<GitUsageWarning> warns = generator.generateWarnings(commitEntity, null);
        assertThat(warns, contains(warning));
    }

    @Test
    public void testMergeCommit() {
        when(commitModel.getCommit()).thenReturn(COMMIT_ID);
        when(commitModel.getParents()).thenReturn(new String[] { "fe30124", "ab30124" });
        Set<GitUsageWarning> warns = generator.generateWarnings(commitEntity, null);
        assertEquals(warns, Collections.<GitUsageWarning>emptySet());
    }

    @Test
    public void testCommitNotOnHead() {
        when(commitModel.getCommit()).thenReturn(OTHER_COMMIT_ID);
        when(commitModel.getParents()).thenReturn(new String[] { "fe30124" });
        Set<GitUsageWarning> warns = generator.generateWarnings(commitEntity, null);
        assertEquals(warns, Collections.<GitUsageWarning>emptySet());
    }

    @Test
    public void testMergeCommitNotOnHead() {
        when(commitModel.getCommit()).thenReturn(OTHER_COMMIT_ID);
        when(commitModel.getParents()).thenReturn(new String[] { "fe30124", "ab30124" });
        Set<GitUsageWarning> warns = generator.generateWarnings(commitEntity, null);
        assertEquals(warns, Collections.<GitUsageWarning>emptySet());
    }

}
