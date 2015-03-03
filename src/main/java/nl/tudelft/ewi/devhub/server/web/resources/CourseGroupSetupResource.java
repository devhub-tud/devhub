package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.devhub.server.backend.ProjectsBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import org.eclipse.jetty.util.UrlEncoded;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

/**
 * Created by jgmeligmeyling on 03/03/15.
 * @author Jan-Willem Gmelig Meyling
 */
@Path("courses/{courseCode}/create")
@RequestScoped
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class CourseGroupSetupResource extends Resource {

    private final static int MIN_GROUP_SIZE = 1;

    @Inject
    private ProjectsBackend projectsBackend;

    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private Courses courses;

    @Inject
    @Named("current.user")
    private User currentUser;

    @Inject
    private Users users;

    @GET
    @Transactional
    public Response showProjectSetupPage(@Context HttpServletRequest request,
                                         @PathParam("courseCode") String courseCode,
                                         @QueryParam("error") String error,
                                         @QueryParam("step") Integer step) throws IOException {

        if (step != null) {
            if (step == 1) {
                return showProjectSetupPageStep1(request, courseCode, error);
            }
            else if (step == 2) {
                return showProjectSetupPageStep2(request, courseCode, error);
            }
        }
        return redirect("/courses/" + courseCode + "/create?step=1");
    }

    private Response showProjectSetupPageStep1(@Context HttpServletRequest request,
                                               @PathParam("courseCode") String courseCode,
                                               @QueryParam("error") String error) throws IOException {

        HttpSession session = request.getSession();
        Course course = courses.find(courseCode);
        List<User> members = (List<User>) session.getAttribute("projects.setup.members");

        String previousCourseCode = String.valueOf(session.getAttribute("projects.setup.course"));
        session.setAttribute("projects.setup.course", courseCode);
        if (!courseCode.equals(previousCourseCode)) {
            session.removeAttribute("projects.setup.members");
        }

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
                                               @QueryParam("error") String error) throws IOException {

        HttpSession session = request.getSession();
        Course course = courses.find(courseCode);
        List<User> members = (List<User>) session.getAttribute("projects.setup.members");

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
                                        @QueryParam("step") int step)
            throws IOException {

        HttpSession session = request.getSession();
        Course course = courses.find(courseCode);

        if (step == 1) {
            List<User> groupMembers = getGroupMembers(request);
            int maxGroupSize = getMaxGroupSize(course);
            int minGroupSize = getMinGroupSize(course);

            if (!groupMembers.contains(currentUser) && !currentUser.isAdmin() && !currentUser.isAssisting(course)) {
                return redirect("/courses/" + courseCode + "/create?step=2&error=error.must-be-group-member");
            }
            if (groupMembers.size() < minGroupSize || groupMembers.size() > maxGroupSize) {
                return redirect("/courses/" + courseCode + "/create?step=2&error=error.invalid-group-size");
            }

            for (User user : groupMembers) {
                if (user.isParticipatingInCourse(course)) {
                    return redirect("/courses/" + courseCode + "/create?step=2&error=error.already-registered-for-course");
                }
            }

            session.setAttribute("projects.setup.members", groupMembers);
            return redirect("/courses/" + courseCode + "/create?step=2");
        }

        try {
            List<User> members = (List<User>) session.getAttribute("projects.setup.members");
            projectsBackend.setupProject(course, members);

            session.removeAttribute("projects.setup.course");
            session.removeAttribute("projects.setup.members");
            return redirect("/courses/" + courseCode);
        }
        catch (ApiError e) {
            String error = UrlEncoded.encodeString(e.getResourceKey());
            return redirect("/courses/" + courseCode + "/create?step=2&error=" + error);
        }
    }

    private List<User> getGroupMembers(HttpServletRequest request) {
        String netId;
        int memberId = 1;
        Set<String> netIds = Sets.newHashSet();
        while (!Strings.isNullOrEmpty((netId = request.getParameter("member-" + memberId)))) {
            memberId++;
            netIds.add(netId);
        }

        Map<String, User> members = users.mapByNetIds(netIds);
        List<User> sortedMembers = Lists.newArrayList();
        for (String memberNetId : netIds) {
            sortedMembers.add(members.get(memberNetId));
        }
        return sortedMembers;
    }

    public int getMinGroupSize(Course course) {
        if (currentUser.isAdmin() || currentUser.isAssisting(course)) {
            return MIN_GROUP_SIZE;
        }
        return course.getMinGroupSize();
    }

    public int getMaxGroupSize(Course course) {
        return course.getMaxGroupSize();
    }

}
