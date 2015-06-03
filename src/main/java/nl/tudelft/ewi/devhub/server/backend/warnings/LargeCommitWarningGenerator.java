package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LargeCommitWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.models.DiffModel.DiffFile;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Liam Clark
 */
@Slf4j
public class LargeCommitWarningGenerator  extends AbstractCommitWarningGenerator<LargeCommitWarning, GitPush>
implements CommitPushWarningGenerator<LargeCommitWarning> {

    private static final int MAX_AMOUNT_OF_FILES = 10;
    private static final int MAX_AMOUNT_OF_LINES_TOUCHED = 200;
    private static final String MAX_FILES_PROPERTY = "warnings.max-touched-files";
    private static final String MAX_LINE_TOUCHED_PROPERTY = "warnings.max-line-edits";

    @Inject
    public LargeCommitWarningGenerator(GitServerClient gitServerClient) {
        super(gitServerClient);
    }

    @Override
    @SneakyThrows
    public Set<LargeCommitWarning> generateWarnings(Commit commit, GitPush attachment) {
        log.debug("Start generating warnings for {} in {}", commit, this);
        List<DiffFile> diffs = getGitCommit(commit).diff().getDiffs();

        if(!commit.getMerge() && (tooManyFiles(diffs, commit) || tooManyLineChanges(diffs, commit))) {
            LargeCommitWarning warning = new LargeCommitWarning();
            warning.setCommit(commit);
            log.debug("Finished generating warnings for {} in {}", commit, this);
            return Sets.newHashSet(warning);
        }

        log.debug("Finished generating warnings for {} in {}", commit, this);
        return Collections.emptySet();
    }

    private boolean tooManyFiles(List<DiffFile> diffFiles, Commit commit) {
        Course course = commit.getRepository().getCourse();
        int maxAmountOfFiles = getIntegerProperty(course, MAX_FILES_PROPERTY, MAX_AMOUNT_OF_FILES);
        return diffFiles.size() > maxAmountOfFiles;
    }

    private boolean tooManyLineChanges(List<DiffFile> diffFiles, Commit commit) {
        Course course = commit.getRepository().getCourse();
        int maxCountOfFiles = getIntegerProperty(course, MAX_LINE_TOUCHED_PROPERTY, MAX_AMOUNT_OF_LINES_TOUCHED);
        int count = (int) diffFiles.stream()
                .flatMap(diffFile -> diffFile.getContexts().stream())
                .flatMap(context -> context.getLines().stream())
                .filter(line -> line.isAdded() || line.isRemoved())
                .count();
        return count > maxCountOfFiles;
    }

    private int getIntegerProperty(Course course, String key, int other) {
        String value = course.getProperties().get(key);
        if (!Strings.isNullOrEmpty(value)) {
            return Integer.valueOf(value);
        }
        return other;
    }

}
