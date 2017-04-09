package nl.tudelft.ewi.devhub.server.web.resources;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.CourseEventFeed;
import nl.tudelft.ewi.devhub.server.backend.CoursesBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The CoursesResource is used to view, create and edit {@link CourseEdition CourseEditions}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@Path("courses/{courseCode: (?!edit|setup)[^/]+?}/{editionCode : (?!edit|setup)[^/]+?}")
@RequestScoped
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class CourseEditionResource extends Resource {

	@Inject
	private CourseEditions courseEditions;

	@Inject @Named("current.user")
	private User currentUser;

	@Inject
	private CoursesBackend coursesBackend;

	@Inject
	private Groups groups;

	@Inject
	private TemplateEngine templateEngine;

	@Inject
	private CourseEventFeed courseEventFeed;

	@Context
	private HttpServletRequest request;

	/**
	 * Get an overview for a specific course. For users this redirects to the group page.
	 * Administrators and assistants are presented with an overview of groups and assignments.
	 * @param courseCode the course code for the course
	 * @param editionCode the course code for the course edition
	 * @return a Response containing the generated page
	 * @throws IOException if an I/O error occurs
	 */
	@GET
	public Response getCourse(@PathParam("courseCode") String courseCode,
							  @PathParam("editionCode") String editionCode) throws IOException {

		CourseEdition courseEdition = courseEditions.find(courseCode, editionCode);

		if(!(currentUser.isAdmin() || currentUser.isAssisting(courseEdition))) {
			try {
				Group group = groups.find(courseEdition, currentUser);
				return redirect(group.getURI());
			}
			catch (EntityNotFoundException e) {
				return redirect(Course.COURSE_BASE_PATH);
			}
		}

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", currentUser);
		parameters.put("course", courseEdition);
		parameters.put("courseEdition", courseEdition);
		parameters.put("groups", courseEdition.getGroups());

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("course-edition-view.ftl", locales, parameters));
	}

	/**
	 * Edit a course
	 * @param courseCode the course code for the course
	 * @param editionCode the course code for the course edition
	 * @param error an error message
	 * @return a Response containing the generated page
	 * @throws IOException if an I/O error occurs
	 */
	@GET
	@Path("edit")
	public Response getEditPage(@PathParam("courseCode") String courseCode,
								@PathParam("editionCode") String editionCode,
								@QueryParam("error") String error) throws IOException {

		if(!currentUser.isAdmin()) {
			throw new UnauthorizedException();
		}

		CourseEdition courseEdition = courseEditions.find(courseCode, editionCode);

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", currentUser);
		parameters.put("course", courseEdition);

		if(error != null)
			parameters.put("error", error);

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("course-edition-setup.ftl", locales, parameters));
	}

	/**
	 * Submit changes to a course
	 * @param courseCode the course code for the course
	 * @param editionCode the course code for the course edition
	 * @param courseName name for the course
	 * @param templateRepository template repository url for the course
	 * @param minGroupSize min group size for the course
	 * @param maxGroupSize max group size for the course
	 * @param buildTimeout build timeout for the course
	 * @return a Response containing the generated page
	 */
	@POST
	@Path("edit")
	public Response editCourse(@PathParam("courseCode") String courseCode,
							   @PathParam("editionCode") String editionCode,
							   @FormParam("name") String courseName,
							   @FormParam("template") String templateRepository,
							   @FormParam("min") Integer minGroupSize,
							   @FormParam("max") Integer maxGroupSize,
							   @FormParam("timeout") Integer buildTimeout) {

		if(!currentUser.isAdmin()) {
			throw new UnauthorizedException();
		}

		CourseEdition courseEdition = courseEditions.find(courseCode, editionCode);
		courseEdition.getCourse().setName(courseName);
		courseEdition.setTemplateRepositoryUrl(templateRepository);
		courseEdition.setMinGroupSize(minGroupSize);
		courseEdition.setMaxGroupSize(maxGroupSize);
		courseEdition.getBuildInstruction().setBuildTimeout(buildTimeout);

		try {
			coursesBackend.mergeCourse(courseEdition);
		}
		catch (ConstraintViolationException e) {
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			if(violations.isEmpty()) {
				return redirect(courseEdition.getURI().resolve("/edit?error=error.course-create-error"));
			}
			return redirect(courseEdition.getURI().resolve("/edit?error=" + violations.iterator().next().getMessage()));
		}
		catch (Exception e) {
			return redirect(courseEdition.getURI().resolve("/edit?error=error.course-create-error"));
		}

		return redirect(Course.COURSE_BASE_PATH);
	}

	/**
	 * Get an event feed for the course.
	 * @param courseCode the course code for the course
	 * @param editionCode the course code for the course edition
	 * @param limit The maximal number of events to retrieve
	 * @return a Response containing the generated page
	 * @throws IOException if an I/O error occurs
     */
	@GET
	@Path("feed")
	public Response getEventFeed(@PathParam("courseCode") String courseCode,
	                             @PathParam("editionCode") String editionCode,
	                             @QueryParam("limit") @DefaultValue("100") int limit) throws IOException {


		CourseEdition courseEdition = courseEditions.find(courseCode, editionCode);

		if(!currentUser.isAdmin() && !currentUser.isAssisting(courseEdition)) {
			throw new UnauthorizedException();
		}

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", currentUser);
		parameters.put("course", courseEdition);
		parameters.put("events", courseEventFeed.getEventsFor(courseEdition, limit));

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("courses/course-feed.ftl", locales, parameters));
	}

}
