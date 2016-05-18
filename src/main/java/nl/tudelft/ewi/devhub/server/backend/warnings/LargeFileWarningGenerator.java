package nl.tudelft.ewi.devhub.server.backend.warnings;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LargeFileWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffContext;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffFile;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffLine;
import nl.tudelft.ewi.git.models.EntryType;

import com.google.inject.Inject;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@SuppressWarnings("unused")
public class LargeFileWarningGenerator extends AbstractCommitWarningGenerator<LargeFileWarning, GitPush>
implements CommitPushWarningGenerator<LargeFileWarning> {

    private static final int MAX_FILE_SIZE = 500;
    static final String MAX_FILE_SIZE_PROPERTY = "warnings.max-file-size";

    private Commit commit;
    int maxFileSize;

    @Inject
    public LargeFileWarningGenerator(RepositoriesApi repositoriesApi) {
        super(repositoriesApi);
    }

    @Override
    @SneakyThrows
    public Set<LargeFileWarning> generateWarnings(Commit commit, GitPush attachment) {
        log.debug("Started generating warnings for {} in {}", commit, this);
        final List<DiffFile<DiffContext<DiffLine>>> diffs = getGitCommit(commit).diff().getDiffs();
        RepositoryApi repositoryApi = getRepository(commit);
        this.commit = commit;
        this.maxFileSize = commit.getRepository().getIntegerProperty(MAX_FILE_SIZE_PROPERTY, MAX_FILE_SIZE);

        Set<LargeFileWarning> warnings = diffs.stream()
            .filter(file -> !file.isDeleted())
            .filter(this::filterTextFiles)
            .filter(this::filterLargeFiles)
            .map(this::mapToWarning)
            .collect(Collectors.toSet());

        log.debug("Finished generating warnings for {} in {}", commit, this);
        return warnings;
    }

    @SneakyThrows
    protected boolean filterTextFiles(DiffFile file) {
        String folderPath = folderForPath(file.getNewPath());
        String fileName = fileNameForPath(file.getNewPath());
        return getGitCommit(commit).showTree(folderPath)
                .get(fileName).equals(EntryType.TEXT);
    }

    @SneakyThrows
    protected boolean filterLargeFiles(DiffFile file) {
        String contents = getGitCommit(commit)
            .showTextFile(file.getNewPath());
        return contents.split("\n").length > maxFileSize;
    }

    protected LargeFileWarning mapToWarning(DiffFile file) {
        String fileName = fileNameForPath(file.getNewPath());
        LargeFileWarning warning = new LargeFileWarning();
        warning.setFileName(fileName);
        warning.setCommit(commit);
        return warning;
    }

    public static String folderForPath(final String path) {
        int index;
        if((index = path.lastIndexOf('/')) != -1) {
            return path.substring(0, index);
        }
        return "";
    }

    public static String fileNameForPath(final String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

}
