package nl.tudelft.ewi.devhub.server.database.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import java.util.Date;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@Entity
@Table(name = "build_results")
public class BuildResult {

	public static BuildResult newBuildResult(Group group, String commit) {
		BuildResult result = new BuildResult();
		result.setQueued(new Date());
		result.setRepository(group);
		result.setCommitId(commit);
		result.setSuccess(null);
		result.setLog(null);
		return result;
	}

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "repository_id")
	private Group repository;

	@NotEmpty
	@Column(name = "commit_id")
	private String commitId;

	@Column(name = "queued")
	private Date queued;
	
	@Column(name = "started")
	private Date started;
	
	@Column(name = "completed")
	private Date completed;

	@Column(name = "success")
	private Boolean success;

	@Column(name = "log")
	private String log;

}
