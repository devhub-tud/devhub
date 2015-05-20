package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.types.query.ListSubQuery;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LineWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.Warning;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

import static nl.tudelft.ewi.devhub.server.database.entities.warnings.QLineWarning.lineWarning;
import static nl.tudelft.ewi.devhub.server.database.entities.warnings.QCommitWarning.commitWarning;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class Warnings extends Controller<Warning> {

    @Inject
    public Warnings(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Check which commits for a group have warnings
     * @param group {@link Group} to check for
     * @return a list of commit ids that have warnings
     */
    @Transactional
    public Map<String, Long> commitsWithWarningsFor(Group group) {
        return query().from(commitWarning)
            .where(commitWarning.repository.eq(group))
            .groupBy(commitWarning.commit)
            .map(commitWarning.commit.commitId, commitWarning.count());
    }

    /**
     * Get the {@link CommitWarning CommitWarnings} for the given commit ids.
     * This excludes all the {@link LineWarning LineWarnings}.
     *
     * @param group {@link Group} to check for
     * @param commitId A commit id ids
     * @return a {@code List} of {@code CommitWarnings} for the given commits
     */
    @Transactional
    public List<CommitWarning> getWarningsFor(final Group group, final String commitId) {
        return getWarningsFor(group, ImmutableList.of(commitId));
    }


    /**
     * Get the {@link CommitWarning CommitWarnings} for the given commit ids.
     * This excludes all the {@link LineWarning LineWarnings}.
     *
     * @param group {@link Group} to check for
     * @param commitIds A collection of commit ids
     * @return a {@code List} of {@code CommitWarnings} for the given commits
     */
    @Transactional
    public List<CommitWarning> getWarningsFor(final Group group, final List<String> commitIds) {
        return query().from(commitWarning)
                .where(commitWarning.repository.eq(group)
                        .and(commitWarning.commit.commitId.in(commitIds))
                        .and(commitWarning.notIn(getLineWarningsForQuery(group, commitIds))))
                .list(commitWarning);
    }

    protected ListSubQuery<LineWarning> getLineWarningsForQuery(final Group group, final List<String> commitIds) {
        return new JPASubQuery().from(lineWarning)
            .where(lineWarning.repository.eq(group)
                    .and(lineWarning.commit.commitId.in(commitIds)))
            .list(lineWarning);
    }

    /**
     * Get the {@link LineWarning LineWarnings} for the given commit ids.
     *
     * @param group {@link Group} to check for
     * @param commitId a Commit id
     * @return a {@code List} of {@code LineWarnings}
     */
    @Transactional
    public List<LineWarning> getLineWarningsFor(final Group group, final String commitId) {
        return query().from(lineWarning)
            .where(lineWarning.repository.eq(group)
            .and(lineWarning.commit.commitId.eq(commitId)))
            .list(lineWarning);
    }

    /**
     * Get the {@link LineWarning LineWarnings} for the given commit ids.
     *
     * @param group {@link Group} to check for
     * @param commitIds A collection of commit ids
     * @return a {@code List} of {@code LineWarnings}
     */
    @Transactional
    public List<LineWarning> getLineWarningsFor(final Group group, final List<String> commitIds) {
        return query().from(lineWarning)
                .where(lineWarning.repository.eq(group)
                        .and(lineWarning.commit.commitId.in(commitIds)))
                .list(lineWarning);
    }

}
