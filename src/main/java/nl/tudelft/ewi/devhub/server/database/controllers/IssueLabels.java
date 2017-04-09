package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.persistence.EntityManager;

import com.google.inject.Inject;

import nl.tudelft.ewi.devhub.server.database.entities.issues.IssueLabel;

public class IssueLabels extends Controller<IssueLabel>{

	@Inject
	public IssueLabels(EntityManager entityManager) {
		super(entityManager);
	}

}
