package nl.tudelft.ewi.devhub.server.web.resources;

import nl.tudelft.ewi.devhub.server.backend.ProjectsBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;

import org.eclipse.jetty.util.UrlEncoded;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by jgmeligmeyling on 03/03/15.
 * @author Jan-Willem Gmelig Meyling
 */
@Path("courses/{courseCode}/{editionCode}/enroll")
@RequestScoped
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class CourseEnrollResource extends Resource {

    private final static int MIN_GROUP_SIZE = 1;

    @Inject
    private ProjectsBackend projectsBackend;

    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private CourseEditions courses;

    @Inject
    @Named("current.user")
    private User currentUser;

    @Inject
    private Users users;

    @GET
    @Transactional
    public Response showProjectSetupPage(@Context HttpServletRequest request,
                                         @PathParam("courseCode") String courseCode,
                                         @PathParam("editionCode") String editionCode,
                                         @QueryParam("error") String error,
                                         @QueryParam("step") Integer step) throws IOException {

        CourseEdition courseEdition = courses.find(courseCode, editionCode);

        if (step != null) {
            if (step == 1) {
                return showProjectSetupPageStep1(request, courseCode, editionCode, error);
            }
            else if (step == 2) {
                return showProjectSetupPageStep2(request, courseCode, editionCode, error);
            }
        }
        return redirect(courseEdition.getURI().resolve("enroll?step=1"));
    }

    private Response showProjectSetupPageStep1(@Context HttpServletRequest request,
                                               @PathParam("courseCode") String courseCode,
                                               @PathParam("editionCode") String editionCode,
                                               @QueryParam("error") String error) throws IOException {

        HttpSession session = request.getSession();
        CourseEdition course = courses.find(courseCode, editionCode);

        String previousCourseCode = String.valueOf(session.getAttribute("projects.setup.course"));
        session.setAttribute("projects.setup.course", courseCode);
        if (!courseCode.equals(previousCourseCode)) {
            session.removeAttribute("projects.setup.members");
        }

        Collection<User> members = (Collection<User>) session.getAttribute("projects.setup.members");

        int maxGroupSize = getMaxGroupSize(course);
        int minGroupSize = getMinGroupSize(course);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("user", currentUser);
        parameters.put("course", course);
        if (members != null && !members.isEmpty()) {
            parameters.put("members", members);
        }
        parameters.put("maxGroupSize", maxGroupSize);
        parameters.put("minGroupSize", minGroupSize);
        if (!Strings.isNullOrEmpty(error)) {
            parameters.put("error", error);
        }

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("project-setup-2.ftl", locales, parameters));
    }

    @SuppressWarnings("unchecked")
    private Response showProjectSetupPageStep2(@Context HttpServletRequest request,
                                               @PathParam("courseCode") String courseCode,
											   @PathParam("editionCode") String editionCode,
                                               @QueryParam("error") String error) throws IOException {

        HttpSession session = request.getSession();
        CourseEdition course = courses.find(courseCode, editionCode);
        Collection<User> members = (Collection<User>) session.getAttribute("projects.setup.members");

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("user", currentUser);
        parameters.put("course", course);
        parameters.put("members", members);

        if (!Strings.isNullOrEmpty(error)) {
            parameters.put("error", error);
        }

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("project-setup-3.ftl", locales, parameters));
    }

    @POST
    @SuppressWarnings("unchecked")
    public Response processProjectSetup(@Context HttpServletRequest request,
                                        @PathParam("courseCode") String courseCode,
										@PathParam("editionCode") String editionCode,
                                        @QueryParam("step") int step)
            throws IOException {

        HttpSession session = request.getSession();
        CourseEdition course = courses.find(courseCode, editionCode);

        if (step == 1) {
            Collection<User> groupMembers = getGroupMembers(request);
            int maxGroupSize = getMaxGroupSize(course);
            int minGroupSize = getMinGroupSize(course);

            if (!groupMembers.contains(currentUser) && !currentUser.isAdmin() && !currentUser.isAssisting(course)) {
                return redirect(course.getURI().resolve("enroll?step=1&error=error.must-be-group-member"));
            }
            if (groupMembers.size() < minGroupSize || groupMembers.size() > maxGroupSize) {
                return redirect(course.getURI().resolve("enroll?step=1&error=error.invalid-group-size"));
            }

            for (User user : groupMembers) {
                if (user.isParticipatingInCourse(course)) {
                    return redirect(course.getURI().resolve("enroll?step=1&error=error.already-registered-for-course"));
                }
            }

            session.setAttribute("projects.setup.members", groupMembers);
            return redirect(course.getURI().resolve("enroll?step=2"));
        }

        try {
            Collection<User> members = (Collection<User>) session.getAttribute("projects.setup.members");
            projectsBackend.setupProject(course, members);

            session.removeAttribute("projects.setup.course");
            session.removeAttribute("projects.setup.members");
            return redirect(course.getURI());
        }
        catch (ApiError e) {
            String error = UrlEncoded.encodeString(e.getResourceKey());
            return redirect(course.getURI().resolve("enroll?step=2&error=" + error));
        }
    }

    private Collection<User> getGroupMembers(HttpServletRequest request) {
        String netId;
        int memberId = 1;
        Set<String> netIds = Sets.newHashSet();
        while (!Strings.isNullOrEmpty((netId = request.getParameter("member-" + memberId)))) {
            memberId++;
            netIds.add(netId);
        }

        Map<String, User> members = users.mapByNetIds(netIds);
        return members.values();
    }

    public int getMinGroupSize(CourseEdition course) {
        if (currentUser.isAdmin() || currentUser.isAssisting(course)) {
            return MIN_GROUP_SIZE;
        }
        return course.getMinGroupSize();
    }

    public int getMaxGroupSize(CourseEdition course) {
        return course.getMaxGroupSize();
    }

}
