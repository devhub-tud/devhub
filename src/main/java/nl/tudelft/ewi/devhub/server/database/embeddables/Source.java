package nl.tudelft.ewi.devhub.server.database.embeddables;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * A {@code Source} defines the origin of a line and can be used to reference
 * the line from warnings and comments.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Source {

    /*
     * Sadly @JoinColumns doesn't support overlapping parts for ORM v3 and v4
     * see : http://stackoverflow.com/a/9567902/2104280
     * and : https://hibernate.atlassian.net/browse/HHH-6221
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(formula = @JoinFormula(value = "repository_name", referencedColumnName = "repository_name")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "source_commit_id", referencedColumnName = "commit_id"))
    })
    private Commit sourceCommit;

    @NotNull
    @Column(name = "source_line_number")
    private Integer sourceLineNumber;

    @NotNull
    @Column(name = "source_file_path")
    private String sourceFilePath;

    public boolean equals(final String commitId, final String sourceFilePath, final Integer sourceLineNumber) {
        return this.sourceCommit.getCommitId().equals(commitId) &&
            this.sourceFilePath.equals(sourceFilePath) &&
            this.sourceLineNumber.equals(sourceLineNumber);
    }

}
