package nl.tudelft.ewi.devhub.server.backend.warnings;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.IllegalFileWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.models.EntryType;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Liam Clark
 */
@Slf4j
public class IllegalFileWarningGenerator extends AbstractCommitWarningGenerator<IllegalFileWarning, GitPush>
implements CommitPushWarningGenerator<IllegalFileWarning> {


    private static final String DEFAULT_EXTENSIONS_PROPERTY_KEY = "illegal.file-extensions";
    private static final String DEFAULT_FOLDERS_PROPERTY_KEY = "illegal.folder-names";
    private static final String[] DEFAULT_EXTENSIONS = {".iml",".class", ".bin", ".project", ".DS_Store"};
    private static final String[] DEFAULT_FOLDERS = { ".idea/", ".metadata/", ".settings/",
        ".project/", ".classpath/", "target/", "bin/", ".metadata/"};
    public static final String ROOT_PATH = "";

    private Commit commit;
    private String[] illegalExtensions;
    private String[] illegalFolders;

    @Inject
    public IllegalFileWarningGenerator(RepositoriesApi repositoriesApi) {
        super(repositoriesApi);
    }

    @Override
    @SneakyThrows
    public Set<IllegalFileWarning> generateWarnings(Commit commit, GitPush attachment) {
        log.debug("Start generating warnings for {} in {}", commit, this);

        this.commit = commit;
        this.illegalExtensions = commit.getRepository().getCommaSeparatedValues(DEFAULT_EXTENSIONS_PROPERTY_KEY, DEFAULT_EXTENSIONS);
        this.illegalFolders = commit.getRepository().getCommaSeparatedValues(DEFAULT_FOLDERS_PROPERTY_KEY, DEFAULT_FOLDERS);

        Set<IllegalFileWarning> warnings = Sets.newHashSet();
        walkCommitStructure(ROOT_PATH, warnings);
        log.debug("Finished generating warnings for {} in {}", commit, this);
        return warnings;
    }

    @SneakyThrows
    public void walkCommitStructure(String path, Collection<? super IllegalFileWarning> warnings){

        if(folderViolation(path)){
            IllegalFileWarning warning = new IllegalFileWarning();
            warning.setFileName(path);
            warning.setCommit(commit);
            warnings.add(warning);
        }

        Map<String,EntryType> directory = getGitCommit(commit).showTree(path);
        directory.entrySet().stream()
                .filter(entry -> entry.getValue().equals(EntryType.FOLDER))
                .forEach(entry-> walkCommitStructure(path+entry.getKey(), warnings));

        directory.entrySet().stream()
                .filter(entry -> !entry.getValue().equals(EntryType.FOLDER))
                .map(Map.Entry::getKey)
                .filter(this::fileViolation)
                .map(file -> {
                    IllegalFileWarning warning = new IllegalFileWarning();
                    warning.setCommit(commit);
                    warning.setFileName(file);
                    return warning;
                })
                .forEach(warnings::add);
    }

    private boolean fileViolation(String path){
       return Stream.of(illegalExtensions).filter(path::endsWith).findAny().isPresent();
    }

    private boolean folderViolation(String path){
        return Stream.of(illegalFolders).filter(path::endsWith).findAny().isPresent();
    }
}
