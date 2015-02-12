package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.persistence.EntityManager;

import com.google.inject.Inject;

import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;

public class CommitComments extends Controller<CommitComment> {

	@Inject
	public CommitComments(final EntityManager entityManager) {
		super(entityManager);
	}

}
