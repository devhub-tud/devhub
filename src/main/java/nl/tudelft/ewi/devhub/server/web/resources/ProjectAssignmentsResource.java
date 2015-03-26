package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.DeliveriesBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.*;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repository;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.GenericType;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by jgmeligmeyling on 05/03/15.
 */
@RequestScoped
@Path("courses/{courseCode}/groups/{groupNumber : \\d+}/assignments")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class ProjectAssignmentsResource extends Resource {

    private static final int PAGE_SIZE = 25;

    private final TemplateEngine templateEngine;
    private final User currentUser;
    private final BuildResults buildResults;
    private final Group group;
    private final Commits commits;
    private final CommitComments commitComments;
    private final PullRequests pullRequests;
    private final CommentBackend commentBackend;
    private final GitServerClient gitClient;
    private final Deliveries deliveries;
    private final DeliveriesBackend deliveriesBackend;
    private final Assignments assignments;

    @Inject
    public ProjectAssignmentsResource(final TemplateEngine templateEngine,
                                      final @Named("current.user") User currentUser,
                                      final @Named("current.group") Group group,
                                      final Commits commits,
                                      final CommentBackend commentBackend,
                                      final CommitComments commitComments,
                                      final BuildResults buildResults,
                                      final PullRequests pullRequests,
                                      final Deliveries deliveries,
                                      final GitServerClient gitClient,
                                      final DeliveriesBackend deliveriesBackend,
                                      final Assignments assignments) {

        this.templateEngine = templateEngine;
        this.group = group;
        this.currentUser = currentUser;
        this.commits = commits;
        this.commitComments = commitComments;
        this.commentBackend = commentBackend;
        this.buildResults = buildResults;
        this.pullRequests = pullRequests;
        this.deliveries = deliveries;
        this.gitClient = gitClient;
        this.deliveriesBackend = deliveriesBackend;
        this.assignments = assignments;
    }

    /**
     * Get assignment overview for project
     * @param request the current HttpServletRequest
     * @return rendered assignment overview
     * @throws java.io.IOException if an I/O error occurs
     */
    @GET
    @Transactional
    public Response getAssignmentsOverview(@Context HttpServletRequest request) throws IOException, ApiError, GitClientException {
        Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());
        Map<String, Object> parameters = Maps.newLinkedHashMap();
        parameters.put("user", currentUser);
        parameters.put("group", group);
        parameters.put("course", group.getCourse());
        parameters.put("repository", repository);
        parameters.put("deliveries", deliveries);

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("courses/assignments/group-assignments.ftl", locales, parameters));
    }

    /**
     * Get a specific assignment. Administrators and assistants see delivered assignments.
     * Students see whether or not an assignment has been delivered, and their grades.
     * @param request the current HttpServletRequest
     * @param assignmentId assignmentId for the assignment
     * @return rendered assignment overview
     * @throws IOException if an I/O error occurs
     */
    @GET
    @Transactional
    @Path("{assignmentId : \\d+}")
    public Response getAssignmentView(@Context HttpServletRequest request,
                                      @PathParam("assignmentId") Long assignmentId)
            throws IOException, ApiError, GitClientException {

        Assignment assignment = assignments.find(group.getCourse(), assignmentId);
        Repository repository = gitClient.repositories().retrieve(group.getRepositoryName());

        Map<String, Object> parameters = Maps.newLinkedHashMap();
        parameters.put("user", currentUser);
        parameters.put("group", group);
        parameters.put("course", group.getCourse());
        parameters.put("repository", repository);
        parameters.put("assignment", assignment);
        parameters.put("deliveries", deliveries);
        parameters.put("states", new ProjectResource.CommitChecker(group, buildResults));
        parameters.put("recentCommits", repository.retrieveBranch("master").retrieveCommits(0, 25).getCommits());

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("courses/assignments/group-assignment-view.ftl", locales, parameters));
    }

    /**
     * Submit an assignment for a course
     * @param request the current HttpServletRequest
     * @param assignmentId assignmentId for the assignment
     * @param formData submit data
     * @return a redirect request to the assignment page
     * @throws IOException if an I/O error occurs
     * @throws ApiError if an ApiError occurs
     */
    @POST
    @Path("{assignmentId : \\d+}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response postAssignment(@Context HttpServletRequest request,
                                   @PathParam("assignmentId") Long assignmentId,
                                   MultipartFormDataInput formData) throws IOException, ApiError {

        Map<String, List<InputPart>> formDataMap = formData.getFormDataMap();
        String commitId = extractString(formDataMap, "commit-id");
        String notes = extractString(formDataMap, "notes");

        Assignment assignment = assignments.find(group.getCourse(), assignmentId);
        Delivery delivery = new Delivery();
        delivery.setAssignment(assignment);
        delivery.setCommitId(commitId);
        delivery.setNotes(notes);
        delivery.setGroup(group);
        deliveriesBackend.deliver(delivery);

        List<InputPart> attachments = formDataMap.get("file-attachment");
        for(InputPart attachment : attachments) {
            String fileName = extractFilename(attachment);
            if(fileName.isEmpty()) continue;
            InputStream in = attachment.getBody(new GenericType<InputStream>() {});
            deliveriesBackend.attach(delivery, fileName, in);
        }

        return redirect(request.getRequestURI());
    }

    private static String extractString(Map<String, List<InputPart>> data, String key) throws IOException {
        List<InputPart> parts = data.get(key);
        if(parts != null && (!(parts.isEmpty()))) {
            return parts.get(0).getBodyAsString();
        }
        throw new IllegalArgumentException("No " + key + " in" + data.toString());
    }

    private static String extractFilename(final InputPart attachment) {
        Preconditions.checkNotNull(attachment);
        MultivaluedMap<String, String> headers = attachment.getHeaders();
        String contentDispositionHeader = headers.getFirst("Content-Disposition");
        Preconditions.checkNotNull(contentDispositionHeader);

        for(String headerPart : contentDispositionHeader.split(";(\\s)+")) {
            String[] split = headerPart.split("=");
            if(split.length == 2 && split[0].equalsIgnoreCase("filename")) {
                return split[1].replace("\"", "");
            }
        }

        return null;
    }

    /**
     * Get a file from a delivery
     * @param request the current HttpServletRequest
     * @param assignmentId assignmentId for the assignment
     * @param attachmentPath requested file
     * @return the requested file
     */
    @GET
    @Path("{assignmentId : \\d+}/attachment/{path}")
    public Response getAttachment(@Context HttpServletRequest request,
                                  @PathParam("assignmentId") Long assignmentId,
                                  @PathParam("path") String attachmentPath) {

        Assignment assignment = assignments.find(group.getCourse(), assignmentId);
        File file = deliveriesBackend.getAttachment(assignment, group, attachmentPath);
        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM).build();
    }

    /**
     * Review a specific delivery
     * @param request the current HttpServletRequest
     * @param deliveryId the delivery id
     * @return a view for reviewing the delivery
     */
    @GET
    @Transactional
    @Path("deliveries/{deliveryId}/review")
    public Response getReviewView(@Context HttpServletRequest request,
                                  @PathParam("deliveryId") Long deliveryId)
            throws ApiError, IOException, GitClientException {

        if(!(currentUser.isAdmin() || currentUser.isAssisting(group.getCourse()))) {
            throw new UnauthorizedException();
        }

        Delivery delivery = deliveries.find(deliveryId);

        Map<String, Object> parameters = Maps.newLinkedHashMap();
        parameters.put("user", currentUser);
        parameters.put("group", group);
        parameters.put("course", group.getCourse());
        parameters.put("delivery", delivery);
        parameters.put("assignment", delivery.getAssignment());
        parameters.put("deliveryStates", Delivery.State.values());
        parameters.put("states", new ProjectResource.CommitChecker(group, buildResults));
        parameters.put("repository", gitClient.repositories().retrieve(group.getRepositoryName()));

        List<Locale> locales = Collections.list(request.getLocales());
        return display(templateEngine.process("courses/assignments/group-delivery-review.ftl", locales, parameters));
    }

    /**
     * Submit a review for a delivery
     * @param request the current HttpServletRequest
     * @param deliveryId the delivery id
     * @return a redirect to the deliveries
     */
    @POST
    @Path("deliveries/{deliveryId}/review")
    public Response processReview(@Context HttpServletRequest request,
                                  @PathParam("deliveryId") Long deliveryId,
                                  @FormParam("grade") String grade,
                                  @FormParam("commentary") String commentary,
                                  @FormParam("state") Delivery.State state) throws UnauthorizedException, ApiError {
        if(!(currentUser.isAdmin() || currentUser.isAssisting(group.getCourse()))) {
            throw new UnauthorizedException();
        }

        Integer gradeInt = grade.isEmpty() ? null : Integer.valueOf(grade);
        Delivery delivery = deliveries.find(deliveryId);
        Delivery.Review review = new Delivery.Review();
        review.setState(state);
        review.setGrade(gradeInt);
        review.setCommentary(commentary);

        try {
            deliveriesBackend.review(delivery, review);
        }
        catch (Exception e){
            throw new ApiError("error.could-not-review", e);
        }

        return redirect(request.getRequestURI());
    }

}
