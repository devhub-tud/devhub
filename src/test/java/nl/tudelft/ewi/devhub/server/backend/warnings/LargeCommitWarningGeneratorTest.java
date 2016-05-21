package nl.tudelft.ewi.devhub.server.backend.warnings;

import nl.tudelft.ewi.devhub.server.database.Configurable;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.models.AbstractDiffModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LargeCommitWarningGeneratorTest {

    private LargeCommitWarningGenerator generator;

    private static final String MAX_FILES_PROPERTY = "warnings.max-touched-files";
    private static final int MAX_AMOUNT_OF_FILES = 10;
    private static final String REPOSITORY_NAME = "John Cena";
    private static final String COMMIT_ID = "1";

    @Mock private RepositoriesApi repositoriesApi;
    @Mock private Commit commit;
    @Mock private RepositoryEntity configurable;
    @Mock private List<AbstractDiffModel.DiffFile<AbstractDiffModel.DiffContext<AbstractDiffModel.DiffLine>>> diffFiles;
    @Mock private GitPush gitPush;
    @Mock private List<AbstractDiffModel.DiffFile<AbstractDiffModel.DiffContext<AbstractDiffModel.DiffLine>>> diffs;
    @Mock private AbstractCommitWarningGenerator abstractCommitWarningGenerator;
    @Mock private CommitApi commitApi;
    @Mock private DiffModel diffModel;
    @Mock private RepositoryApi repositoryApi;

    @Before
    public void setUp() {
        generator = new LargeCommitWarningGenerator(repositoriesApi);
        when(commit.getRepository()).thenReturn(configurable);
        when(commit.getRepository().getRepositoryName()).thenReturn(REPOSITORY_NAME);
        when(commit.getCommitId()).thenReturn(COMMIT_ID);
        when(abstractCommitWarningGenerator.getRepository(commit)).thenReturn(repositoryApi);
        when(abstractCommitWarningGenerator.getGitCommit(commit)).thenReturn(commitApi);
        when(repositoryApi.getCommit(COMMIT_ID)).thenReturn(commitApi);
        when(commitApi.diff()).thenReturn(diffModel);
        when(diffModel.getDiffs()).thenReturn(diffs);
        when(repositoriesApi.getRepository(REPOSITORY_NAME)).thenReturn(repositoryApi);
    }

    @Test
    public void testIsMerge() {
        when(commit.isMerge()).thenReturn(true);
        Set empty = generator.generateWarnings(commit, gitPush);
        assertTrue(empty.isEmpty());
    }
}
