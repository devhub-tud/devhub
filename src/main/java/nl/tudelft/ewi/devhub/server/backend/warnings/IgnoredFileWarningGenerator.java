package nl.tudelft.ewi.devhub.server.backend.warnings;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.IgnoredFileWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.models.DiffModel;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Liam Clark
 */
@Slf4j
@RequestScoped
public class IgnoredFileWarningGenerator extends AbstractCommitWarningGenerator<IgnoredFileWarning, GitPush>
implements CommitPushWarningGenerator<IgnoredFileWarning> {

    private static final String PROPERTY_KEY = "ignored.file-extensions";
    private static final String[] DEFAULT_EXTENSIONS = {".doc", ".docx", ".jar", ".xls", ".xlsx"};

    private String[] ext;

    @Inject
    public IgnoredFileWarningGenerator(GitServerClient gitServerClient) {
        super(gitServerClient);
    }

    @Override
    @SneakyThrows
    public Set<IgnoredFileWarning> generateWarnings(Commit commit, GitPush attachment) {
        log.debug("Start generating warnings for {} in {}", commit, this);
        this.ext = commit.getRepository().getCommaSeparatedValues(PROPERTY_KEY, DEFAULT_EXTENSIONS);

        final Set<IgnoredFileWarning> warnings = getGitCommit(commit).diff().getDiffs().stream()
            .filter(diffFile -> !diffFile.isDeleted())
            .map(DiffModel.DiffFile::getNewPath)
            .filter(this::fileViolation)
            .map(path -> {
                IgnoredFileWarning warning = new IgnoredFileWarning();
                warning.setCommit(commit);
                warning.setFileName(path);
                return warning;
            })
            .collect(Collectors.toSet());

        log.debug("Finished generating warnings for {} in {}", commit, this);
        return warnings;
    }

    private boolean fileViolation(final String path){
       return Stream.of(ext).filter(path::endsWith).findAny().isPresent();
    }

}
