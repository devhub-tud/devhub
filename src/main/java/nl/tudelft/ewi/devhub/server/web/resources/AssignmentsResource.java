package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.backend.AssignmentStats;
import nl.tudelft.ewi.devhub.server.backend.DeliveriesBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.Assignments;
import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import org.jboss.resteasy.spi.NotImplementedYetException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by jgmeligmeyling on 04/03/15.
 * @author Jan-Willem Gmleig Meyling
 */
@Path("courses/{courseCode}/assignments")
public class AssignmentsResource extends Resource {

    public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm";

    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private Courses courses;

    @Inject
    private Assignments assignmentsDAO;

    @Inject
    private DeliveriesBackend deliveriesBackend;

    @Inject
    private Deliveries deliveriesDAO;

    @Inject
    @Named("current.user")
    private User currentUser;

    /**
     * Get an overview of the courses
     * @param request the current HttpServletRequest
     * @param courseCode the course to create an assignment for
     * @return a Response containing the generated page
     */
    @GET
    public Response getOverviewPage(@Context HttpServletRequest request,
                                    @PathParam("courseCode") String courseCode) {
        throw new NotImplementedYetException();
    }

    /**
     * Present the user a form to create a new assignment
     * @param request the current HttpServletRequest
     * @param courseCode the course to create an assignment for
     * @return a Response containing the generated page
     */
    @GET
    @Transactional
    @Path("create")
    public Response getCreatePage(@Context HttpServletRequest request,
                                  @PathParam("courseCode") String courseCode,
                                  @QueryParam("error") String error) throws IOException {

        Course course = courses.find(courseCode);
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
     * @param request the current HttpServletRequest
     * @param courseCode the course to create an assignment for
     * @param name name for the assignment
     * @param summary summary for the assignment
     * @param dueDate due date for the assignment
     * @return a Response containing the generated page
     */
    @POST
    @Path("create")
    public Response createPage(@Context HttpServletRequest request,
                               @PathParam("courseCode") String courseCode,
                               @FormParam("id") Long assignmentId,
                               @FormParam("name") String name,
                               @FormParam("summary") String summary,
                               @FormParam("due-date") String dueDate) {

        Course course = courses.find(courseCode);
        if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
            throw new UnauthorizedException();
        }

        if(assignmentsDAO.exists(course, assignmentId)) {
            return redirect("/courses/" + courseCode + "/assignments/create?error=error.assignment-number-exists");
        }

        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setAssignmentId(assignmentId);
        assignment.setName(name);
        assignment.setSummary(summary);

        if(!Strings.isNullOrEmpty(dueDate)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
            try {
                assignment.setDueDate(simpleDateFormat.parse(dueDate));
            }
            catch (ParseException e) {
                return redirect("/courses/" + courseCode + "/assignments/create?error=error.invalid-date-format");
            }
        }

        try {
            assignmentsDAO.persist(assignment);
        }
        catch (ConstraintViolationException e) {
            Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
            if(violations.isEmpty()) {
                return redirect("/courses/" + courseCode + "/assignments/create?error=error.assignment-create-error");
            }
            return redirect("/courses/" + courseCode + "/assignments/create?error=" + violations.iterator().next().getMessage());
        }
        catch (Exception e) {
            return redirect("/courses/" + courseCode + "/assignments/create?error=error.assignment-create-error");
        }

        return redirect("/courses/" + courseCode);
    }

    /**
     * An overview page for an assignment
     * @param request the current HttpServletRequest
     * @param courseCode the course to create an assignment for
     * @param assignmentId the assignment id
     * @return a Response containing the generated page
     */
    @GET
    @Transactional
    @Path("{assignmentId : \\d+}")
    public Response getAssignmentPage(@Context HttpServletRequest request,
                                      @PathParam("courseCode") String courseCode,
                                      @PathParam("assignmentId") Long assignmentId) throws IOException {

        Course course = courses.find(courseCode);
        if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
            throw new UnauthorizedException();
        }

        Assignment assignment = assignmentsDAO.find(course, assignmentId);
        List<Delivery> lastDeliveries = deliveriesDAO.getLastDeliveries(assignment);
        AssignmentStats assignmentStats = deliveriesBackend.getAssignmentStats(assignment);

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
    private final static char CSV_FIELD_SEPARATOR = ';';
    private final static char CSV_ROW_SEPARATOR = '\n';

    /**
     * Download the grades for this assignment
     * @param request the current HttpServletRequest
     * @param courseCode the course to create an assignment for
     * @param assignmentId the assignment id
     * @return a CSV file with the most recent deliveries
     */
    @GET
    @Transactional
    @Produces(TEXT_CSV)
    @Path("{assignmentId : \\d+}/deliveries/download")
    public String downloadAssignmentResults(@Context HttpServletRequest request,
                                            @PathParam("courseCode") String courseCode,
                                            @PathParam("assignmentId") Long assignmentId) throws IOException {

        Course course = courses.find(courseCode);
        Assignment assignment = assignmentsDAO.find(course, assignmentId);

        if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
            throw new UnauthorizedException();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Assignment;NetId;StudentNo;Name;Group;State;Grade;").append(CSV_ROW_SEPARATOR);

        deliveriesDAO.getLastDeliveries(assignment).forEach(delivery -> {
            User user = delivery.getCreatedUser();
            Delivery.Review review = delivery.getReview();

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
                sb.append("SUBMITTED;").append(CSV_FIELD_SEPARATOR);
            }

            sb.append(CSV_ROW_SEPARATOR);
        });

        return sb.toString();
    }

    /**
     * An edit page page for an assignment
     * @param request the current HttpServletRequest
     * @param courseCode the course to create an assignment for
     * @param assignmentId the assignment id
     */
    @GET
    @Transactional
    @Path("{assignmentId : \\d+}/edit")
    public Response getEditAssignmentPage(@Context HttpServletRequest request,
                                          @PathParam("courseCode") String courseCode,
                                          @PathParam("assignmentId") long assignmentId,
                                          @QueryParam("error") String error) throws IOException {


        Course course = courses.find(courseCode);
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
    public Response editAssignment(@Context HttpServletRequest request,
                                   @PathParam("courseCode") String courseCode,
                                   @PathParam("assignmentId") long assignmentId,
                                   @FormParam("name") String name,
                                   @FormParam("summary") String summary,
                                   @FormParam("due-date") String dueDate) {

        Course course = courses.find(courseCode);

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
                return redirect("/courses/" + courseCode + "/assignments/create?error=error.invalid-date-format");
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
                return redirect("/courses/" + courseCode + "/assignments/\" + assignmentId + \"/edit?error=error.assignment-create-error");
            }
            return redirect("/courses/" + courseCode + "/assignments/" + assignmentId + "/edit?error=" + violations.iterator().next().getMessage());
        }
        catch (Exception e) {
            return redirect("/courses/" + courseCode + "/assignments/" + assignmentId + "/edit?error=error.assignment-create-error");
        }

        return redirect("/courses/" + courseCode);
    }

}
