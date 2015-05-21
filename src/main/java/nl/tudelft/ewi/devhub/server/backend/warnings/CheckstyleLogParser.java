package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.inject.Inject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CheckstyleWarning;
import nl.tudelft.ewi.git.client.GitServerClient;

import java.util.List;
import java.util.stream.Stream;

import  nl.tudelft.ewi.devhub.server.backend.warnings.CheckstyleLogParser.CheckStyleReport;
import  nl.tudelft.ewi.devhub.server.backend.warnings.CheckstyleLogParser.CheckStyleFile;

/**
 * A {@code LineWarningGenerator} for Checkstyle warnings
 * 
 * @author Jan-Willem Gmelig Meyling
 */
public class CheckstyleLogParser extends AbstractLineWarningGenerator<CheckStyleReport, CheckStyleFile, CheckstyleWarning> {

    @Data
    @EqualsAndHashCode
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CheckStyleError {

        @JacksonXmlProperty(isAttribute = true)
        private int line;

        @JacksonXmlProperty(isAttribute = true)
        private String severity;

        @JacksonXmlProperty(localName = "message", isAttribute = true)
        private String message;

        @JacksonXmlProperty(isAttribute = true)
        private String source;

    }

    @Data
    @EqualsAndHashCode
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CheckStyleFile {

        @JacksonXmlProperty(localName = "name", isAttribute = true)
        private String name;

        @JacksonXmlProperty(localName = "error")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<CheckStyleError> errors;

    }

    @Data
    @EqualsAndHashCode
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JacksonXmlRootElement(localName="checkstyle")
    public static class CheckStyleReport {

        @JacksonXmlProperty(localName = "version", isAttribute = true)
        private String version;

        @JacksonXmlProperty(localName = "file")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<CheckStyleFile> files;

    }

    @Inject
    public CheckstyleLogParser(final GitServerClient gitServerClient, final Commits commits) {
        super(gitServerClient, commits);
    }

    @Override
    protected Stream<CheckstyleWarning> map(final Commit commit, final CheckStyleFile element) {
        if(element.getErrors() == null || element.getErrors().isEmpty()) {
            return Stream.empty();
        }
        return element.getErrors().stream().map(checkStyleError -> {
            CheckstyleWarning warning = new CheckstyleWarning();
            warning.setSource(new Source(commit, checkStyleError.getLine(), null));
            warning.setMessage(checkStyleError.getMessage());
            warning.setSeverity(checkStyleError.getSeverity());
            return warning;
        });
    }

    @Override
    protected Stream<CheckStyleFile> getFiles(final CheckStyleReport report) {
        return report.getFiles().stream();
    }

    @Override
    protected String filePathFor(CheckStyleFile value) {
        return getRelativePath(value.getName());
    }

}
