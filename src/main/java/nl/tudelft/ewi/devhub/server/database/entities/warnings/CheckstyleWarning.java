package nl.tudelft.ewi.devhub.server.database.entities.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * A {@code CheckstyleWarning} is a warning generated from the Maven Checkstyle plugin.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@ToString(callSuper = true)
@DiscriminatorValue("checkstyle")
@EqualsAndHashCode(callSuper = true)
public class CheckstyleWarning extends LineWarning {

    @NotEmpty
    @Column(name = "message")
    private String message;

    @Column(name = "severity")
    private String severity;

    @Override
    public String getMessage(Translator translator) {
        return message;
    }

}
