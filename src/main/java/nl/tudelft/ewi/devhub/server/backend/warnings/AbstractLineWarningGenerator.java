package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.inject.persist.Transactional;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LineWarning;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.BlameModel;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * The {@code AbstractLineWarningGenerator} contains logic that can be shared for generating {@link LineWarning
 * LineWarnings}. For every file in the commit it retrieves the {@code BlameModel}, in order to set the warnings
 * on the original commits.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public abstract class AbstractLineWarningGenerator<A, F, V, T extends LineWarning> implements CommitWarningGenerator<T, A> {

    protected final GitServerClient gitServerClient;
    protected final Commits commits;

    protected AbstractLineWarningGenerator(final GitServerClient gitServerClient, final Commits commits) {
        this.gitServerClient = gitServerClient;
        this.commits = commits;
    }

    @Override
    public Set<T> generateWarnings(final Commit commit, final A attachment) {
        final Group group = commit.getRepository();
        final Repository repository = retrieveRepository(group);

        return getFiles(attachment).flatMap(v -> {
            String filePath = filePathFor(v, commit);
            BlameModel blameModel = retrieveBlameModel(repository, commit.getCommitId(), filePath);
            return getViolations(v).map(violation -> {
                T warning = mapToWarning(violation);
                Source source = blameSource(violation, group, blameModel);
                warning.setSource(source);
                return warning;
            });
        })
        .collect(toSet());
    }

    /**
     * Maven tools return absolute paths, we need them relative to the repository root.
     * @param path absolute path
     * @return relative path
     */
    protected static String getRelativePath(final String path) {
        return path.substring(path.indexOf("/src/") + 1);
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
     * @param violation the violation that causes the warning
     * @param group the {@link Group} that owns the repository
     * @param blameModel {@link BlameModel} for the current file
     * @return {@link Source} for the current {@code Warning}
     */
    @Transactional
    private final Source blameSource(final V violation, final Group group, final BlameModel blameModel) {
        int sourceLineNumber = getLineNumber(violation);

        BlameModel.BlameBlock block = blameModel.getBlameBlock(sourceLineNumber);
        String sourceCommitId = block.getFromCommitId();
        Commit sourceCommit = commits.ensureExists(group, sourceCommitId);

        Source source = new Source();
        source.setSourceCommit(sourceCommit);
        source.setSourceFilePath(block.getFromFilePath());
        source.setSourceLineNumber(block.getFromLineNumber(sourceLineNumber));
        return source;
    }

    /**
     * Get the line number for a violation
     * @param violation Violation that will be converted to a {@code Warning}
     * @return the line number for the warning
     */
    protected abstract int getLineNumber(V violation);

    /**
     * Fill the warning with parameters from the violation
     * @param violation Violation that will be converted to a {@code Warning}
     * @return the created {@code Warning}
     */
    protected abstract T mapToWarning(V violation);


    /**
     * Get the violations for a file
     * @param file File from the log
     * @return a {@code Stream} of violations
     */
    protected abstract Stream<V> getViolations(F file);

    /**
     * Get the initial stream of files
     *
     * @return stream of files
     */
    protected abstract Stream<F> getFiles(A attachment);

    /**
     * Return the file path for the file in the stream
     * @param value Stream entity
     * @param commit current commit
     * @return file path for the entity
     */
    protected abstract String filePathFor(F value, Commit commit);

    /**
     * Return an empty list if a stream is null.
     * @param input input stream
     * @param <P> Type of the stream
     * @return the input stream, or an empty stream if the stream was null
     */
    protected static <P>  List<P> emptyIfNull(final List<P> input) {
        if(input == null) {
            return Collections.emptyList();
        }
        return input;
    }

}
