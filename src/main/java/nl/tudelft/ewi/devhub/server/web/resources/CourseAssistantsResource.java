package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.devhub.server.backend.CoursesBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.client.GitClientException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jgmeligmeyling on 03/03/15.
 * @author Jan-Willem Gmelig Meyling
 */
@Path("courses/{courseCode}/assistants")
@RequestScoped
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class CourseAssistantsResource extends Resource {

    private final TemplateEngine templateEngine;
    private final Courses courses;
    private final User currentUser;
    private final Users users;
    private final CoursesBackend coursesBackend;

    @Inject
    public CourseAssistantsResource(TemplateEngine templateEngine,
                                    Courses courses,
                                    Users users,
                                    @Named("current.user") User currentUser,
                                    final CoursesBackend coursesBackend) {
        this.templateEngine = templateEngine;
        this.courses = courses;
        this.currentUser = currentUser;
        this.users = users;
        this.coursesBackend = coursesBackend;
    }

    @GET
    @Transactional
    public Response showProjectSetupPage(@Context HttpServletRequest request,
                                         @PathParam("courseCode") String courseCode,
                                         @QueryParam("error") String error,
                                         @QueryParam("step") Integer step) throws IOException {

        if(!currentUser.isAdmin()) {
            throw new UnauthorizedException();
        }

        if (step != null) {
            if (step == 1) {
                return showCourseAssistantsPageStep1(request, courseCode, error);
            }
            else if (step == 2) {
                return showCourseAssistantsPageStep2(request, courseCode, error);
            }
        }
        return redirect("/courses/" + courseCode + "/assistants?step=1");
    }

    private Response showCourseAssistantsPageStep1(@Context HttpServletRequest request,
                                                   @PathParam("courseCode") String courseCode,
                                                   @QueryParam("error") String error) throws IOException {


        HttpSession session = request.getSession();
        Course course = courses.find(courseCode);

        String previousCourseCode = String.valueOf(session.getAttribute("courses.setup.course"));
        session.setAttribute("courses.setup.course", courseCode);
        if (!courseCode.equals(previousCourseCode)) {
            session.removeAttribute("courses.course.assistants");
        }

        List<User> members = (List<User>) session.getAttribute("courses.course.assistants");
        if(members == null)
            members = course.getAssistants();

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("user", currentUser);
        parameters.put("course", course);
        if (members != null && !members.isEmpty()) {
            parameters.put("members", members);
        }
        if (!Strings.isNullOrEmpty(error)) {
            parameters.put("error", error);
        }

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("course-assistants-edit.ftl", locales, parameters));
    }

    @SuppressWarnings("unchecked")
    private Response showCourseAssistantsPageStep2(@Context HttpServletRequest request,
                                                   @PathParam("courseCode") String courseCode,
                                                   @QueryParam("error") String error) throws IOException {

        HttpSession session = request.getSession();
        Course course = courses.find(courseCode);
        Collection<User> members = (Collection<User>) session.getAttribute("courses.course.assistants");

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("user", currentUser);
        parameters.put("course", course);
        parameters.put("members", members);
        if (!Strings.isNullOrEmpty(error)) {
            parameters.put("error", error);
        }

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("course-assistants-confirm.ftl", locales, parameters));
    }

    @POST
    @SuppressWarnings("unchecked")
    public Response processProjectSetup(@Context HttpServletRequest request,
                                        @PathParam("courseCode") String courseCode,
                                        @QueryParam("step") int step)
            throws IOException, GitClientException {

        if(!currentUser.isAdmin()) {
            throw new UnauthorizedException();
        }

        HttpSession session = request.getSession();
        Course course = courses.find(courseCode);

        if (step == 1) {
            Collection<User> courseAssistants = getCourseAssistants(request);
            session.setAttribute("courses.course.assistants", courseAssistants);
            return redirect("/courses/" + courseCode + "/assistants?step=2");
        }

        Collection<User> courseAssistants = (Collection<User>) session.getAttribute("courses.course.assistants");
        coursesBackend.setAssistants(course, courseAssistants);

        session.removeAttribute("courses.course.assistants");
        return redirect("/courses/" + courseCode);
    }

    private Collection<User> getCourseAssistants(HttpServletRequest request) {
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

}
