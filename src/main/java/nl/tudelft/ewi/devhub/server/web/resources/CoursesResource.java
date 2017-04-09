package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.CoursesBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.embeddables.TimeSpan;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.builds.MavenBuildInstructionEntity;
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
import javax.validation.constraints.Min;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * The CoursesResource is used to view, create and edit courses.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@Path("courses")
@RequestScoped
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class CoursesResource extends Resource {

	@Inject
	private Courses courses;

    @Inject
    private CourseEditions courseEditions;

    @Inject @Named("current.user")
    private User currentUser;

    @Inject
    private CoursesBackend coursesBackend;

    @Inject
    private TemplateEngine templateEngine;

    @Context
    private HttpServletRequest request;

    /**
     * Get an course overview. This page lists the participating courses, assisting courses
     * and administrating courses.
     * @param request the current HttpServletRequest
     * @return a Response containing the generated page
     * @throws IOException if an I/O error occurs
     */
    @GET
    public Response getCourses(@Context HttpServletRequest request) throws IOException {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("user", currentUser);
        parameters.put("courses", courses);
        parameters.put("courseEditions", courseEditions);
        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("courses.ftl", locales, parameters));
    }

    @GET
    @Path("{courseCode: (?!edit|setup)[^/]+?}")
    public Response getCourse(@PathParam("courseCode") String courseCode) throws IOException {
        Course course = courses.find(courseCode);

        if(!(currentUser.isAdmin() || course.getEditions().stream().anyMatch(currentUser::isAssisting))) {
            CourseEdition courseEdition = courseEditions.getActiveCourseEdition(course);
            return redirect(courseEdition.getURI());
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("user", currentUser);
        parameters.put("course", course);

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("course-view.ftl", locales, parameters));
    }

    /**
     * Edit a course
     * @param courseCode the course code for the course
     * @param error an error message
     * @return a Response containing the generated page
     * @throws IOException if an I/O error occurs
     */
    @GET
    @Path("{courseCode:(?!edit|setup)[^/]+?}/edit")
    public Response getEditPage(@PathParam("courseCode") String courseCode,
                                @QueryParam("error") String error) throws IOException {

        if(!currentUser.isAdmin()) {
            throw new UnauthorizedException();
        }

        Course course = courses.find(courseCode);
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("user", currentUser);
        parameters.put("course", course);

        if(error != null)
            parameters.put("error", error);

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("course-setup.ftl", locales, parameters));
    }

    /**
     * Submit changes to a course
     * @param courseCode the course code for the course
     * @param courseName name for the course
     * @param templateRepository template repository url for the course
     * @param minGroupSize min group size for the course
     * @param maxGroupSize max group size for the course
     * @param buildTimeout build timeout for the course
     * @return a Response containing the generated page
     */
    @POST
    @Path("{courseCode: (?!edit|setup)[^/]+?}/edit")
    public Response editCourse(@PathParam("courseCode") String courseCode,
                               @FormParam("code") String newCode,
                               @FormParam("name") String courseName,
                               @FormParam("template") String templateRepository,
                               @FormParam("min") Integer minGroupSize,
                               @FormParam("max") Integer maxGroupSize,
                               @FormParam("timeout") Integer buildTimeout) {

        if(!currentUser.isAdmin()) {
            throw new UnauthorizedException();
        }

        Course course = courses.find(courseCode);

        if (! isNullOrEmpty(newCode)) {
            course.setCode(newCode);
        }

        course.setName(courseName);

        try {
            courses.merge(course);
        }
        catch (ConstraintViolationException e) {
            Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
            if(violations.isEmpty()) {
                return redirect(course.getURI().resolve("edit?error=error.course-create-error"));
            }
            return redirect(course.getURI().resolve("edit?error=" + violations.iterator().next().getMessage()));
        }
        catch (Exception e) {
            return redirect(course.getURI().resolve("edit?error=error.course-create-error"));
        }

        return redirect(course.getURI());
    }

    /**
     * Set up a new course
     * @param request the current HttpServletRequest
     * @param error an error message
     * @return a Response containing the generated page
     * @throws IOException if an I/O error occurs
     */
    @GET
    @Path("setup")
    public Response getSetupPage(@Context HttpServletRequest request,
                                 @QueryParam("error") String error) throws IOException {

        if(!currentUser.isAdmin()) {
            throw new UnauthorizedException();
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("user", currentUser);
        parameters.put("courses", courseEditions);

        if(error != null)
            parameters.put("error", error);

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("course-setup.ftl", locales, parameters));
    }

    /**
     * Submit a course creation
     * @param request the current HttpServletRequest
     * @param courseCode course code for the course
     * @param courseName course name for the course
     * @return a Response containing the generated page
     */
    @POST
    @Path("setup")
    public Response setupCourse(@Context HttpServletRequest request,
                                @FormParam("code") String courseCode,
                                @FormParam("name") String courseName) {

        if(!currentUser.isAdmin()) {
            throw new UnauthorizedException();
        }

        Course course = new Course();
        course.setCode(courseCode);
        course.setName(courseName);

        try {
            coursesBackend.createCourse(course);
        }
        catch (ConstraintViolationException e) {
            Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
            if(violations.isEmpty()) {
                return redirect("/courses/setup?error=error.courseEdition-create-error");
            }
            return redirect("/courses/setup?error=" + violations.iterator().next().getMessage());
        }
        catch (Exception e) {
            return redirect("/courses/setup?error=error.courseEdition-create-error");
        }

        return redirect(Course.COURSE_BASE_PATH);
    }

    /**
     * Set up a new course
     * @param request the current HttpServletRequest
     * @param error an error message
     * @return a Response containing the generated page
     * @throws IOException if an I/O error occurs
     */
    @GET
    @Path("{courseCode: (?!edit|setup)[^/]+?}/setup")
    public Response getCourseEditionSetupPage(@Context HttpServletRequest request,
                                              @PathParam("courseCode") String courseCode,
                                              @QueryParam("error") String error) throws IOException {

        if(!currentUser.isAdmin()) {
            throw new UnauthorizedException();
        }

        Course course = courses.find(courseCode);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("user", currentUser);
        parameters.put("courses", courseEditions);
        parameters.put("course", course);

        if(error != null)
            parameters.put("error", error);

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("course-edition-setup.ftl", locales, parameters));
    }

    /**
     * Submit a course creation
     * @param request the current HttpServletRequest
     * @param courseCode course code for the course
     * @param courseEditionCode course code for the course edition
     * @param templateRepository template repository url for the course
     * @param minGroupSize min group size for the course
     * @param maxGroupSize max group size for the course
     * @param buildTimeout build timeout for the course
     * @return a Response containing the generated page
     */
    @POST
    @Path("{courseCode: (?!edit|setup)[^/]+?}/setup")
    public Response setupCourseEdition(@Context HttpServletRequest request,
                                       @PathParam("courseCode") String courseCode,
                                       @FormParam("code") String courseEditionCode,
                                       @FormParam("template") String templateRepository,
                                       @FormParam("min") @Min(1) Integer minGroupSize,
                                       @FormParam("max") @Min(1) Integer maxGroupSize,
                                       @FormParam("timeout") Integer buildTimeout) {

        if(!currentUser.isAdmin()) {
            throw new UnauthorizedException();
        }

        Course course = courses.find(courseCode);
        CourseEdition courseEdition = new CourseEdition();
        courseEdition.setCourse(course);

        courseEdition.setTemplateRepositoryUrl(templateRepository);
        courseEdition.setMinGroupSize(minGroupSize);
        courseEdition.setMaxGroupSize(maxGroupSize);

        MavenBuildInstructionEntity buildInstructionEntity = MavenBuildInstructionEntity.mavenTestInstruction(buildTimeout);
        courseEdition.setBuildInstruction(buildInstructionEntity);

        Date today = new Date();
        Date inAYear = new Date(today.getTime() + TimeUnit.DAYS.toMillis(365));
        TimeSpan timeSpan = new TimeSpan(today, inAYear);
        courseEdition.setTimeSpan(timeSpan);
        courseEdition.setCode(courseEditionCode);

        try {
            coursesBackend.createCourse(courseEdition);
        }
        catch (ConstraintViolationException e) {
            Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
            if(violations.isEmpty()) {
                return redirect(Course.COURSE_BASE_PATH + courseCode + "/setup?error=error.courseEdition-create-error");
            }
            return redirect(Course.COURSE_BASE_PATH + courseCode + "/setup?error=" + violations.iterator().next().getMessage());
        }
        catch (Exception e) {
            return redirect(Course.COURSE_BASE_PATH + courseCode + "/setup?error=error.courseEdition-create-error");
        }

        return redirect(Course.COURSE_BASE_PATH + courseCode);
    }

}
