package nl.devhub.server.database.controllers;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import nl.devhub.database.entities.QProject;
import nl.devhub.server.database.entities.Project;

public class Projects extends Controller<Project> {

	@Inject
	public Projects(EntityManager entityManager) {
		super(entityManager);
	}
	
	public Project find(long id) {
		return query().from(QProject.project)
				.where(QProject.project.id.eq(id))
				.singleResult(QProject.project);
	}
	
}
