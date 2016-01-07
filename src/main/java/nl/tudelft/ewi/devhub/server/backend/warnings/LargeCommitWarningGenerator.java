package nl.tudelft.ewi.devhub.server.backend.warnings;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.Configurable;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LargeCommitWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffContext;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffFile;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffLine;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Liam Clark
 */
@Slf4j
@RequestScoped
@SuppressWarnings("unused")
public class LargeCommitWarningGenerator  extends AbstractCommitWarningGenerator<LargeCommitWarning, GitPush>
implements CommitPushWarningGenerator<LargeCommitWarning> {


    private static final String[] DEFAULT_EXTENSIONS = {".java", ".c", ".cpp", ".h", ".scala", ".js", ".html", ".css", ".less"};
    private static final int MAX_AMOUNT_OF_FILES = 10;
    private static final int MAX_AMOUNT_OF_LINES_TOUCHED = 500;
    private static final String MAX_FILES_PROPERTY = "warnings.max-touched-files";
    private static final String MAX_LINE_TOUCHED_PROPERTY = "warnings.max-line-edits";
    private static final String COUNTED_EXTENSIONS_PROPERTY = "warnings.max-line-edits.types";

    private String[] ext;

    @Inject
    public LargeCommitWarningGenerator(RepositoriesApi repositoriesApi) {
        super(repositoriesApi);
    }

    @Override
    @SneakyThrows
    public Set<LargeCommitWarning> generateWarnings(Commit commit, GitPush attachment) {
        log.debug("Start generating warnings for {} in {}", commit, this);
        List<DiffFile<DiffContext<DiffLine>>> diffs = getGitCommit(commit).diff().getDiffs();
        this.ext = commit.getRepository().getCommaSeparatedValues(COUNTED_EXTENSIONS_PROPERTY, DEFAULT_EXTENSIONS);

        if(!commit.getMerge() && (tooManyFiles(diffs, commit) || tooManyLineChanges(diffs, commit))) {
            LargeCommitWarning warning = new LargeCommitWarning();
            warning.setCommit(commit);
            log.debug("Finished generating warnings for {} in {}", commit, this);
            return Sets.newHashSet(warning);
        }

        log.debug("Finished generating warnings for {} in {}", commit, this);
        return Collections.emptySet();
    }

    private boolean tooManyFiles(List<DiffFile<DiffContext<DiffLine>>> diffFiles, Commit commit) {
        Configurable configurable = commit.getRepository();
        int maxAmountOfFiles = configurable.getIntegerProperty(MAX_FILES_PROPERTY, MAX_AMOUNT_OF_FILES);
        return diffFiles.size() > maxAmountOfFiles;
    }

    private boolean tooManyLineChanges(List<DiffFile<DiffContext<DiffLine>>> diffFiles, Commit commit) {
        Configurable configurable = commit.getRepository();
        int maxCountOfFiles = configurable.getIntegerProperty(MAX_LINE_TOUCHED_PROPERTY, MAX_AMOUNT_OF_LINES_TOUCHED);
        int count = (int) diffFiles.stream()
                .filter(file -> !file.isDeleted() && fileViolation(file.getNewPath()))
                .flatMap(diffFile -> diffFile.getContexts().stream())
                .flatMap(context -> context.getLines().stream())
                .filter(line -> line.isAdded() || line.isRemoved())
                .count();
        return count > maxCountOfFiles;
    }

    private boolean fileViolation(final String path){
        return Stream.of(ext).filter(path::endsWith).findAny().isPresent();
    }

}
