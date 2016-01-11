package nl.tudelft.ewi.devhub.server.database.entities.warnings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Size;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class FileWarning extends CommitWarning {

    @NotEmpty
    @Size(max = 50)
    @Column(name = "file_name", length = 50, nullable = false)
    private String fileName;

}
