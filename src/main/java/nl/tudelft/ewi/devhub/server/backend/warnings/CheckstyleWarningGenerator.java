package nl.tudelft.ewi.devhub.server.backend.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.backend.warnings.CheckstyleWarningGenerator.CheckStyleError;
import nl.tudelft.ewi.devhub.server.backend.warnings.CheckstyleWarningGenerator.CheckStyleFile;
import nl.tudelft.ewi.devhub.server.backend.warnings.CheckstyleWarningGenerator.CheckStyleReport;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CheckstyleWarning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.inject.Inject;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

import java.util.List;
import java.util.stream.Stream;

/**
 * A {@code LineWarningGenerator} for Checkstyle warnings
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class CheckstyleWarningGenerator extends AbstractLineWarningGenerator<CheckStyleReport, CheckStyleFile, CheckStyleError, CheckstyleWarning> {

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
    public CheckstyleWarningGenerator(final RepositoriesApi repositoriesApi, final Commits commits) {
        super(repositoriesApi, commits);
    }

    @Override
    protected int getLineNumber(final CheckStyleError violation) {
        return violation.getLine() == 0 ? 1 : violation.getLine();
    }

    @Override
    protected CheckstyleWarning mapToWarning(final CheckStyleError violation) {
        CheckstyleWarning warning = new CheckstyleWarning();
        warning.setMessage(violation.getMessage());
        warning.setSeverity(violation.getSeverity());
        return warning;
    }

    @Override
    protected Stream<CheckStyleError> getViolations(final CheckStyleFile file) {
        return emptyIfNull(file.getErrors()).stream();
    }

    @Override
    protected Stream<CheckStyleFile> getFiles(final CheckStyleReport report) {
        return emptyIfNull(report.getFiles()).stream();
    }

    @Override
    protected String filePathFor(CheckStyleFile value, Commit commit) {
        return getRelativePath(value.getName());
    }

}
