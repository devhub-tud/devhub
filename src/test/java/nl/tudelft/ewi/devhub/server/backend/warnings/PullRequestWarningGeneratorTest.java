package nl.tudelft.ewi.devhub.server.backend.warnings;

import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.GitUsageWarning;
import nl.tudelft.ewi.git.models.BranchModel;

import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(MockitoJUnitRunner.class)
public class PullRequestWarningGeneratorTest {

    private final static String COMMIT_ID = "abcd";
    private final static String OTHER_COMMIT_ID = "dcad";
    private final static String MASTER = "master";


	@Mock private GroupRepository groupRepository;
	@Mock private nl.tudelft.ewi.devhub.server.database.entities.Commit commitEntity;
    @Mock private Group group;
    @Mock private Commits commits;
    @Mock private RepositoriesApi repositories;
    @Mock private RepositoryApi repository;
    @Mock private CommitApi commitApi;
    @Mock private BranchApi branchApi;
    @Mock private BranchModel branchModel;
    @Mock private DetailedCommitModel repoCommit;
    @InjectMocks  private PullRequestWarningGenerator generator;

    private GitUsageWarning warning;

    @Before
    public void beforeTest() throws Exception {
        when(commitEntity.getCommitId()).thenReturn(COMMIT_ID);
		when(commitEntity.getRepository()).thenReturn(groupRepository);
		when(groupRepository.getRepositoryName()).thenReturn("");

        when(repositories.getRepository(anyString())).thenReturn(repository);
        when(commitApi.get()).thenReturn(repoCommit);
        when(repository.getBranch(MASTER)).thenReturn(branchApi);
        when(branchApi.getCommit()).thenReturn(commitApi);

        warning = new GitUsageWarning();
        warning.setCommit(commitEntity);
    }

    @Test
    public void testCommitWithSingleParent() {
        when(repoCommit.getCommit()).thenReturn(COMMIT_ID);
        when(repoCommit.getParents()).thenReturn(new String[] { "fe30124" });
        Set<GitUsageWarning> warns = generator.generateWarnings(commitEntity, null);
        assertThat(warns, contains(warning));
    }

    @Test
    public void testMergeCommit() {
        when(repoCommit.getCommit()).thenReturn(COMMIT_ID);
        when(repoCommit.getParents()).thenReturn(new String[] { "fe30124", "ab30124" });
        Set<GitUsageWarning> warns = generator.generateWarnings(commitEntity, null);
        assertEquals(warns, Collections.<GitUsageWarning>emptySet());
    }

    @Test
    public void testCommitNotOnHead() {
        when(repoCommit.getCommit()).thenReturn(OTHER_COMMIT_ID);
        Set<GitUsageWarning> warns = generator.generateWarnings(commitEntity, null);
        assertEquals(warns, Collections.<GitUsageWarning>emptySet());
    }

    @Test
    public void testMergeCommitNotOnHead() {
        when(repoCommit.getCommit()).thenReturn(OTHER_COMMIT_ID);
        Set<GitUsageWarning> warns = generator.generateWarnings(commitEntity, null);
        assertEquals(warns, Collections.<GitUsageWarning>emptySet());
    }

}
