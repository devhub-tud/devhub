package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.common.collect.Sets;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LargeCommitWarning;
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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LargeCommitWarningGeneratorTest {

    private LargeCommitWarningGenerator generator;
    private Set<LargeCommitWarning> testEquals;

    private static final String REPOSITORY_NAME = "John Cena";
    private static final String COMMIT_ID = "1";

    @Mock private RepositoriesApi repositoriesApi;
    @Mock private Commit commit;
    @Mock private RepositoryEntity configurable;
    @Mock private GitPush gitPush;
    @Mock private List<AbstractDiffModel.DiffFile<AbstractDiffModel.DiffContext<AbstractDiffModel.DiffLine>>> diffs;
    @Mock private AbstractCommitWarningGenerator abstractCommitWarningGenerator;
    @Mock private CommitApi commitApi;
    @Mock private DiffModel diffModel;
    @Mock private RepositoryApi repositoryApi;
    @Mock private Collection collection;
    @Mock private Stream<AbstractDiffModel.DiffFile<AbstractDiffModel.DiffContext<AbstractDiffModel.DiffLine>>> stream;
    @Mock private Stream<Object> objectStream;

    @Before
    public void setUp() {
        generator = new LargeCommitWarningGenerator(repositoriesApi);

        /* Build the mocked commit */
        when(commit.getRepository()).thenReturn(configurable);
        when(commit.getRepository().getRepositoryName()).thenReturn(REPOSITORY_NAME);
        when(commit.getCommitId()).thenReturn(COMMIT_ID);
        when(commit.isMerge()).thenReturn(false);

        when(abstractCommitWarningGenerator.getRepository(commit)).thenReturn(repositoryApi);
        when(abstractCommitWarningGenerator.getGitCommit(commit)).thenReturn(commitApi);

        /* Build the mocked repo */
        when(repositoriesApi.getRepository(REPOSITORY_NAME)).thenReturn(repositoryApi);
        when(repositoryApi.getCommit(COMMIT_ID)).thenReturn(commitApi);
        when(commitApi.diff()).thenReturn(diffModel);
        when(diffModel.getDiffs()).thenReturn(diffs);

        /* Used to prevent NullPointerExceptions in tooManyLineChanges */
        when(diffs.stream()).thenReturn(stream);
        when(stream.filter(anyObject())).thenReturn(stream);
        when(stream.flatMap(anyObject())).thenReturn(objectStream);
        when(objectStream.flatMap(anyObject())).thenReturn(objectStream);
        when(objectStream.filter(anyObject())).thenReturn(objectStream);

        /* Set up a warning, used to test if the generator returns an equal object */
        LargeCommitWarning warning = new LargeCommitWarning();
        warning.setCommit(commit);
        testEquals = Sets.newHashSet(warning);
    }

    @Test
    public void testIsMerge() {
        when(commit.isMerge()).thenReturn(true);
        Set<LargeCommitWarning> empty = generator.generateWarnings(commit, gitPush);
        assertTrue(empty.isEmpty());
    }

    @Test
    public void testTooManyFiles() {
        when(diffs.size()).thenReturn(1);
        when(configurable.getIntegerProperty(anyString(), anyString())).thenReturn(0);

        Set<LargeCommitWarning> warnings = generator.generateWarnings(commit, gitPush);

        verify(diffs).size();

        assertFalse(warnings.isEmpty());
        assertEquals(1, warnings.size());
        assertEquals(testEquals, warnings);
    }

    @Test
    public void testTooManyLineChanges() {
        when(objectStream.count()).thenReturn((long) 1);
        when(configurable.getIntegerProperty(anyString(), anyString())).thenReturn(0);

        Set<LargeCommitWarning> warnings = generator.generateWarnings(commit, gitPush);

        assertFalse(warnings.isEmpty());
        assertEquals(1, warnings.size());
        assertEquals(testEquals, warnings);
    }

}
