package nl.devhub.server.database.controllers;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import nl.devhub.server.database.entities.Project;
import nl.devhub.server.database.entities.QProject;

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

	public Project find(String projectCode) {
		return query().from(QProject.project)
				.where(QProject.project.code.eq(projectCode))
				.where(QProject.project.end.isNull())
				.singleResult(QProject.project);
	}
	
}
