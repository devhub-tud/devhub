package nl.tudelft.ewi.devhub.server.database.entities.issues;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;

@Data
@Entity
@Table(name="repository_labels", uniqueConstraints = {
	@UniqueConstraint(name = "unique_tag_name_in_repo", columnNames = {"repository_id", "tag"})
})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString(includeFieldNames=true)
public class IssueLabel {
   
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="label_id")
	private long labelId;
	
    @ManyToOne(optional = false)
	@JoinColumn(name = "repository_id", referencedColumnName = "id", nullable = false)
    private RepositoryEntity repository;
	
	@Column(name="tag")
	private String tag;
	
	@Column(name="color")
	private int color;

	public String getColorAsHexString() {
		return String.format("#%06X", 0xFFFFFF & color);
	}

}
