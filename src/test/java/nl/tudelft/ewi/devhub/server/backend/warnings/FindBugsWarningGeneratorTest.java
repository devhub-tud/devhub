package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.common.collect.ImmutableMap;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.FindbugsWarning;
import nl.tudelft.ewi.git.models.BlameModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(MockitoJUnitRunner.class)
public class FindBugsWarningGeneratorTest {

    private final static String EXPECTED_FOLDER_PATH = "src/test/java/nl/tudelft/jpacman/board";
    private final static String EXPECTED_FILE_NAME = "DirectionTest.java";
    private final static String EXPECTED_PATH = EXPECTED_FOLDER_PATH + "/" + EXPECTED_FILE_NAME;
    private final static String COMMIT_ID = "234345345345";

	@Mock private GroupRepository groupRepository;
    @Mock private Group group;
    @Mock private Commit commit;
    @InjectMocks  private FindBugsWarningGenerator findBugsWarningGenerator;
    @Mock private Commits commits;
    @Mock private RepositoriesApi repositories;
    @Mock private RepositoryApi repository;
    @Mock private CommitApi commitApi;
    @Mock private DetailedCommitModel repoCommit;
    @Mock private BlameModel blameModel;
    @Mock private BlameModel.BlameBlock blameBlock;

    @Before
    public void initializeMocks() {
        when(commit.getCommitId()).thenReturn(COMMIT_ID);
        when(commit.getRepository()).thenReturn(groupRepository);
        when(group.getRepository()).thenReturn(groupRepository);
        when(groupRepository.getRepositoryName()).thenReturn("");
        when(commits.ensureExists(any(), any())).thenReturn(commit);

        when(repositories.getRepository(anyString())).thenReturn(repository);
        when(repository.getCommit(COMMIT_ID)).thenReturn(commitApi);
        when(commitApi.get()).thenReturn(repoCommit);

        blameCurrentCommit();
    }

    public void blameCurrentCommit() {
        when(commitApi.blame(EXPECTED_PATH)).thenReturn(blameModel);
        when(commitApi.showTree(EXPECTED_FOLDER_PATH)).thenReturn(ImmutableMap.of(EXPECTED_FILE_NAME, EntryType.TEXT));
        when(blameModel.getBlameBlock(anyInt())).thenReturn(blameBlock);
        when(blameBlock.getFromCommitId()).thenReturn(COMMIT_ID);
        when(blameBlock.getFromFilePath()).thenReturn(EXPECTED_PATH);
        when(blameBlock.getFromLineNumber(any())).thenAnswer(invocation ->
                invocation.getArguments()[0]);
    }

    @Test
    public void testGenerateFindbugsWarnings() throws IOException {
        ObjectMapper mapper = new XmlMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        AnnotationIntrospector introspector = new JacksonXmlAnnotationIntrospector();
        mapper.getDeserializationConfig().withAppendedAnnotationIntrospector(introspector);

        FindbugsWarning expected = expectedWarning(EXPECTED_PATH, 47,
            "Null pointer dereference of nullsy in nl.tudelft.jpacman.board.DirectionTest.testThatNullHasToString()", 1);

        try(InputStreamReader inputStreamReader = new InputStreamReader(FindBugsWarningGeneratorTest.class.getResourceAsStream("/findbugsXml.xml"))) {
            FindBugsWarningGenerator.FindBugsReport report = mapper.readValue(inputStreamReader, FindBugsWarningGenerator.FindBugsReport.class);
            Set<FindbugsWarning> warnings = findBugsWarningGenerator.generateWarnings(commit, report);
            assertThat(warnings, contains(expected));
        }
    }

    protected FindbugsWarning expectedWarning(String path, int lineNumber, String message, Integer priority) {
        final Source bSource = new Source();
        bSource.setSourceLineNumber(lineNumber);
        bSource.setSourceFilePath(path);
        bSource.setSourceCommit(commit);

        final FindbugsWarning b = new FindbugsWarning();
        b.setCommit(commit);
        b.setSource(bSource);
        b.setMessage(message);
        b.setPriority(priority);
        return b;
    }

}
