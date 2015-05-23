package nl.tudelft.ewi.devhub.server.backend.warnings;

import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.Warning;

import java.util.Collection;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface CommitWarningGenerator<T extends Warning, A> {

    /**
     * Generate warnings for a {@link Commit}.
     *
     * @param commit Commit to generate warnings for
     * @return a collectoin of warnings
     */
    Collection<T> generateWarnings(Commit commit, A attachment);

}
