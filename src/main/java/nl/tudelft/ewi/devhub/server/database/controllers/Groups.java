package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static nl.tudelft.ewi.devhub.server.database.entities.QGroup.group;



public class Groups extends Controller<Group> {

	@Inject
	public Groups(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public Group findByRepoName(String repoName) {
		Preconditions.checkNotNull(repoName);
		Group res = query().from(group)
			.where(group.repository.repositoryName.equalsIgnoreCase(repoName))
			.singleResult(group);

		return ensureNotNull(res, "Could not find group by repository name: " + repoName);
	}

	@Transactional
	public List<Group> find(CourseEdition course) {
		Preconditions.checkNotNull(course);
		return query().from(group)
			.where(group.courseEdition.id.eq(course.getId()))
			.orderBy(group.groupNumber.asc())
			.list(group);
	}

	@Transactional
	public Group find(CourseEdition course, long groupNumber) {
		Preconditions.checkNotNull(course);
		Group res = query().from(group)
			.where(group.courseEdition.id.eq(course.getId()))
			.where(group.groupNumber.eq(groupNumber))
			.singleResult(group);

		return ensureNotNull(res, "Could not find group by course: " + course + " and groupNumber: " + groupNumber);
	}

	@Transactional
	public Group find(CourseEdition courseEdition, User user) {
		Preconditions.checkNotNull(courseEdition);
		Preconditions.checkNotNull(user);

		return ensureNotNull(query().from(group)
			.where(group.members.contains(user)
			.and(group.courseEdition.eq(courseEdition)))
			.singleResult(group),
			String.format("Could not find group by course %s and user %s", courseEdition, user));
	}

}
