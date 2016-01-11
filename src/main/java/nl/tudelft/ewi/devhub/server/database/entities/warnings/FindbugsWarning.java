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
 * A {@code FindbugsWarning} is a warning generated from the Maven Findbugs plugin.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@ToString(callSuper = true)
@DiscriminatorValue("findbugs")
@EqualsAndHashCode(callSuper = true)
public class FindbugsWarning extends LineWarning {

    @NotEmpty
    @Column(name = "message", length = 255)
    private String message;

    @NotNull
    @Column(name = "priority")
    private int priority;

    @Override
    public String getMessage(Translator translator) {
        return message;
    }
}
