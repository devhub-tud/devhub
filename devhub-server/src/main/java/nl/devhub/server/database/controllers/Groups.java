package nl.devhub.server.database.controllers;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import nl.devhub.database.entities.QGroup;
import nl.devhub.server.database.entities.Group;
import nl.devhub.server.database.entities.Project;

public class Groups extends Controller<Group> {

	@Inject
	public Groups(EntityManager entityManager) {
		super(entityManager);
	}
	
	public List<Group> find(Project project) {
		return query().from(QGroup.group)
				.where(QGroup.group.project.id.eq(project.getId()))
				.list(QGroup.group);
	}
	
	public Group find(Project project, long groupId) {
		return query().from(QGroup.group)
				.where(QGroup.group.project.id.eq(project.getId()))
				.where(QGroup.group.groupId.eq(groupId))
				.singleResult(QGroup.group);
	}
	
}
