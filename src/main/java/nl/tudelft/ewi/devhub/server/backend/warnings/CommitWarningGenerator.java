package nl.tudelft.ewi.devhub.server.backend.warnings;

import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.Warning;

import java.util.List;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface CommitWarningGenerator<T extends Warning> {

    /**
     * Generate warnings for a {@link Commit}.
     *
     * @param commit Commit to generate warnings for
     * @return a list of warnings
     */
    List<T> generateWarnings(Commit commit);

}
