package nl.tudelft.ewi.devhub.server.database.controllers;

import java.util.List;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;

import static nl.tudelft.ewi.devhub.server.database.entities.issues.QIssue.issue;

public class Issues extends Controller<Issue> {

	@Inject
	public Issues(EntityManager em) {
		super(em);
	}
	
	@Transactional
	public List<Issue> findIssuesForRepo(RepositoryEntity repo){
		return query()
			.where(issue.repository.eq(repo))
			.list(issue);
	}

}
