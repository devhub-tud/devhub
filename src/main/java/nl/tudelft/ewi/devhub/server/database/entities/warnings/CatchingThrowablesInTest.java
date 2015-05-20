package nl.tudelft.ewi.devhub.server.database.entities.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * It's often a bad practise to catch exceptions in test cases, these exceptions
 * should instead be expected or prevented.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@DiscriminatorValue("catching-throwables")
@EqualsAndHashCode(callSuper = true)
public class CatchingThrowablesInTest extends CommitWarning {

    private static final String RESOURCE_KEY = "warning.test-catches-throwables";

    @Override
    public String getMessage(Translator translator) {
        return translator.translate(RESOURCE_KEY);
    }

}
