package nl.tudelft.ewi.devhub.server.database.controllers;

import java.util.List;

import javax.persistence.EntityManager;

import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.comments.IssueComment;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;

import static nl.tudelft.ewi.devhub.server.database.entities.comments.QIssueComment.issueComment;

public class IssueComments extends Controller<IssueComment> {

	public IssueComments(EntityManager entityManager) {
		super(entityManager);
	}
	

}
