package nl.tudelft.ewi.devhub.server.backend.warnings.pmd;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import com.google.inject.Inject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.backend.warnings.CommitWarningGenerator;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.PMDWarning;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.BlameModel;

public class PMDLogParser implements CommitWarningGenerator<PMDWarning> {

	private final GitServerClient gitServerClient;
	private final Commits commits;

	@Inject
	public PMDLogParser(GitServerClient gitServerClient, Commits commits) {
		this.gitServerClient = gitServerClient;
		this.commits = commits;
	}

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

	@SneakyThrows
	protected Repository retrieveRepository(final Group group) {
		return gitServerClient.repositories().retrieve(group.getRepositoryName());
	}

	@SneakyThrows
	protected static BlameModel retrieveBlameModel(final Repository repository, final Commit commit, final String path) {
		return repository.retrieveCommit(commit.getCommitId()).blame(path);
	}

	protected static String getRelativePath(final String path) {
		return path.substring(path.indexOf("/src/") + 1);
	}

	@SneakyThrows
	protected List<PMDWarning> extractWarnings(final Commit commit, final PMDReport report) {
		Repository repository = retrieveRepository(commit.getRepository());
		return report.getFiles().stream()
            .flatMap(pmdFile -> {
				final String path = getRelativePath(pmdFile.getName());
				final BlameModel blameModel = retrieveBlameModel(repository, commit, path);

				return pmdFile.getViolations().stream().map(pmdViolation -> {
					Source source = constructSourceFromBlame(commit, path, blameModel, pmdViolation.getBeginLine());

					final PMDWarning warning = new PMDWarning();
					warning.setSource(source);
					warning.setRule(pmdViolation.getRule());
					warning.setPriority(pmdViolation.getPriority());
					warning.setMessage(pmdViolation.getMessage());
					return warning;
				});
			})
			.filter(pmdWarning -> pmdWarning.getCommit().equals(commit))
			.collect(Collectors.toList());
	}

	protected Source constructSourceFromBlame(final Commit commit,
											  final String path,
											  final BlameModel blameModel,
											  final int pmdLine) {
		BlameModel.BlameBlock block = blameModel.getBlameBlock(pmdLine);
		String sourceCommitId = block.getFromCommitId();
		Commit sourceCommit = commits.ensureExists(commit.getRepository(), sourceCommitId);

		Source source = new Source();
		source.setSourceCommit(sourceCommit);
		source.setSourceFilePath(path);
		source.setSourceLineNumber(block.getFromLineNumber(pmdLine));
		return source;
	}

	@Getter
	@Setter
	private PMDReport pmdReport;


	@Override
	public List<PMDWarning> generateWarnings(final Commit commit) {
		return extractWarnings(commit, pmdReport);
	}

}
