package nl.tudelft.ewi.devhub.server.database.entities.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Files should not be too large
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@DiscriminatorValue("large-file")
@EqualsAndHashCode(callSuper = true)
public class LargeFileWarning extends FileWarning {

    private static final String RESOURCE_KEY = "warning.large-file";

    @Override
    public String getMessage(Translator translator) {
        return translator.translate(RESOURCE_KEY, getFileName());
    }

}
