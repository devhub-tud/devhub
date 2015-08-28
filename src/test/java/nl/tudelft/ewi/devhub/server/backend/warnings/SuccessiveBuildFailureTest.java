package nl.tudelft.ewi.devhub.server.backend.warnings;

import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.SuccessiveBuildFailure;
import nl.tudelft.ewi.git.client.Commit;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Repository;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(Theories.class)
public class SuccessiveBuildFailureTest {

    private final static String COMMIT_ID = "abcd";


	@Mock private GroupRepository groupRepository;
	@Mock private nl.tudelft.ewi.devhub.server.database.entities.Commit commitEntity;
    @Mock private Group group;
    @Mock private Commit commit;
    @Mock private Repository repository;
    @Mock private Repositories repositories;
    @Mock private GitServerClient gitServerClient;
    @Mock private BuildResults buildResults;
    @InjectMocks private SuccessiveBuildFailureGenerator generator;

    @DataPoint public static BuildResult succeeding = withSuccess(true);
    @DataPoint public static BuildResult failing = withSuccess(false);

    private SuccessiveBuildFailure warning;

    @Before
    public void beforeTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(commitEntity.getCommitId()).thenReturn(COMMIT_ID);
		when(commitEntity.getRepository()).thenReturn(groupRepository);
		when(group.getRepository()).thenReturn(groupRepository);
        when(gitServerClient.repositories()).thenReturn(repositories);
        when(repositories.retrieve(anyString())).thenReturn(repository);
        when(repository.retrieveCommit(COMMIT_ID)).thenReturn(commit);
        when(commit.getParents()).thenReturn(new String[0]);

        warning = new SuccessiveBuildFailure();
        warning.setCommit(commitEntity);
    }

    /**
     * Single parent, both the commit and the parent commit are failing
     * @param current commit
     * @param parent parent commit
     */
    @Theory
    public void testSuccessiveBuildFailure(BuildResult current, BuildResult parent) {
        assumeTrue(parent.hasFailed());
        assumeTrue(current.hasFailed());

        when(buildResults.findBuildResults(eq(groupRepository), any()))
                .thenReturn(ImmutableMap.of(COMMIT_ID, parent));
        Set<SuccessiveBuildFailure> warnings = generator.generateWarnings(commitEntity, current);
        assertThat(warnings, contains(warning));
    }

    /**
     * Merge commit, one of the parents and the merge commit are failing
     * @param current commit
     * @param parent parent commit
     * @param parent2 second parent
     */
    @Theory
    public void testSuccessiveBuildFailure(BuildResult current, BuildResult parent, BuildResult parent2) {
        assumeTrue(parent.hasFailed() || parent2.hasFailed());
        assumeTrue(current.hasFailed());

        when(buildResults.findBuildResults(eq(groupRepository), any()))
                .thenReturn(ImmutableMap.of("parent", parent, "parent2", parent2));
        Set<SuccessiveBuildFailure> warnings = generator.generateWarnings(commitEntity, current);
        assertThat(warnings, contains(warning));
    }

    /**
     * Merge commit, none of the parents are failing
     * @param current commit
     * @param parent parent commit
     * @param parent2 second parent
     */
    @Theory
    public void testNotSuccessiveBuildFailure(BuildResult current, BuildResult parent, BuildResult parent2) {
        assumeTrue(parent.hasSucceeded() && parent2.hasSucceeded());

        when(buildResults.findBuildResults(eq(groupRepository), any()))
                .thenReturn(ImmutableMap.of("parent", parent, "parent2", parent2));
        Set<SuccessiveBuildFailure> warnings = generator.generateWarnings(commitEntity, current);
        assertEquals(warnings, Collections.<SuccessiveBuildFailure>emptySet());
    }

    /**
     * Commit, parent not failing
     * @param current commit
     * @param parent parent commit
     * @param parent
     */
    @Theory
    public void testNotSuccessiveBuildFailure(BuildResult current, BuildResult parent) {
        assumeTrue(parent.hasSucceeded());

        when(buildResults.findBuildResults(eq(groupRepository), any()))
                .thenReturn(ImmutableMap.of("parent", parent));
        Set<SuccessiveBuildFailure> warnings = generator.generateWarnings(commitEntity, current);
        assertEquals(warnings, Collections.<SuccessiveBuildFailure>emptySet());
    }

    /**
     * The initial commit can never be a successive build failure
     * @param current commit
     */
    @Theory
    public void testNotSuccessiveBuildFailure(BuildResult current) {
        when(buildResults.findBuildResults(eq(groupRepository), any()))
                .thenReturn(ImmutableMap.of());
        Set<SuccessiveBuildFailure> warnings = generator.generateWarnings(commitEntity, current);
        assertEquals(warnings, Collections.<SuccessiveBuildFailure>emptySet());
    }

    private static BuildResult withSuccess(Boolean success) {
        BuildResult buildResult = new BuildResult();
        buildResult.setSuccess(success);
        return buildResult;
    }

}
