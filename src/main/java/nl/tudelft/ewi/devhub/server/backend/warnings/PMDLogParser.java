package nl.tudelft.ewi.devhub.server.backend.warnings;

import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import com.google.inject.Inject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.PMDWarning;
import nl.tudelft.ewi.git.client.GitServerClient;

import nl.tudelft.ewi.devhub.server.backend.warnings.PMDLogParser.PMDReport;
import nl.tudelft.ewi.devhub.server.backend.warnings.PMDLogParser.PMDFile;

/**
 * Generator for PMD warnings
 */
public class PMDLogParser extends AbstractLineWarningGenerator<PMDReport, PMDFile, PMDWarning> {

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
	public PMDLogParser(final GitServerClient gitServerClient, final Commits commits) {
		super(gitServerClient, commits);
	}

	@Override
	protected Stream<PMDWarning> map(final Commit commit, final PMDFile pmdFile) {
		if(pmdFile.getViolations() == null || pmdFile.getViolations().isEmpty()) {
			return Stream.empty();
		}
		return pmdFile.getViolations().stream().map(pmdViolation -> {
			final PMDWarning warning = new PMDWarning();
			warning.setSource(new Source(commit, pmdViolation.getBeginLine(), null));
			warning.setRule(pmdViolation.getRule());
			warning.setPriority(pmdViolation.getPriority());
			warning.setMessage(pmdViolation.getMessage());
			return warning;
		});
	}

	@Override
	protected Stream<PMDFile> getFiles(final PMDReport pmdReport) {
		return pmdReport.getFiles().stream();
	}

	@Override
	protected String filePathFor(final PMDFile value) {
		return getRelativePath(value.getName());
	}

}
