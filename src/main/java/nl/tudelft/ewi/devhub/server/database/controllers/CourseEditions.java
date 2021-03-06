package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.query.ListSubQuery;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static nl.tudelft.ewi.devhub.server.database.entities.QCourseEdition.courseEdition;
import static nl.tudelft.ewi.devhub.server.database.entities.QGroup.group;

public class CourseEditions extends Controller<CourseEdition> {

	@Inject
	public CourseEditions(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public CourseEdition find(long id) {
		return ensureNotNull(query().from(courseEdition)
			.where(courseEdition.id.eq(id))
			.singleResult(courseEdition), "Could not find course with id: " + id);
	}

	@Transactional
	public CourseEdition find(String courseCode, String period) {
		Preconditions.checkNotNull(courseCode);
		Preconditions.checkNotNull(period);

		return ensureNotNull(query().from(courseEdition)
			.where(courseEdition.code.equalsIgnoreCase(period)
				.and(courseEdition.course.code.equalsIgnoreCase(courseCode)))
			.singleResult(courseEdition), "Could not find course with code: " + courseCode);
	}

    @Transactional
    public List<CourseEdition> listParticipatingCourses(User user) {
		return query().from(group)
				.where(group.members.contains(user))
				.list(group.courseEdition);
    }

	/**
	 * @param user
	 * @return Get the assisting {@link CourseEdition CourseEditions} for a {@link User}.
	 * @deprecated Use {@link User#getAssists()} instead.
	 * @see User#getAssists()
	 */
	@Deprecated
    @Transactional
    public Collection<CourseEdition> listAssistingCourses(User user) {
		return user.getAssists();
    }

	private JPAQuery activeCoursesBaseQuery() {
		Date now = new Date();
		return query().from(courseEdition)
			.where(courseEdition.timeSpan.start.before(now)
				.and(courseEdition.timeSpan.end.isNull()
					.or(courseEdition.timeSpan.end.after(now))));
	}

	@Transactional
	public List<CourseEdition> listActiveCourses() {
		return activeCoursesBaseQuery()
			.orderBy(courseOrdering())
			.list(courseEdition);
	}

	@Transactional
	public CourseEdition getActiveCourseEdition(Course course) {
		return ensureNotNull(activeCoursesBaseQuery()
			.where(courseEdition.course.eq(course))
			.singleResult(courseEdition), "Could not find active course edition for " + course);
	}

	@Transactional
	public List<CourseEdition> listNotYetParticipatedCourses(User user) {
		Preconditions.checkNotNull(user);

		ListSubQuery<CourseEdition> participatingCourses = new JPASubQuery().from(group)
			.where(group.members.contains(user))
			.list(group.courseEdition);

		return activeCoursesBaseQuery()
			.where(courseEdition.notIn(participatingCourses))
			.orderBy(courseOrdering())
			.list(courseEdition);
	}

	private static OrderSpecifier<?>[] courseOrdering() {
		return new OrderSpecifier<?>[] {
			courseEdition.course.code.asc(),
			courseEdition.course.name.asc(),
			courseEdition.timeSpan.start.asc()
		};
	}
}
