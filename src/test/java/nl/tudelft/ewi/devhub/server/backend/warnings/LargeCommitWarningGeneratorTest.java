package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LargeCommitWarningGeneratorTest {

    private LargeCommitWarningGenerator generator;
    private Set<LargeCommitWarning> testEquals;
    private PrivateRepository repository;
    private Commit commit;
    private DiffModel diffModel;

    private static final String REPOSITORY_NAME = "John Cena";
    private static final String COMMIT_ID = "1";

    @Mock private RepositoriesApi repositoriesApi;
    @Mock private RepositoryApi repositoryApi;
    @Mock private CommitApi commitApi;
    @Mock private GitPush gitPush;

    @Before
    public void setUp() {
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

        /* I will convert this to a JSON resource */
        AbstractDiffModel.DiffLine diffLine = new AbstractDiffModel.DiffLine();
        diffLine.setNewLineNumber(1);
        diffLine.setContent("Hiephoi");

        AbstractDiffModel.DiffContext<AbstractDiffModel.DiffLine> diffContext = new AbstractDiffModel.DiffContext<>();
        diffContext.setLines(Lists.newArrayList(diffLine));
        AbstractDiffModel.DiffFile<AbstractDiffModel.DiffContext<AbstractDiffModel.DiffLine>> diffFile = new AbstractDiffModel.DiffFile<>();
        diffFile.setContexts(Lists.newArrayList(diffContext));
        diffModel.setDiffs(Lists.newArrayList(diffFile));

        System.out.println(diffModel);

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
    public void testTooManyFiles() {
        Set<LargeCommitWarning> warnings = generator.generateWarnings(commit, gitPush);

        assertEquals(1, warnings.size());
        assertEquals(testEquals, warnings);
    }

    /**
     * Confirms that the warning is generated when there are too many lines committed.
     */
    @Test
    public void testTooManyLineChanges() {
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
