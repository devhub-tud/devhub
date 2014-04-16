package nl.tudelft.ewi.devhub.server.database.controllers;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.QGroup;

import com.google.inject.persist.Transactional;

public class Groups extends Controller<Group> {

	@Inject
	public Groups(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public Group findByRepoName(String repoName) {
		Group group = query().from(QGroup.group)
			.where(QGroup.group.repositoryName.eq(repoName))
			.singleResult(QGroup.group);

		return ensureNotNull(group, "Could not find group by repository name: " + repoName);
	}

	@Transactional
	public Group find(long groupId) {
		Group group = query().from(QGroup.group)
			.where(QGroup.group.groupId.eq(groupId))
			.singleResult(QGroup.group);

		return ensureNotNull(group, "Could not find group by group ID: " + groupId);
	}

	@Transactional
	public List<Group> find(Course course) {
		return query().from(QGroup.group)
			.where(QGroup.group.course.id.eq(course.getId()))
			.orderBy(QGroup.group.groupNumber.asc())
			.list(QGroup.group);
	}

	@Transactional
	public Group find(Course course, long groupNumber) {
		Group group = query().from(QGroup.group)
			.where(QGroup.group.course.id.eq(course.getId()))
			.where(QGroup.group.groupNumber.eq(groupNumber))
			.singleResult(QGroup.group);

		return ensureNotNull(group, "Could not find group by course: " + course + " and groupNumber: " + groupNumber);
	}

}
