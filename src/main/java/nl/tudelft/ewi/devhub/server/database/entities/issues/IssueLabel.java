package nl.tudelft.ewi.devhub.server.database.entities.issues;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;

@Data
@Entity
@Table(name="repository_labels")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString(includeFieldNames=true)
@IdClass(IssueLabel.LabelId.class)
public class IssueLabel {
	
    @Data
    @NoArgsConstructor
	@AllArgsConstructor
    public static class LabelId implements Serializable {

        private long repository;

        private long labelId;

    }
   
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="label_id")
	private long labelId;
	
    @ManyToOne(optional = false)
	@JoinColumn(name = "repository_id")
    private RepositoryEntity repository;
	
	@Column(name="label_tag")
	private String tag;
	
	@Column(name="label_color")
	private int color;

}
