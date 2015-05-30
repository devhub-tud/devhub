package nl.tudelft.ewi.devhub.server.backend.warnings;

import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitedIgnoredFiles;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.IgnoredFileWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.models.DiffModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by LC on 30/05/15.
 */
public class IgnoredFileWarningGenerator extends GitCommitPushWarningGenerator<CommitedIgnoredFiles> {

    private static final String[] extensions = {".class", ".bin.pdf", ".doc", ".docx", ".jar"};
    private static final String[] folders = {".iml", ".idea/", "", ".metadata/", ".settings/", ".project/", ".classpath/", "target/", "bin/", ".metadata/"};


    @SneakyThrows
    @Override
    public Collection<CommitedIgnoredFiles> generateWarnings(Commit commit, GitPush attachment) {
        nl.tudelft.ewi.git.client.Commit gitCommit = getGitCommit(commit,attachment);
        List<DiffModel.DiffFile> files = gitCommit.diff().getDiffs();
        files.stream().map(DiffModel.DiffFile::getNewPath).forEach(s -> this.checkFile(s));
        return null;
    }


    private static List<CommitWarning> checkFile(String path){
        List<CommitWarning> warnings  = new ArrayList<>();
        for(String extension : extensions) {
            if(path.endsWith(extension)) {
                IgnoredFileWarning ignoredFileWarning = new IgnoredFileWarning();
                String filename = path.substring(path.lastIndexOf('/' + 1));
                ignoredFileWarning.setFileName(filename);
            }
        }
        return warnings;
    }
}
