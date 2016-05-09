package nl.tudelft.ewi.devhub.server.web.resources;

import nl.tudelft.ewi.devhub.server.backend.AssignmentStats;
import nl.tudelft.ewi.devhub.server.backend.DeliveriesBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.Assignments;
import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.rubrics.Characteristic;
import nl.tudelft.ewi.devhub.server.database.entities.rubrics.Task;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;

import org.jboss.resteasy.spi.NotImplementedYetException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Created by jgmeligmeyling on 04/03/15.
 * @author Jan-Willem Gmleig Meyling
 */
@Path("courses/{courseCode}/{editionCode}/assignments")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class AssignmentsResource extends Resource {

    private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm";

	@Inject
    private TemplateEngine templateEngine;

    @Inject
    private CourseEditions courses;

    @Inject
    private Assignments assignmentsDAO;

    @Inject
    private DeliveriesBackend deliveriesBackend;

    @Inject
    private Deliveries deliveriesDAO;

    @Inject
    @Named("current.user")
    private User currentUser;

	@Context
	HttpServletRequest request;

	@Context
	HttpServletResponse response;

    /**
     * Get an overview of the courses
     * @param courseCode the course to create an assignment for
     * @param editionCode the course to create an assignment for
     * @return a Response containing the generated page
     */
    @GET
    public Response getOverviewPage(@PathParam("courseCode") String courseCode,
									@PathParam("editionCode") String editionCode) {
        throw new NotImplementedYetException();
    }

    /**
     * Present the user a form to create a new assignment
     * @param courseCode the course to create an assignment for
	 * @param editionCode the course to create an assignment for
     * @return a Response containing the generated page
     */
    @GET
    @Transactional
    @Path("create")
    public Response getCreatePage(@PathParam("courseCode") String courseCode,
								  @PathParam("editionCode") String editionCode,
                                  @QueryParam("error") String error) throws IOException {

        CourseEdition course = courses.find(courseCode, editionCode);
        if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
            throw new UnauthorizedException();
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("user", currentUser);
        parameters.put("course", course);

        if(error != null)
            parameters.put("error", error);

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("courses/assignments/create-assignment.ftl", locales, parameters));
    }

    /**
     * Submit a create assignment form
     * @param courseCode the course to create an assignment for
	 * @param editionCode the course to create an assignment for
     * @param name name for the assignment
     * @param summary summary for the assignment
     * @param dueDate due date for the assignment
     * @return a Response containing the generated page
     */
    @POST
    @Path("create")
    public Response createPage(@PathParam("courseCode") String courseCode,
							   @PathParam("editionCode") String editionCode,
                               @FormParam("id") Long assignmentId,
                               @FormParam("name") String name,
                               @FormParam("summary") String summary,
                               @FormParam("due-date") String dueDate) {

        CourseEdition course = courses.find(courseCode, editionCode);
        if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
            throw new UnauthorizedException();
        }

        if(assignmentsDAO.exists(course, assignmentId)) {
            return redirect(course.getURI().resolve("assignments/create?error=error.assignment-number-exists"));
        }

        Assignment assignment = new Assignment();
        assignment.setCourseEdition(course);
        assignment.setAssignmentId(assignmentId);
        assignment.setName(name);
        assignment.setSummary(summary);

        if(!Strings.isNullOrEmpty(dueDate)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
            try {
                assignment.setDueDate(simpleDateFormat.parse(dueDate));
            }
            catch (ParseException e) {
                return redirect(course.getURI().resolve("assignments/create?error=error.invalid-date-format"));
            }
        }

        try {
            assignmentsDAO.persist(assignment);
        }
        catch (ConstraintViolationException e) {
            Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
            if(violations.isEmpty()) {
                return redirect(course.getURI().resolve("assignments/create?error=error.assignment-create-error"));
            }
            return redirect(course.getURI().resolve("assignments/create?error=" + violations.iterator().next().getMessage()));
        }
        catch (Exception e) {
            return redirect(course.getURI().resolve("assignments/create?error=error.assignment-create-error"));
        }

        return redirect(course.getURI());
    }

    /**
     * An overview page for an assignment
     * @param courseCode the course to create an assignment for
	 * @param editionCode the course to create an assignment for
     * @param assignmentId the assignment id
     * @return a Response containing the generated page
     */
    @GET
    @Transactional
    @Path("{assignmentId : \\d+}")
    public Response getAssignmentPage(@PathParam("courseCode") String courseCode,
									  @PathParam("editionCode") String editionCode,
                                      @PathParam("assignmentId") Long assignmentId) throws IOException {

        CourseEdition course = courses.find(courseCode, editionCode);
        if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
            throw new UnauthorizedException();
        }

        Assignment assignment = assignmentsDAO.find(course, assignmentId);
        List<Delivery> lastDeliveries = deliveriesDAO.getLastDeliveries(assignment);
        AssignmentStats assignmentStats = deliveriesBackend.getAssignmentStats(assignment, lastDeliveries);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("user", currentUser);
        parameters.put("course", course);
        parameters.put("assignment", assignment);
        parameters.put("assignmentStats", assignmentStats);
        parameters.put("deliveryStates", Delivery.State.values());
        parameters.put("lastDeliveries", lastDeliveries);

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("courses/assignments/assignment-view.ftl", locales, parameters));
    }

    private final static String TEXT_CSV = "text/csv";
    private final static char CSV_FIELD_SEPARATOR = ',';
    private final static char CSV_ROW_SEPARATOR = '\n';

    /**
     * Download the grades for this assignment
     * @param courseCode the course to create an assignment for
	 * @param editionCode the course to create an assignment for
     * @param assignmentId the assignment id
     * @return a CSV file with the most recent deliveries
     */
    @GET
    @Transactional
    @Produces(TEXT_CSV)
    @Path("{assignmentId : \\d+}/deliveries/download")
    public String downloadAssignmentResults(@PathParam("courseCode") String courseCode,
											@PathParam("editionCode") String editionCode,
                                            @PathParam("assignmentId") Long assignmentId) throws IOException {

        CourseEdition course = courses.find(courseCode, editionCode);
        Assignment assignment = assignmentsDAO.find(course, assignmentId);

        if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
            throw new UnauthorizedException();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Assignment,NetId,StudentNo,Name,Group,State,Grade,").append(CSV_ROW_SEPARATOR);

        deliveriesDAO.getLastDeliveries(assignment).forEach(delivery -> {
            Delivery.Review review = delivery.getReview();

            delivery.getGroup().getMembers().forEach(user -> {
                sb.append(assignment.getName()).append(CSV_FIELD_SEPARATOR);
                sb.append(user.getNetId()).append(CSV_FIELD_SEPARATOR);
                sb.append(user.getStudentNumber()).append(CSV_FIELD_SEPARATOR);
                sb.append(user.getName()).append(CSV_FIELD_SEPARATOR);
                sb.append(delivery.getGroup().getGroupName()).append(CSV_FIELD_SEPARATOR);

                if(review != null) {
                    sb.append(review.getState()).append(CSV_FIELD_SEPARATOR);
                    sb.append(review.getGrade()).append(CSV_FIELD_SEPARATOR);
                }
                else {
                    sb.append(Delivery.State.SUBMITTED).append(CSV_FIELD_SEPARATOR);
                }

                sb.append(CSV_ROW_SEPARATOR);
            });
        });

		response.addHeader("Content-Disposition", " attachment; filename=\"assignment_" + assignmentId.toString()+ ".csv\"");
        return sb.toString();
    }

	/**
	 * Download the grades for this assignment
	 * @param courseCode the course to create an assignment for
	 * @param editionCode the course to create an assignment for
	 * @param assignmentId the assignment id
	 * @return a CSV file with the most recent deliveries
	 */
	@GET
	@Transactional
	@Produces(TEXT_CSV)
	@Path("{assignmentId : \\d+}/deliveries/download-rubrics")
	public String downloadRubrics(@PathParam("courseCode") String courseCode,
								  @PathParam("editionCode") String editionCode,
								  @PathParam("assignmentId") Long assignmentId) throws IOException {

		CourseEdition course = courses.find(courseCode, editionCode);
		Assignment assignment = assignmentsDAO.find(course, assignmentId);

		if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
			throw new UnauthorizedException();
		}
		StringBuilder sb = new StringBuilder();

		int numLevels = assignment.getTasks().stream()
			.map(Task::getCharacteristics).flatMap(Collection::stream)
			.map(Characteristic::getLevels).mapToInt(Collection::size)
			.max().orElse(0);

		List<Delivery> deliveries = Lists.newArrayList(deliveriesDAO.getLastDeliveries(assignment));
		Collections.sort(deliveries, Delivery.DELIVERIES_BY_GROUP_NUMBER);

		// Skip initial columns, list all groups
		IntStream.range(0, 2 + numLevels).forEach(i -> sb.append(CSV_FIELD_SEPARATOR));
		deliveries.forEach(delivery -> sb.append("Group ")
			.append(Long.toString(delivery.getGroup().getGroupNumber()))
			.append(CSV_FIELD_SEPARATOR));
		sb.append(CSV_ROW_SEPARATOR);

		assignment.getTasks().forEach(task -> {
			// Print exercise
			sb.append(task.getDescription()).append(CSV_FIELD_SEPARATOR).append(CSV_FIELD_SEPARATOR);
			IntStream.range(0, numLevels + deliveries.size()).forEach(i -> sb.append(CSV_FIELD_SEPARATOR));
			sb.append(CSV_ROW_SEPARATOR);

			task.getCharacteristics().forEach(characteristic -> {
				sb.append("-> ").append(characteristic.getDescription()).append(CSV_FIELD_SEPARATOR)
					.append(characteristic.getWeight()).append(CSV_FIELD_SEPARATOR);

				characteristic.getLevels().stream().sorted().forEach(mastery ->
					sb.append(mastery.getDescription()).append(CSV_FIELD_SEPARATOR));

				IntStream.range(0, numLevels - characteristic.getLevels().size())
					.forEach(a -> sb.append(CSV_FIELD_SEPARATOR));

				deliveries.forEach(delivery -> {
					if (delivery.getRubrics().containsKey(characteristic)) {
						sb.append(Double.toString(delivery.getRubrics().get(characteristic).getPoints()));
					}
					else {
						sb.append(CSV_FIELD_SEPARATOR);
					}
					sb.append(CSV_FIELD_SEPARATOR);
				});

				sb.append(CSV_ROW_SEPARATOR);
			});
		});

		response.addHeader("Content-Disposition", " attachment; filename=\"assignment_" + assignmentId.toString()+ ".csv\"");
		return sb.toString();
	}

    /**
     * An edit page page for an assignment
     * @param courseCode the course to create an assignment for
	 * @param editionCode the course to create an assignment for
     * @param assignmentId the assignment id
     */
    @GET
    @Transactional
    @Path("{assignmentId : \\d+}/edit")
    public Response getEditAssignmentPage(@PathParam("courseCode") String courseCode,
										  @PathParam("editionCode") String editionCode,
                                          @PathParam("assignmentId") long assignmentId,
                                          @QueryParam("error") String error) throws IOException {


        CourseEdition course = courses.find(courseCode, editionCode);
        Assignment assignment = assignmentsDAO.find(course, assignmentId);

        if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
            throw new UnauthorizedException();
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("user", currentUser);
        parameters.put("course", course);
        parameters.put("assignment", assignment);

        if(error != null)
            parameters.put("error", error);

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("courses/assignments/create-assignment.ftl", locales, parameters));
    }

    @POST
    @Path("{assignmentId : \\d+}/edit")
    public Response editAssignment(@PathParam("courseCode") String courseCode,
								   @PathParam("editionCode") String editionCode,
                                   @PathParam("assignmentId") long assignmentId,
                                   @FormParam("name") String name,
                                   @FormParam("summary") String summary,
                                   @FormParam("due-date") String dueDate) {

        CourseEdition course = courses.find(courseCode, editionCode);

        if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
            throw new UnauthorizedException();
        }

        Assignment assignment = assignmentsDAO.find(course, assignmentId);
        assignment.setName(name);
        assignment.setSummary(summary);


        if(!Strings.isNullOrEmpty(dueDate)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
            try {
                assignment.setDueDate(simpleDateFormat.parse(dueDate));
            }
            catch (ParseException e) {
                return redirect(course.getURI().resolve("assignments/create?error=error.invalid-date-format"));
            }
        }
        else {
            assignment.setDueDate(null);
        }

        try {
            assignmentsDAO.merge(assignment);
        }
        catch (ConstraintViolationException e) {
            Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
            if(violations.isEmpty()) {
                return redirect(course.getURI().resolve("assignments/" + assignmentId + "/edit?error=error.assignment-create-error"));
            }
            return redirect(course.getURI().resolve("assignments/" + assignmentId + "/edit?error=" + violations.iterator().next().getMessage()));
        }
        catch (Exception e) {
            return redirect(course.getURI().resolve("assignments/" + assignmentId + "/edit?error=error.assignment-create-error"));
        }

        return redirect(course.getURI());
    }

	/**
	 * Display the rubrics page for this {@link Assignment}.
	 * @param courseCode the course to create an assignment for.
	 * @param editionCode the course to create an assignment for.
	 * @param assignmentId the assignment id.
	 * @return The rubrics page.
	 * @throws IOException If an I/O error occurs.
	 */
	@GET
	@Transactional
	@Path("{assignmentId : \\d+}/rubrics")
	public Response getEditRubricsPage(@PathParam("courseCode") String courseCode,
									   @PathParam("editionCode") String editionCode,
									   @PathParam("assignmentId") long assignmentId) throws IOException {


		CourseEdition course = courses.find(courseCode, editionCode);
		Assignment assignment = assignmentsDAO.find(course, assignmentId);

		if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
			throw new UnauthorizedException();
		}

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("user", currentUser);
		parameters.put("course", course);
		parameters.put("assignment", assignment);

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("courses/assignments/assignment-rubrics.ftl", locales, parameters));
	}

	/**
	 * Retrieve the {@link Assignment} as JSON.
	 * @param courseCode the course to create an assignment for.
	 * @param editionCode the course to create an assignment for.
	 * @param assignmentId the assignment id.
	 * @return The {@link Assignment} instance.
	 */
	@GET
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{assignmentId : \\d+}/json")
	public Assignment getAssignmentAsJson(@PathParam("courseCode") String courseCode,
										  @PathParam("editionCode") String editionCode,
										  @PathParam("assignmentId") long assignmentId) {

		CourseEdition course = courses.find(courseCode, editionCode);

		if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
			throw new UnauthorizedException();
		}

		Assignment assignment = assignmentsDAO.find(course, assignmentId);
		// Trigger lazy initialization of tasks...
		assignment.getTasks().size();
		return assignment;
	}

	/**
	 * Update an {@link Assignment}.
	 * @param courseCode the course to create an assignment for.
	 * @param editionCode the course to create an assignment for.
	 * @param assignmentId the assignment id.
	 * @param assignment the updated assignment instance.
	 * @return The updated assignment instance.
	 */
	@PUT
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{assignmentId : \\d+}/json")
	public Assignment updateAssignment(@PathParam("courseCode") String courseCode,
									   @PathParam("editionCode") String editionCode,
									   @PathParam("assignmentId") long assignmentId,
									   @Valid Assignment assignment) {

		CourseEdition course = courses.find(courseCode, editionCode);

		if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
			throw new UnauthorizedException();
		}

		assignment.setCourseEdition(course);
		assignment.setAssignmentId(assignmentId);
		return assignmentsDAO.merge(assignment);
	}

	/**
	 * Get the last assignment deliveries as JSON.
	 * @param courseCode the course to create an assignment for.
	 * @param editionCode the course to create an assignment for.
	 * @param assignmentId the assignment id.
	 * @return a list of deliveries.
	 */
	@GET
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{assignmentId : \\d+}/last-deliveries/json")
	public List<Delivery> getLastDeliveries(@PathParam("courseCode") String courseCode,
											@PathParam("editionCode") String editionCode,
											@PathParam("assignmentId") long assignmentId) {
		CourseEdition course = courses.find(courseCode, editionCode);
		Assignment assignment = assignmentsDAO.find(course, assignmentId);

		if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
			throw new UnauthorizedException();
		}

		List<Delivery> deliveries = deliveriesDAO.getLastDeliveries(assignment);
		// Lazy load...
		deliveries.forEach(Delivery::getMasteries);
		return deliveries;
	}

}
