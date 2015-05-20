package nl.tudelft.ewi.devhub.server.database.entities.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Ignored files should not be commited.
 * Furthermore, the .gitignore should not be removed or cleaned.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@DiscriminatorValue("committed-ignored-files")
@EqualsAndHashCode(callSuper = true)
public class CommitedIgnoredFiles extends CommitWarning {

    private static final String RESOURCE_KEY = "warning.committed-ignored-files";

    @Override
    public String getMessage(Translator translator) {
        return translator.translate(RESOURCE_KEY);
    }

}
