package nl.tudelft.ewi.devhub.server.backend.warnings.pmd;

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
import lombok.Getter;
import lombok.Setter;

import nl.tudelft.ewi.devhub.server.backend.warnings.AbstractLineWarningGenerator;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.PMDWarning;
import nl.tudelft.ewi.git.client.GitServerClient;

/**
 * Generator for PMD warnings
 */
public class PMDLogParser extends AbstractLineWarningGenerator<PMDWarning, PMDLogParser.PMDFile> {

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

	@Getter
	@Setter
	private PMDReport pmdReport;

	@Override
	protected Stream<PMDWarning> map(final Commit commit, final PMDFile pmdFile) {
		return pmdFile.getViolations().stream().map(pmdViolation -> {
			Source source = new Source();
			source.setSourceCommit(commit);
			source.setSourceLineNumber(pmdViolation.getBeginLine());

			final PMDWarning warning = new PMDWarning();
			warning.setSource(source);
			warning.setRule(pmdViolation.getRule());
			warning.setPriority(pmdViolation.getPriority());
			warning.setMessage(pmdViolation.getMessage());
			return warning;
		});
	}

	@Override
	protected Stream<PMDFile> getStream() {
		return pmdReport.getFiles().stream();
	}

	/**
	 * PMD returns absolute paths, we need them relative to the repository root.
	 * @param path absolute path
	 * @return relative path
	 */
	protected static String getRelativePath(final String path) {
		return path.substring(path.indexOf("/src/") + 1);
	}

	@Override
	protected String filePathFor(final PMDFile value) {
		return getRelativePath(value.getName());
	}

	/**
	 * Extract warnings
	 * @param commit Commit to parse
	 * @param pmdReport Report received from build
	 * @return A generated list of warnings
	 */
	public List<PMDWarning> extractWarnings(final Commit commit, final PMDReport pmdReport) {
		setPmdReport(pmdReport);
		return generateWarnings(commit);
	}

}
