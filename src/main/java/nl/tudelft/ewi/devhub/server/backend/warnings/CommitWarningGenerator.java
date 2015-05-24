package nl.tudelft.ewi.devhub.server.backend.warnings;

import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;

import java.util.Collection;

/**
 * The {@code CommitWarningGenerator} is a base interface for objects that create
 * {@link CommitWarning CommitWarnings} from a type of attachment, such as a
 * push hook or an XML build report.
 *
 * @param <T> The type of warning that will be generated
 * @param <A> The type of attachment that this generator consumes
 * @author Jan-Willem Gmelig Meyling
 */
public interface CommitWarningGenerator<T extends CommitWarning, A> {

    /**
     * Generate warnings for a {@link Commit}.
     *
     * @param commit Commit to generate warnings for
     * @return a {@code Collection} of warnings
     */
    Collection<T> generateWarnings(Commit commit, A attachment);

}
