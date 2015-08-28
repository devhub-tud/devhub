package nl.tudelft.ewi.devhub.server.backend.warnings;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LineWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.Warning;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.BlameModel;

import com.google.inject.persist.Transactional;

import javax.ws.rs.NotFoundException;
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
 * @param <A> The type of attachment that this {@link CommitWarningGenerator} consumes
 * @param <F> The type of file objects
 * @param <V> The type of unconverted violations for a file
 * @param <T> The type of warning that will be generated from the violation
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public abstract class AbstractLineWarningGenerator<A, F, V, T extends LineWarning> implements CommitWarningGenerator<T, A> {

    protected final GitServerClient gitServerClient;
    protected final Commits commits;

    protected AbstractLineWarningGenerator(final GitServerClient gitServerClient, final Commits commits) {
        this.gitServerClient = gitServerClient;
        this.commits = commits;
    }

    @Override
    @Transactional
    public Set<T> generateWarnings(final Commit commit, final A attachment) {
        final ScopedWarningGenerator scopedWarningGenerator = new ScopedWarningGenerator(commit);
        return getFiles(attachment)
            .flatMap(scopedWarningGenerator::mapWarningsForFile)
            .collect(toSet());
    }

    /**
     * The {@code ScopedWarningGenerator} is used by the
     * {@link AbstractLineWarningGenerator#generateWarnings(Commit, Object)} method
     * to produce warnings. It adds scope to the used lambda expressions.
     */
    class ScopedWarningGenerator {

        private final Commit commit;
        private final RepositoryEntity repositoryEntity;
        private final Repository repository;

        /**
         * The {@code ScopedWarningGenerator} is used by the
         * {@link AbstractLineWarningGenerator#generateWarnings(Commit, Object)} method
         * to produce warnings. It adds scope to the used lambda expressions.
         */
        public ScopedWarningGenerator(Commit commit) {
            this.commit = commit;
            this.repositoryEntity = commit.getRepository();
            this.repository = retrieveRepository(repositoryEntity);
        }

        /**
         * Map a file representation to a {@code Stream} of {@link Warning Warnings}.
         *
         * @param file File representation
         * @return A {@code Stream} of generated warnings
         */
        public Stream<T> mapWarningsForFile(final F file) {
            BlameModel blameModel;

            try {
                String filePath = filePathFor(file, commit);
                blameModel = retrieveBlameModel(repository, commit.getCommitId(), filePath);
            }
            catch (NotFoundException e) {
                log.warn(e.getMessage());
                return Stream.empty();
            }

            BlameSourceScope blameSourceGenerator = new BlameSourceScope(blameModel);
            return getViolations(file).map(blameSourceGenerator::map);
        }

        /**
         * For warnings to be generated, a {@link BlameModel} should be fetched from
         * the git server. The {@code BlameSourceScope} adds the {@code BlameModel} to
         * the scope for the used lambda expressions.
         */
        protected class BlameSourceScope {

            private final BlameModel blameModel;

            /**
             * For warnings to be generated, a {@link BlameModel} should be fetched from
             * the git server. The {@code BlameSourceScope} adds the {@code BlameModel} to
             * the scope for the used lambda expressions.
             */
            public BlameSourceScope(BlameModel blameModel) {
                this.blameModel = blameModel;
            }

            /**
             * Convert a violation into a generated warning.
             *
             * @param violation Violation to be converted
             * @return Converted violation
             */
            public T map(V violation) {
                T warning = mapToWarning(violation);
                Source source = getSource(violation);
                warning.setSource(source);
                return warning;
            }

            /**
             * Set the correct source for the warning.
             *
             * @param violation the violation that causes the warning
             * @return {@link Source} for the current {@code Warning}
             */
            protected Source getSource(final V violation) {
                int sourceLineNumber = getLineNumber(violation);

                BlameModel.BlameBlock block = blameModel.getBlameBlock(sourceLineNumber);
                String sourceCommitId = block.getFromCommitId();
                Commit sourceCommit = commits.ensureExists(repositoryEntity, sourceCommitId);

                Source source = new Source();
                source.setSourceCommit(sourceCommit);
                source.setSourceFilePath(block.getFromFilePath());
                source.setSourceLineNumber(block.getFromLineNumber(sourceLineNumber));
                return source;
            }
        }
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
     * Retrieve the repository for this repositoryEntity
     * @param group The current repositoryEntity
     * @return Repository for the repositoryEntity
     */
    @SneakyThrows
    protected Repository retrieveRepository(final RepositoryEntity group) {
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
