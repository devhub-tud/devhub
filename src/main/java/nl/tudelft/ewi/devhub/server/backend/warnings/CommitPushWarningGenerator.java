package nl.tudelft.ewi.devhub.server.backend.warnings;

import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;

/**
 * A {@code CommitPushWarningGenerator} is a {@link CommitWarningGenerator} that hooks onto a
 * {@link GitPush} event. This level of abstraction is used in order to use multibindings to
 * compute all push related {@link CommitWarning CommitWarnings} in batch.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface CommitPushWarningGenerator<T extends CommitWarning> extends CommitWarningGenerator<T, GitPush> {
}
