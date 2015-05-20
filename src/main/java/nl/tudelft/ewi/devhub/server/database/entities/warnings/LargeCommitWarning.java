package nl.tudelft.ewi.devhub.server.database.entities.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Commits should be small and touch only a few lines.
 * This also marks students that cloned an entire framework in their repository.
 *
 * We can find large commits using the following algorithm:
 *
 * <ul>
 *     <li>Given a push of a commit,</li>
 *     <li>that touches more than an arbitrarily amount of files,</li>
 *     <li>or has more than an arbitrarily amount of line changes,</li>
 *     <li>a {@code LargeCommitWarning} should be generated.</li>
 * </ul>
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@DiscriminatorValue("large-commit")
@EqualsAndHashCode(callSuper = true)
public class LargeCommitWarning extends CommitWarning {

    private static final String RESOURCE_KEY = "warning.large-commit";

    @Override
    public String getMessage(Translator translator) {
        return translator.translate(RESOURCE_KEY);
    }

}
