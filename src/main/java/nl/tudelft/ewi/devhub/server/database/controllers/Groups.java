package nl.tudelft.ewi.devhub.server.database.controllers;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.QGroup;

import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;

public class Groups extends Controller<Group> {

	@Inject
	public Groups(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public Group findByRepoName(String repoName) {
		Preconditions.checkNotNull(repoName);
		Group group = query().from(QGroup.group)
			.where(QGroup.group.repositoryName.equalsIgnoreCase(repoName))
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
		Preconditions.checkNotNull(course);
		return query().from(QGroup.group)
			.where(QGroup.group.course.id.eq(course.getId()))
			.orderBy(QGroup.group.groupNumber.asc())
			.list(QGroup.group);
	}

	@Transactional
	public Group find(Course course, long groupNumber) {
		Preconditions.checkNotNull(course);
		Group group = query().from(QGroup.group)
			.where(QGroup.group.course.id.eq(course.getId()))
			.where(QGroup.group.groupNumber.eq(groupNumber))
			.singleResult(QGroup.group);

		return ensureNotNull(group, "Could not find group by course: " + course + " and groupNumber: " + groupNumber);
	}

}
