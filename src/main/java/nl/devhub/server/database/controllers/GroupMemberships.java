package nl.devhub.server.database.controllers;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import nl.devhub.server.database.entities.Group;
import nl.devhub.server.database.entities.GroupMembership;
import nl.devhub.server.database.entities.QGroupMembership;

public class GroupMemberships extends Controller<GroupMembership> {

	@Inject
	public GroupMemberships(EntityManager entityManager) {
		super(entityManager);
	}

	public List<Group> listParticipating(long userId) {
		return query().from(QGroupMembership.groupMembership)
				.where(QGroupMembership.groupMembership.user.id.eq(userId))
				.where(QGroupMembership.groupMembership.group.project.start.isNotNull())
				.where(QGroupMembership.groupMembership.group.project.end.isNull())
				.orderBy(QGroupMembership.groupMembership.group.project.code.asc())
				.list(QGroupMembership.groupMembership.group);
	}
	
}
