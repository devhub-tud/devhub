package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.common.collect.Sets;
import com.google.inject.servlet.RequestScoped;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.IgnoredFileWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.EntryType;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by LC on 30/05/15.
 */
@RequestScoped
public class IgnoredFileWarningGenerator extends GitCommitPushWarningGenerator<IgnoredFileWarning> {

    private static final String[] extensions = {".iml",".class", ".bin", ".pdf", ".doc", ".docx", ".jar"};
    private static final String[] folders = { ".idea/", ".metadata/", ".settings/",
            ".project/", ".classpath/", "target/", "bin/", ".metadata/"};
    private Repository repository;

    @SneakyThrows
    @Override
    public Collection<IgnoredFileWarning> generateWarnings(Commit commit, GitPush attachment) {
        repository = getRepository(commit);
        Set<IgnoredFileWarning> warnings = Sets.newHashSet();
        walkCommitStructure("",commit.getCommitId(),warnings);
        return warnings;
    }
    @SneakyThrows
    public void walkCommitStructure(String path, String commit, Set<IgnoredFileWarning> warnings){

        if(folderViolation(path)){
            IgnoredFileWarning warning = new IgnoredFileWarning();
            warning.setFileName(path);
            warnings.add(warning);
        }

        Map<String,EntryType> directory = repository.listDirectoryEntries(commit,path);
        directory.entrySet().stream()
                .filter(entry -> entry.getValue().equals(EntryType.FOLDER))
                .forEach(entry-> walkCommitStructure(path+entry.getKey(),commit,warnings));

        directory.entrySet().stream()
                .filter(entry -> !entry.getValue().equals(EntryType.FOLDER))
                .map(Map.Entry::getKey)
                .filter(IgnoredFileWarningGenerator::fileViolation)
                .map(file -> {
                    IgnoredFileWarning warning = new IgnoredFileWarning();
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
