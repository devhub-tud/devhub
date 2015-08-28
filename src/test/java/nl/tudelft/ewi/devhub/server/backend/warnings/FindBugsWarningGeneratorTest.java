package nl.tudelft.ewi.devhub.server.backend.warnings;

import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.FindbugsWarning;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.BlameModel;
import nl.tudelft.ewi.git.models.EntryType;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableMap;

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

    private final static String FOLDER_PATH = "src/test/java/nl/tudelft/jpacman/board";
    private final static String FILE_NAME = "DirectionTest.java";
    private final static String EXPECTED_PATH = "src/test/java/nl/tudelft/jpacman/board/DirectionTest.java";
    private final static String COMMIT_ID = "234345345345";

	@Mock private GroupRepository groupRepository;
    @Mock private Group group;
    @Mock private Commit commit;
    @InjectMocks  private FindBugsWarningGenerator findBugsWarningGenerator;
    @Mock private Commits commits;
    @Mock private GitServerClient gitServerClient;
    @Mock private Repositories repositories;
    @Mock private Repository repository;
    @Mock private nl.tudelft.ewi.git.client.Commit repoCommit;
    @Mock private BlameModel blameModel;
    @Mock private BlameModel.BlameBlock blameBlock;

    @Before
    public void initializeMocks() throws GitClientException {
        when(commit.getCommitId()).thenReturn(COMMIT_ID);
		when(commit.getRepository()).thenReturn(groupRepository);
		when(group.getRepository()).thenReturn(groupRepository);
		when(groupRepository.getRepositoryName()).thenReturn("");
        when(commits.ensureExists(any(), any())).thenReturn(commit);

        when(gitServerClient.repositories()).thenReturn(repositories);
        when(repositories.retrieve(anyString())).thenReturn(repository);
        when(repository.retrieveCommit(COMMIT_ID)).thenReturn(repoCommit);
        when(repository.listDirectoryEntries(COMMIT_ID, FOLDER_PATH)).thenReturn(ImmutableMap.of(FILE_NAME, EntryType.TEXT));
        blameCurrentCommit();
    }

    public void blameCurrentCommit() throws GitClientException {
        when(repoCommit.blame(EXPECTED_PATH)).thenReturn(blameModel);
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
