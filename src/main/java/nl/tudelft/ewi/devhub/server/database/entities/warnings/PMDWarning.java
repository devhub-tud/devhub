package nl.tudelft.ewi.devhub.server.database.entities.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * A {@code PMDWarning} is a warning generated from the Maven PMD plugin.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@DiscriminatorValue("pmd")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PMDWarning extends LineWarning {

    private static final String RESOURCE_KEY = "warning.pmd";

    @NotEmpty
    @Column(name = "message")
    private String message;

    @NotEmpty
    @Column(name = "rule")
    private String rule;

    @NotNull
    @Column(name = "priority")
    private int priority;

    @Override
    public String getMessage(Translator translator) {
        return translator.translate(RESOURCE_KEY, message, rule);
    }

}
