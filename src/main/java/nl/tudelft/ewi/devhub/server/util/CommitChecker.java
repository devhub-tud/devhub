package nl.tudelft.ewi.devhub.server.util;

import lombok.Data;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Group;

import javax.persistence.EntityNotFoundException;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
public class CommitChecker {
    private final Group group;
    private final BuildResults buildResults;

    public boolean hasFinished(String commitId) {
        try {
            BuildResult buildResult = buildResults.find(group, commitId);
            return buildResult.getSuccess() != null;
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    public boolean hasStarted(String commitId) {
        try {
            buildResults.find(group, commitId);
            return true;
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    public boolean hasSucceeded(String commitId) {
        BuildResult buildResult = buildResults.find(group, commitId);
        return buildResult.getSuccess();
    }

    public String getLog(String commitId) {
        BuildResult buildResult = buildResults.find(group, commitId);
        return buildResult.getLog();
    }
}
