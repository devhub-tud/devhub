package nl.tudelft.ewi.devhub.server.backend.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.backend.warnings.PMDWarningGenerator.PMDFile;
import nl.tudelft.ewi.devhub.server.backend.warnings.PMDWarningGenerator.PMDReport;
import nl.tudelft.ewi.devhub.server.backend.warnings.PMDWarningGenerator.PMDViolation;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.PMDWarning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import com.google.inject.Inject;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

import java.util.List;
import java.util.stream.Stream;

/**
 * Generator for PMD warnings
 */
public class PMDWarningGenerator extends AbstractLineWarningGenerator<PMDReport, PMDFile, PMDViolation, PMDWarning> {

	@Data
	@EqualsAndHashCode
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PMDViolation {

		@JacksonXmlProperty(isAttribute = true, localName = "beginline")
		private int beginLine;

		@JacksonXmlProperty(isAttribute = true)
		private Integer priority;

		@JacksonXmlText
		private String message;

		@JacksonXmlProperty(isAttribute = true)
		private String rule;

	}

	@Data
	@EqualsAndHashCode
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PMDFile {

		@JacksonXmlProperty(localName = "name", isAttribute = true)
		private String name;

		@JacksonXmlProperty(localName = "violation")
		@JacksonXmlElementWrapper(useWrapping = false)
		private List<PMDViolation> violations;

	}

	@Data
	@EqualsAndHashCode
	@JacksonXmlRootElement(localName="checkstyle")
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PMDReport {

		@JacksonXmlProperty(localName = "version", isAttribute = true)
		private String version;

		@JacksonXmlProperty(localName = "file")
		@JacksonXmlElementWrapper(useWrapping = false)
		private List<PMDFile> files;

	}

	@Inject
	public PMDWarningGenerator(final RepositoriesApi repositoriesApi, final Commits commits) {
		super(repositoriesApi, commits);
	}

	@Override
	protected Stream<PMDFile> getFiles(final PMDReport pmdReport) {
		return emptyIfNull(pmdReport.getFiles()).stream();
	}

	@Override
	protected String filePathFor(final PMDFile value, final Commit commit) {
		return getRelativePath(value.getName());
	}

	@Override
	protected Stream<PMDViolation> getViolations(final PMDFile file) {
		List<PMDViolation> violations = file.getViolations();
		return emptyIfNull(violations).stream();
	}

	@Override
	protected PMDWarning mapToWarning(final PMDViolation pmdViolation) {
		final PMDWarning warning = new PMDWarning();
		warning.setRule(pmdViolation.getRule());
		warning.setPriority(pmdViolation.getPriority());
		warning.setMessage(pmdViolation.getMessage());
		return warning;
	}

	@Override
	protected int getLineNumber(final PMDViolation warning) {
		return warning.getBeginLine();
	}

}
