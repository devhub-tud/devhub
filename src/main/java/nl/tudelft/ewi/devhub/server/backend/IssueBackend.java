package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.IssueLabels;
import nl.tudelft.ewi.devhub.server.database.controllers.Issues;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.devhub.server.database.entities.issues.IssueLabel;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

/**
 * 
 * @author Aron Zwaan
 *
 */
@Slf4j
public class IssueBackend {
	
	private final Issues issues;	

	private final IssueLabels issueLabels;
	
	@Inject
	public IssueBackend(final Issues issues, final IssueLabels issueLabels){
		Preconditions.checkNotNull(issues);
		Preconditions.checkNotNull(issueLabels);
		this.issues = issues;
		this.issueLabels = issueLabels;
	}
	
	/**
	 * Creates a new Issue for a repository
	 * @param repository Repository that contains the issue
	 * @param issue The Issue to create
	 */
	@Transactional
	public void createIssue(RepositoryApi repository, Issue issue){
		log.info("Persisting issue {}", issue);
		issues.persist(issue);
	}
	
	@Transactional
	public IssueLabel addIssueLabelToRepository(RepositoryEntity repository, String tag, int color){
		log.info("Adding label {} to repository {}", tag, repository);
		IssueLabel label = new IssueLabel();
		label.setColor(color);
		label.setRepository(repository);
		label.setTag(tag);
		issueLabels.persist(label);
		return label;
	}

}
