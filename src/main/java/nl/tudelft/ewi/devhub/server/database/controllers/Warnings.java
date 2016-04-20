package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LineWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.Warning;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.types.query.ListSubQuery;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static nl.tudelft.ewi.devhub.server.database.entities.warnings.QCommitWarning.commitWarning;
import static nl.tudelft.ewi.devhub.server.database.entities.warnings.QLineWarning.lineWarning;

/**
 * Data access object for Warnings
 * @author Jan-Willem Gmelig Meyling
 */
public class Warnings extends Controller<Warning> {

    @Inject
    public Warnings(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Check which commits for a repositoryEntity have warnings
     * @param repositoryEntity {@link Group} to check for
     * @return a list of commit ids that have warnings
     */
    @Transactional
    public Map<String, Long> commitsWithWarningsFor(RepositoryEntity repositoryEntity, Collection<String> commitIds) {
        return query().from(commitWarning)
            .where(commitWarning.repository.eq(repositoryEntity)
                .and(commitWarning.commit.commitId.in(commitIds))
                    .and(commitWarning.notIn(getLineWarningsNotIntroducedInCommit(repositoryEntity, commitIds))))
            .groupBy(commitWarning.commit.commitId)
            .map(commitWarning.commit.commitId, commitWarning.id.count());
    }

    protected ListSubQuery<LineWarning> getLineWarningsNotIntroducedInCommit(final RepositoryEntity repositoryEntity, final Collection<String> commitIds) {
        return new JPASubQuery().from(lineWarning)
            .where(lineWarning.repository.eq(repositoryEntity)
                .and(lineWarning.commit.commitId.in(commitIds)
                    .and(lineWarning.source.sourceCommit.commitId.ne(lineWarning.commit.commitId))))
            .list(lineWarning);
    }

    /**
     * Get the {@link CommitWarning CommitWarnings} for the given commit ids.
     * This excludes all the {@link LineWarning LineWarnings}.
     *
     * @param repositoryEntity {@link Group} to check for
     * @param commitId A commit id ids
     * @return a {@code List} of {@code CommitWarnings} for the given commits
     * @see #getWarningsFor(RepositoryEntity, Collection)
     */
    @Transactional
    public List<CommitWarning> getWarningsFor(final RepositoryEntity repositoryEntity, final String commitId) {
        return getWarningsFor(repositoryEntity, ImmutableList.of(commitId));
    }

    /**
     * Get the {@link CommitWarning CommitWarnings} for the given commit ids.
     * This excludes all the {@link LineWarning LineWarnings}.
     *
     * @param repositoryEntity {@link Group} to check for
     * @param commitIds A collection of commit ids
     * @return a {@code List} of {@code CommitWarnings} for the given commits
     */
    @Transactional
    public List<CommitWarning> getWarningsFor(final RepositoryEntity repositoryEntity, final Collection<String> commitIds) {
        return query().from(commitWarning)
                .where(commitWarning.repository.eq(repositoryEntity)
                        .and(commitWarning.commit.commitId.in(commitIds))
                        .and(commitWarning.notIn(getLineWarningsForQuery(repositoryEntity, commitIds))))
                .list(commitWarning);
    }

    protected ListSubQuery<LineWarning> getLineWarningsForQuery(final RepositoryEntity repositoryEntity, final Collection<String> commitIds) {
        return new JPASubQuery().from(lineWarning)
            .where(lineWarning.repository.eq(repositoryEntity)
                    .and(lineWarning.commit.commitId.in(commitIds)))
            .list(lineWarning);
    }

    /**
     * Get the {@link LineWarning LineWarnings} for the given commit ids.
     *
     * @param repositoryEntity {@link Group} to check for
     * @param commitId a Commit id
     * @return a {@code List} of {@code LineWarnings}
     */
    @Transactional
    public List<LineWarning> getLineWarningsFor(final RepositoryEntity repositoryEntity, final String commitId) {
        return query().from(lineWarning)
            .where(lineWarning.repository.eq(repositoryEntity)
                    .and(lineWarning.source.sourceCommit.commitId.eq(commitId)))
            .list(lineWarning);
    }


    /**
     * Get all CommitWarnings including subclasses as LineWarnings
     * @param repositoryEntity Group to retrieve for
     * @param commits Commits to filter for
     * @return a List of warnings
     */
    @Transactional
    protected List<CommitWarning> getAllCommitWarningsFor(final RepositoryEntity repositoryEntity, final Set<Commit> commits) {
        return query().from(commitWarning)
            .where(commitWarning.repository.eq(repositoryEntity)
                    .and(commitWarning.commit.in(commits)))
            .list(commitWarning);
    }

    /**
     * Persist a Set of {@link CommitWarning CommitWarnings}, but filter out the existing warnings
     * @param repositoryEntity Group to persist warnings for
     * @param warnings Set of warnings
     * @param <V> Type of warning to be persisted
     * @return The set of warnings that were persisted
     */
    @Transactional
    public <V extends CommitWarning> Set<V> persist(final RepositoryEntity repositoryEntity, final Set<V> warnings) {
        Preconditions.checkNotNull(repositoryEntity);
        Preconditions.checkNotNull(warnings);

        final Set<Commit> commits = getCommitsForWarnings(warnings);
        final List<CommitWarning> existingWarnings = getAllCommitWarningsFor(repositoryEntity, commits);
        return warnings.stream()
            .filter(warning -> !existingWarnings.contains(warning))
            .map(this::persist)
            .collect(toSet());
    }

    /**
     * Get the commits for a set of warnings
     * @param warnings Collection of warnings
     * @return Set of commits
     */
    protected static Set<Commit> getCommitsForWarnings(Collection<? extends CommitWarning> warnings) {
        assert warnings != null;
        return warnings.stream()
            .map(CommitWarning::getCommit)
            .map(Preconditions::checkNotNull)
            .collect(toSet());
    }

}
