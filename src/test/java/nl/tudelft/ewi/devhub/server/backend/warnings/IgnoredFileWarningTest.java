package nl.tudelft.ewi.devhub.server.backend.warnings;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.GitUsageWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.IgnoredFileWarning;
import nl.tudelft.ewi.git.client.Branch;
import nl.tudelft.ewi.git.client.Commit;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.EntryType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(MockitoJUnitRunner.class)
public class IgnoredFileWarningTest {

    private final static String COMMIT_ID = "abcd";
    private final Map<String,EntryType> directory1 = new HashMap<>();

    @Mock private nl.tudelft.ewi.devhub.server.database.entities.Commit commitEntity;
    @Mock private Group group;
    @Mock private Commit commit;
    @Mock private Repository repository;
    @Mock private Repositories repositories;
    @Mock private GitServerClient gitServerClient;
    @Mock private Branch branch;
    @Mock private CommitModel commitModel;
    @InjectMocks  private IgnoredFileWarningGenerator generator;

    private GitUsageWarning warning;

    @Before
    public void beforeTest() throws Exception {
        when(commitEntity.getCommitId()).thenReturn(COMMIT_ID);
        when(commitEntity.getRepository()).thenReturn(group);
        when(group.getRepositoryName()).thenReturn("abc");
        when(gitServerClient.repositories()).thenReturn(repositories);
        when(repositories.retrieve(anyString())).thenReturn(repository);
        when(repository.listDirectoryEntries(COMMIT_ID,"")).thenReturn(directory1);

        directory1.clear();

        warning = new GitUsageWarning();
        warning.setCommit(commitEntity);
    }

    @Test
    public void TestIngoredFile(){
        directory1.put("types.class",EntryType.TEXT);
        Collection<IgnoredFileWarning> warnings = generator.generateWarnings(commitEntity, null);
        IgnoredFileWarning warning = new IgnoredFileWarning();
        warning.setFileName("types.class");
        assertEquals(warning,warnings.stream().findFirst().get());
    }

    @Test
    public void TestNotIngoredFile(){
        directory1.put("types.java",EntryType.TEXT);
        Collection<IgnoredFileWarning> warnings = generator.generateWarnings(commitEntity, null);
        IgnoredFileWarning warning = new IgnoredFileWarning();
        warning.setFileName("types.java");
        assertEquals(Collections.emptySet(), warnings);
    }

    @Test
    public void TestIgnoredFolder(){
        directory1.put("bin/",EntryType.FOLDER);
        Collection<IgnoredFileWarning> warnings = generator.generateWarnings(commitEntity,null);
        IgnoredFileWarning warning = new IgnoredFileWarning();
        warning.setFileName("bin/");
        assertEquals(warning,warnings.stream().findFirst().get());
    }

}
