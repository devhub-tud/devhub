package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.IgnoredFileWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.EntryType;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Liam Clark
 */
@Slf4j
@RequestScoped
public class IgnoredFileWarningGenerator extends AbstractCommitWarningGenerator<IgnoredFileWarning, GitPush>
implements CommitPushWarningGenerator<IgnoredFileWarning> {

    private static final String[] extensions = {".iml",".class", ".bin", ".doc", ".docx", ".jar"};
    private static final String[] folders = { ".idea/", ".metadata/", ".settings/",
            ".project/", ".classpath/", "target/", "bin/", ".metadata/"};

    private Repository repository;
    private Commit commit;

    @Inject
    public IgnoredFileWarningGenerator(GitServerClient gitServerClient) {
        super(gitServerClient);
    }

    @Override
    @SneakyThrows
    public Set<IgnoredFileWarning> generateWarnings(Commit commit, GitPush attachment) {
        log.debug("Start generating warnings for {} in {}", commit, this);
        this.repository = getRepository(commit);
        this.commit = commit;
        Set<IgnoredFileWarning> warnings = Sets.newHashSet();
        walkCommitStructure("",commit.getCommitId(),warnings);
        log.debug("Finished generating warnings for {} in {}", commit, this);
        return warnings;
    }

    @SneakyThrows
    public void walkCommitStructure(String path, String commitId, Set<IgnoredFileWarning> warnings){

        if(folderViolation(path)){
            IgnoredFileWarning warning = new IgnoredFileWarning();
            warning.setFileName(path);
            warnings.add(warning);
        }

        Map<String,EntryType> directory = repository.listDirectoryEntries(commitId, path);
        directory.entrySet().stream()
                .filter(entry -> entry.getValue().equals(EntryType.FOLDER))
                .forEach(entry-> walkCommitStructure(path+entry.getKey(),commitId, warnings));

        directory.entrySet().stream()
                .filter(entry -> !entry.getValue().equals(EntryType.FOLDER))
                .map(Map.Entry::getKey)
                .filter(IgnoredFileWarningGenerator::fileViolation)
                .map(file -> {
                    IgnoredFileWarning warning = new IgnoredFileWarning();
                    warning.setCommit(commit);
                    warning.setFileName(file);
                    return warning;
                })
                .forEach(warnings::add);
    }

    private static boolean fileViolation(String path){
       return Stream.of(extensions).filter(path::endsWith).findAny().isPresent();
    }

    private static boolean folderViolation(String path){
        return Stream.of(folders).filter(path::endsWith).findAny().isPresent();
    }
}
