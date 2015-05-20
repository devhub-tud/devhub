package nl.tudelft.ewi.devhub.server.database.entities.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Usage of pull requests.
 * We can determine pull request usage with the following algorithm:
 *
 * <ul>
 *     <li>Given a push of a commit,</li>
 *     <li>that is equal to the master branch pointer,</li>
 *     <li>and not a merge commit,</li>
 *     <li>a {@code GitUsageWarning} should be generated.</li>
 * </ul>
 *
 * <i>This warning can not be reconstructed because this algorithm is dependent
 * on the current state of the master branch.</i>
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@DiscriminatorValue("git-usage")
@EqualsAndHashCode(callSuper = true)
public class GitUsageWarning extends CommitWarning {

    private static final String RESOURCE_KEY = "warning.git-usage-warning";

    @Override
    public String getMessage(Translator translator) {
        return translator.translate(RESOURCE_KEY);
    }

}
