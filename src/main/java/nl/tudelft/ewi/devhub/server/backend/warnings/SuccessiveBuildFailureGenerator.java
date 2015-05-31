package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.SuccessiveBuildFailure;
import nl.tudelft.ewi.git.client.GitServerClient;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@code SuccessiveBuildFailureGenerator} is triggered when a {@link BuildResult} is retrieved.
 * It generates a {@link SuccessiveBuildFailure} when the built commit is a successive build failure.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class SuccessiveBuildFailureGenerator
extends AbstractCommitWarningGenerator<SuccessiveBuildFailure, BuildResult> {

    private final BuildResults buildResults;

    @Inject
    public SuccessiveBuildFailureGenerator(GitServerClient gitServerClient, BuildResults buildResults) {
        super(gitServerClient);
        this.buildResults = buildResults;
    }

    @Override
    public Set<SuccessiveBuildFailure> generateWarnings(Commit commit, BuildResult attachment) {
        if(!attachment.hasFailed()) {
            return Collections.emptySet();
        }

        Collection<String> commitIds = Lists.newArrayList(getGitCommit(commit).getParents());
        Map<?, BuildResult> builds = buildResults.findBuildResults(commit.getRepository(), commitIds);
        return mapToWarnings(commit, builds);
    }

    protected Set<SuccessiveBuildFailure> mapToWarnings(Commit commit, Map<?, BuildResult> builds) {
        return builds.values().stream()
            .filter(BuildResult::hasFailed)
            .limit(1)
            .map(buildFailure -> {
                SuccessiveBuildFailure warning = new SuccessiveBuildFailure();
                warning.setCommit(commit);
                return warning;
            })
            .collect(Collectors.toSet());
    }


}
