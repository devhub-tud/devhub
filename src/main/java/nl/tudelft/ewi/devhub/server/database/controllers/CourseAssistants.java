package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import nl.tudelft.ewi.devhub.server.database.entities.CourseAssistant;

public class CourseAssistants extends Controller<CourseAssistant> {

	@Inject
	public CourseAssistants(EntityManager entityManager) {
		super(entityManager);
	}

}
