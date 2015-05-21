package nl.tudelft.ewi.devhub.server.database.entities.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;

import javax.persistence.Embedded;
import javax.persistence.Entity;

/**
 * A {@code LineWarning} is a {@link CommitWarning} for which we know
 * the exact line in a file that causes the warning.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class LineWarning extends CommitWarning {

    @Embedded
    private Source source;

    public void setSource(final Source source) {
        this.source = source;
        this.setCommit(source.getSourceCommit());
    }
}
