package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.Issues;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

/**
 * 
 * @author Aron Zwaan
 *
 */
@Slf4j
public class IssueBackend {
	
	private final Issues issues;
	
	@Inject
	public IssueBackend(final Issues issues){
		Preconditions.checkNotNull(issues);
		this.issues = issues;
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

}
