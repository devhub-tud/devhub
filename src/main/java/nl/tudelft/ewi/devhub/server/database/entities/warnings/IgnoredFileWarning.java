package nl.tudelft.ewi.devhub.server.database.entities.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.Size;

/**
 * Created by LC on 30/05/15.
 */

@Data
@Entity
@DiscriminatorValue("ignored-file")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IgnoredFileWarning extends CommitWarning {

    @Column(name= "file_name")
    @Size(max = 50)
    private String fileName;

    @Override
    public String getMessage(Translator translator) {
        return null;
    }
}
