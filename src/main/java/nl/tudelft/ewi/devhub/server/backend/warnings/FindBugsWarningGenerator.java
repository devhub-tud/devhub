package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nl.tudelft.ewi.devhub.server.backend.warnings.FindBugsWarningGenerator.FindBugsReport;
import nl.tudelft.ewi.devhub.server.backend.warnings.FindBugsWarningGenerator.FindBugsFile;
import nl.tudelft.ewi.devhub.server.backend.warnings.FindBugsWarningGenerator.BugInstance;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.FindbugsWarning;

import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repository;

import javax.ws.rs.NotFoundException;

/**
 * Convert FindBugs violations into Warnings
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class FindBugsWarningGenerator extends AbstractLineWarningGenerator<FindBugsReport, FindBugsFile, BugInstance, FindbugsWarning> {

    @Data
    @EqualsAndHashCode
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SourceLine {

        @JacksonXmlProperty(localName = "classname", isAttribute = true)
        private String classname;

        @JacksonXmlProperty(localName = "sourcepath", isAttribute = true)
        private String sourcePath;

        @JacksonXmlProperty(localName = "start", isAttribute = true)
        private Integer start;

    }

    @Data
    @EqualsAndHashCode
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BugInstance {

        @JacksonXmlProperty(localName = "priority", isAttribute = true)
        private int priority;

        @JacksonXmlProperty(localName = "type", isAttribute = true)
        private String type;

        @JacksonXmlProperty(localName = "LongMessage")
        private String LongMessage;


        @JacksonXmlProperty(localName = "SourceLine")
        private SourceLine sourceLine;

        public String getSourcePath() {
            return getSourceLine().getSourcePath();
        }

    }

    @Data
    @EqualsAndHashCode
    @JacksonXmlRootElement(localName="BugCollection")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FindBugsReport {

        @JacksonXmlProperty(localName = "version", isAttribute = true)
        private String version;

        @JacksonXmlProperty(localName = "BugInstance")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<BugInstance> violations;

        public Stream<FindBugsFile> getFiles() {
            return emptyIfNull(violations).stream()
                .collect(Collectors.groupingBy(BugInstance::getSourcePath))
                .values().stream()
                .map(FindBugsFile::new);
        }

    }

    @Data
    @AllArgsConstructor
    public static class FindBugsFile {

        private List<BugInstance> warnings;

        public String getSourcePath() {
            return warnings.get(0).getSourcePath();
        }

    }

    @Inject
    public FindBugsWarningGenerator(final GitServerClient gitServerClient, final Commits commits) {
        super(gitServerClient, commits);
    }

    @Override
    protected int getLineNumber(final BugInstance violation) {
        return violation.getSourceLine().getStart();
    }

    @Override
    protected FindbugsWarning mapToWarning(final BugInstance violation) {
        final FindbugsWarning warning = new FindbugsWarning();
        warning.setMessage(violation.getLongMessage());
        warning.setPriority(violation.getPriority());
        return warning;
    }

    @Override
    protected Stream<BugInstance> getViolations(final FindBugsFile file) {
        return emptyIfNull(file.getWarnings()).stream()
            // Apparently some sources do not have line numbers attached to it
            .filter(bugInstance -> bugInstance.getSourceLine().getStart() != null);
    }

    @Override
    protected Stream<FindBugsFile> getFiles(final FindBugsReport attachment) {
        return attachment.getFiles();
    }

    private final static String[] CODE_BASES = { "src/test/java/", "src/main/java/" };

    @Override
    protected String filePathFor(final FindBugsFile value, final Commit commit) {
        Repository repository = retrieveRepository(commit.getRepository());
        for(String codebase : CODE_BASES) {
            String path = codebase.concat(value.getSourcePath());
            String folder = path.substring(0, path.lastIndexOf('/'));
            String fileName = path.substring(folder.length() + 1);

            try {
                if(repository.listDirectoryEntries(commit.getCommitId(), folder).containsKey(fileName)) {
                    return path;
                }
            }
            catch (Exception e) {
                // Folder not found, continue
                continue;
            }
        }
        throw new NotFoundException("File not found in code base!");
    }

}
