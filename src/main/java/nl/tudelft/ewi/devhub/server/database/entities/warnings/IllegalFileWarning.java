package nl.tudelft.ewi.devhub.server.database.entities.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Ignored files should not be commited.
 * Furthermore, the .gitignore should not be removed or cleaned.
 *
 * @author Liam Clark
 */
@Data
@Entity
@DiscriminatorValue("illegal-file")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IllegalFileWarning extends FileWarning {

    private static final String RESOURCE_KEY = "warning.committed-illegal-files";

    @Override
    public String getMessage(Translator translator) {
        return translator.translate(RESOURCE_KEY, getFileName());
    }

}
