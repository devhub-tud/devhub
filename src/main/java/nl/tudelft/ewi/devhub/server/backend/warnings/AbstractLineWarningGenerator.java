package nl.tudelft.ewi.devhub.server.backend.warnings;

import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LineWarning;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.BlameModel;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@code AbstractLineWarningGenerator} contains logic that can be shared for generating {@link LineWarning
 * LineWarnings}. For every file in the commit it retrieves the {@code BlameModel}, in order to set the warnings
 * on the original commits.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public abstract class AbstractLineWarningGenerator<T extends LineWarning, V> implements CommitWarningGenerator<T> {

    private final GitServerClient gitServerClient;
    private final Commits commits;

    protected AbstractLineWarningGenerator(final GitServerClient gitServerClient, final Commits commits) {
        this.gitServerClient = gitServerClient;
        this.commits = commits;
    }

    @Override
    public List<T> generateWarnings(final Commit commit) {
        final Repository repository = retrieveRepository(commit.getRepository());
        return getStream().flatMap(v -> {
            String filePath = filePathFor(v);
            BlameModel blameModel = retrieveBlameModel(repository, commit.getCommitId(), filePath);
            return map(commit, v).map(a -> blameSource(a, blameModel));
        })
        .filter(warning -> warning.getCommit().equals(commit))
        .collect(Collectors.toList());
    }

    /**
     * Retrieve the repository for this group
     * @param group The current group
     * @return Repository for the group
     */
    @SneakyThrows
    protected Repository retrieveRepository(final Group group) {
        return gitServerClient.repositories().retrieve(group.getRepositoryName());
    }

    /**
     * Retrieve the {@code BlameModel} for a file at a certain commit
     * @param repository repository to use
     * @param commitId commit to use
     * @param path path of the file
     * @return BlameModel for the file
     */
    @SneakyThrows
    protected static BlameModel retrieveBlameModel(final Repository repository, final String commitId, final String path) {
        return repository.retrieveCommit(commitId).blame(path);
    }

    /**
     * Set the correct source for the warning
     * @param value {@code Warning} to edit
     * @param blameModel {@code BlameModel} for the current file
     * @return edited {@code Warning}
     */
    protected T blameSource(T value, BlameModel blameModel) {
        Source source = value.getSource();
        int sourceLineNumber = source.getSourceLineNumber();

        BlameModel.BlameBlock block = blameModel.getBlameBlock(sourceLineNumber);
        String sourceCommitId = block.getFromCommitId();
        Commit sourceCommit = commits.ensureExists(source.getSourceCommit().getRepository(), sourceCommitId);

        source.setSourceCommit(sourceCommit);
        source.setSourceFilePath(block.getFromFilePath());
        source.setSourceLineNumber(block.getFromLineNumber(sourceLineNumber));
        return value;
    }

    /**
     * Convert the given element to a stream of warnings
     * @param commit Current commit
     * @param element element
     * @return a {@link Stream} of {@code Warnings}
     */
    protected abstract Stream<T> map(final Commit commit, V element);

    /**
     * Get the initial stream of files
     *
     * @return stream of files
     */
    protected abstract Stream<V> getStream();

    /**
     * Return the file path for the file in the stream
     * @param value Stream entity
     * @return file path for the entity
     */
    protected abstract String filePathFor(V value);


}
