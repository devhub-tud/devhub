package nl.tudelft.ewi.devhub.server.database.entities.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Marks successive build failures for a repository
 *
 * We can find successive build failures using the following algorithm:
 *
 * <ul>
 *     <li>Given a build result for a commit with failing tests,</li>
 *     <li>and one of the parent commits has failing tests,</li>
 *     <li>a {@code SuccessiveBuildfailure} should be generated.</li>
 * </ul>
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@DiscriminatorValue("successive-build-failure")
@EqualsAndHashCode(callSuper = true)
public class SuccessiveBuildfailure extends CommitWarning {

    private static final String RESOURCE_KEY = "warning.successive-build-failures";

    @Override
    public String getMessage(Translator translator) {
        return translator.translate(RESOURCE_KEY);
    }

}
