package nl.tudelft.ewi.devhub.server.backend.warnings.pmd;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.PMDWarning;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class PMDLogParserTest {

    private final static String EXPECTED_PATH = "src/test/java/nl/tudelft/jpacman/board/DirectionTest.java";

    @Mock
    private Commit commit;

    @InjectMocks
    private PMDLogParser pmdLogParser;

    @Test
    public void testBasicParse() throws IOException {
        ObjectMapper mapper = new XmlMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        AnnotationIntrospector introspector = new JacksonXmlAnnotationIntrospector();
        mapper.getDeserializationConfig().withAppendedAnnotationIntrospector(introspector);

        PMDWarning a = expectedWarning(EXPECTED_PATH, 31, "JUnitTestContainsTooManyAsserts",
                "\nJUnit tests should not contain more than 1 assert(s).\n", 3);
        PMDWarning b = expectedWarning(EXPECTED_PATH, 36, "UnnecessaryBooleanAssertion",
                "\nassertTrue(true) or similar statements are unnecessary\n", 3);

        try(InputStreamReader inputStreamReader = new InputStreamReader(PMDLogParserTest.class.getResourceAsStream("/pmd.xml"))) {
            PMDLogParser.PMDReport report = mapper.readValue(inputStreamReader, PMDLogParser.PMDReport.class);
            List<PMDWarning> warnings = pmdLogParser.extractWarnings(commit, report);
            assertThat(warnings, contains(a, b));
        }
    }

    protected PMDWarning expectedWarning(String path, int lineNumber, String rule, String message, Integer priority) {
        final Source bSource = new Source();
        bSource.setSourceLineNumber(lineNumber);
        bSource.setSourceFilePath(path);
        bSource.setSourceCommit(commit);

        final PMDWarning b = new PMDWarning();
        b.setCommit(commit);
        b.setSource(bSource);
        b.setRule(rule);
        b.setMessage(message);
        b.setPriority(priority);
        return b;
    }

}
