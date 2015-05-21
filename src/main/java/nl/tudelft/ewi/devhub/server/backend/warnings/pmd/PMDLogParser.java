package nl.tudelft.ewi.devhub.server.backend.warnings.pmd;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import nl.tudelft.ewi.devhub.server.backend.warnings.CommitWarningGenerator;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.PMDWarning;

public class PMDLogParser implements CommitWarningGenerator<PMDWarning> {

	@Data
	@EqualsAndHashCode
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
	public static class PMDFile {

		@JacksonXmlProperty(localName = "name", isAttribute = true)
		private String name;

		@JacksonXmlProperty(localName = "violation")
		@JacksonXmlElementWrapper(useWrapping = false)
		private List<PMDViolation> violations;

		public Stream<PMDViolation> getFileStream() {
			return getViolations().stream();
		}

	}

	@Data
	@EqualsAndHashCode
	@JacksonXmlRootElement(localName="checkstyle")
	public static class PMDReport {

		@JacksonXmlProperty(localName = "version", isAttribute = true)
		private String version;

		@JacksonXmlProperty(localName = "file")
		@JacksonXmlElementWrapper(useWrapping = false)
		private List<PMDFile> files;

	}

	protected static List<PMDWarning> extractWarnings(final Commit commit, final PMDReport report) {
		return report.getFiles().stream()
            .flatMap(pmdFile -> {
				return pmdFile.getViolations().stream().map(pmdViolation -> {
					String path = pmdFile.getName();
					path = path.substring(path.indexOf("/src/") + 1);

					Source source = new Source();
					source.setSourceCommit(commit);
					source.setSourceFilePath(path);
					source.setSourceLineNumber(pmdViolation.getBeginLine());

					final PMDWarning warning = new PMDWarning();
					warning.setCommit(commit);
					warning.setSource(source);
					warning.setRule(pmdViolation.getRule());
					warning.setPriority(pmdViolation.getPriority());
					warning.setMessage(pmdViolation.getMessage());
					return warning;
				});
			})
            .collect(Collectors.toList());
	}

	@Getter
	@Setter
	private PMDReport pmdReport;


	@Override
	public List<PMDWarning> generateWarnings(final Commit commit) {
		return extractWarnings(commit, pmdReport);
	}

}
