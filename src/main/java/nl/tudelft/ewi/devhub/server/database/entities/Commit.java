package nl.tudelft.ewi.devhub.server.database.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name="commit")
@ToString(exclude="comments")
@EqualsAndHashCode(of={"repository", "commitId"})
public class Commit implements Serializable {

	@Id
	@ManyToOne
	@JoinColumn(name = "repository_name", referencedColumnName = "repository_name")
	private Group repository;
	
	@Id
	@Column(name = "commit_id")
	private String commitId;
	
	@OneToMany(mappedBy = "commit", fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	private List<CommitComment> comments;
	
}
