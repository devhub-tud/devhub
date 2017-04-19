package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.PrivateRepository;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LargeFileWarning;
import nl.tudelft.ewi.git.models.*;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by Douwe Koopmans on 18-5-16.
 */
@RunWith(MockitoJUnitRunner.class)
public class LargeFileWarningGeneratorTest {

    private final static String COMMIT_ID = "abcd";
    private static final String FILE_PATH = "foobar.txt";

    private Commit commit;
    private PrivateRepository privateRepository;
    private AbstractDiffModel.DiffFile<AbstractDiffModel.DiffContext<AbstractDiffModel.DiffLine>> diffFile;
    private List<AbstractDiffModel.DiffFile<AbstractDiffModel.DiffContext<AbstractDiffModel.DiffLine>>> diffValues;
    private LargeFileWarning warning;

    @InjectMocks private LargeFileWarningGenerator generator;
    @Mock private RepositoriesApi repositories;
    @Mock private RepositoryApi repository;
    @Mock private CommitApi commitApi;
    @Mock private DetailedCommitModel commitModel;

    @Mock private DiffModel diffModel;

    @Before
    public void setUp() throws Exception {
        privateRepository = new PrivateRepository();
        privateRepository.setRepositoryName("");
        privateRepository.setProperties(ImmutableMap.of(LargeFileWarningGenerator.MAX_FILE_SIZE_PROPERTY, "5"));
        commit = new Commit();
        commit.setCommitId(COMMIT_ID);
        commit.setRepository(privateRepository);

        diffFile = new AbstractDiffModel.DiffFile<>();
        diffFile.setType(ChangeType.ADD);
        diffFile.setNewPath(FILE_PATH);
        diffValues = Lists.newArrayList(diffFile);

        when(repositories.getRepository(anyString())).thenReturn(repository);
        when(repository.getCommit(COMMIT_ID)).thenReturn(commitApi);
        when(commitApi.diff()).thenReturn(diffModel);
        when(commitApi.showTree(anyString())).thenReturn(ImmutableMap.of(FILE_PATH, EntryType.TEXT));
        when(diffModel.getDiffs()).thenReturn(diffValues);

        warning = new LargeFileWarning();
        warning.setRepository(privateRepository);
        warning.setCommit(commit);
        warning.setFileName(FILE_PATH);
    }

    @Test
    public void testLargeFile() throws Exception {
        when(commitApi.showTextFile(FILE_PATH)).thenReturn("foo\nbar\nfoo\nbar\nfoo\nbar");

        final Set<LargeFileWarning> largeFileWarnings = generator.generateWarnings(commit, null);
        Assert.assertEquals(warning, largeFileWarnings.stream().findFirst().get());
    }

    @Test
    public void testSmallFile() throws Exception {
        when(commitApi.showTextFile(FILE_PATH)).thenReturn("foo\nbar\nfoo\nbar\nfoo");

        final Set<LargeFileWarning> largeFileWarnings = generator.generateWarnings(commit, null);
        Assert.assertEquals(Collections.emptySet(), largeFileWarnings);
    }
}