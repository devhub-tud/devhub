package nl.tudelft.ewi.devhub.server.database.entities.warnings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * A Commit warning is a warning attached to a specific commit.
 * We intend to persist only warnings that were introduced by the specified commit,
 * in order to not give misleading impressions.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class CommitWarning extends Warning {

    @NotNull
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(formula = @JoinFormula(value = "repository_name", referencedColumnName = "repository_name")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "commit_id", referencedColumnName = "commit_id"))
    })
    private Commit commit;

}
