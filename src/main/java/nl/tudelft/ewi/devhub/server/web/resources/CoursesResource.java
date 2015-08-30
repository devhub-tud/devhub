package nl.tudelft.ewi.devhub.server.web.resources;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.CoursesBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.embeddables.TimeSpan;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.builds.MavenBuildInstructionEntity;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.client.GitClientException;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;

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
        parameters.put("courses", courseEditions);
        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("courses.ftl", locales, parameters));
    }

    @GET
    @Path("{courseCode}")
    public Response getCourse(@PathParam("courseCode") String courseCode) {
        Course course = courses.find(courseCode);
        CourseEdition courseEdition = courseEditions.getActiveCourseEdition(course);
        return redirect(courseEdition.getURI());
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
     * @param templateRepository template repository url for the course
     * @param minGroupSize min group size for the course
     * @param maxGroupSize max group size for the course
     * @param buildTimeout build timeout for the course
     * @return a Response containing the generated page
     */
    @POST
    @Path("setup")
    public Response setupCourse(@Context HttpServletRequest request,
                                @FormParam("code") String courseCode,
                                @FormParam("name") String courseName,
                                @FormParam("template") String templateRepository,
                                @FormParam("min") Integer minGroupSize,
                                @FormParam("max") Integer maxGroupSize,
                                @FormParam("timeout") Integer buildTimeout) throws GitClientException {

        if(!currentUser.isAdmin()) {
            throw new UnauthorizedException();
        }

        CourseEdition courseEdition = new CourseEdition();
		Course course = courses.ensureExists(courseCode, courseName);
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
		courseEdition.setCode(createEditionCode(timeSpan));

        try {
            coursesBackend.createCourse(courseEdition);
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

        return redirect("/courses");
    }

	private static String createEditionCode(TimeSpan timeSpan) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(timeSpan.getStart());
		Integer startYear = calendar.get(Calendar.YEAR) % 100;
		calendar.setTime(timeSpan.getEnd());
		Integer endYear  = calendar.get(Calendar.YEAR) % 100;
		return String.format("%02d%02d", startYear, endYear);
	}

}
