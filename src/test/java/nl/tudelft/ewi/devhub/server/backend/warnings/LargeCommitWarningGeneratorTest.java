package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.PrivateRepository;
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

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LargeCommitWarningGeneratorTest {

    private LargeCommitWarningGenerator generator;
    private Set<LargeCommitWarning> testEquals;
    private PrivateRepository repository;
    private Commit commit;
    private DiffModel diffModel;
    private AbstractDiffModel.DiffFile diffFile;

    private static final String REPOSITORY_NAME = "John Cena";
    private static final String COMMIT_ID = "1";
    private static final int MAX_AMOUNT_OF_FILES = 10;
    private static final int MAX_AMOUNT_OF_LINES_TOUCHED = 500;

    @Mock private RepositoriesApi repositoriesApi;
    @Mock private RepositoryApi repositoryApi;
    @Mock private CommitApi commitApi;
    @Mock private GitPush gitPush;

    @Before
    public void setUp() throws IOException {
        generator = new LargeCommitWarningGenerator(repositoriesApi);

        repository = new PrivateRepository();
        repository.setRepositoryName(REPOSITORY_NAME);

        commit = new Commit();
        commit.setRepository(repository);
        commit.setCommitId(COMMIT_ID);
        commit.setParents(Lists.newArrayList());

        diffModel = new DiffModel();

        when(repositoriesApi.getRepository(anyString())).thenReturn(repositoryApi);
        when(repositoryApi.getCommit(anyString())).thenReturn(commitApi);
        when(commitApi.diff()).thenReturn(diffModel);

        ObjectMapper mapper = new ObjectMapper();
        diffFile = mapper.readValue(
                LargeCommitWarningGeneratorTest.class.getResourceAsStream("/test-diff.json"),
                AbstractDiffModel.DiffFile.class
        );
        diffModel.setDiffs(Lists.newArrayList(diffFile));

        /* Set up a warning, used to test if the generator returns an equal object */
        LargeCommitWarning warning = new LargeCommitWarning();
        warning.setCommit(commit);
        testEquals = Sets.newHashSet(warning);
    }

    /**
     * Confirms that the warning is not generated when the commit is a merge.
     */
    @Test
    public void testIsMerge() {
        commit.setParents(Lists.newArrayList(commit, commit));
        Set<LargeCommitWarning> empty = generator.generateWarnings(commit, gitPush);
        assertThat(empty, is(Sets.newHashSet()));
    }

    /**
     * Confirms that the warning is generated when there are too many files committed.
     */
    @Test
    public void testTooManyFiles() throws IOException {
        List<AbstractDiffModel.DiffFile<AbstractDiffModel.DiffContext<AbstractDiffModel.DiffLine>>> diffs = Lists.newArrayList();
        for (int i = 0; i < MAX_AMOUNT_OF_FILES + 2; i++) {
            diffs.add(diffFile);
        }
        diffModel.setDiffs(diffs);
        Set<LargeCommitWarning> warnings = generator.generateWarnings(commit, gitPush);

        assertEquals(1, warnings.size());
        assertEquals(testEquals, warnings);
    }

    /**
     * Confirms that the warning is generated when there are too many lines committed.
     */
    @Test
    public void testTooManyLineChanges() {
        List<AbstractDiffModel.DiffLine> diffLines = Lists.newArrayList();
        for (int i = 0; i < MAX_AMOUNT_OF_LINES_TOUCHED + 2; i++) {
            AbstractDiffModel.DiffLine diffLine = new AbstractDiffModel.DiffLine(null, i, String.valueOf(i));
            diffLines.add(diffLine);
        }
        diffModel.getDiffs().get(0).getContexts().get(0).setLines(diffLines);
        Set<LargeCommitWarning> warnings = generator.generateWarnings(commit, gitPush);

        assertEquals(testEquals, warnings);
    }

    /**
     * Confirms that no warning is generated when the commit is deemed good.
     */
    @Test
    public void testGoodCommit() {
        Set<LargeCommitWarning> warnings = generator.generateWarnings(commit, gitPush);

        assertTrue(warnings.isEmpty());
    }
}
