package nl.tudelft.ewi.devhub.server.web.resources;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.DeliveriesBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.ReviewMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.Assignments;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.TagModel;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;

import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.GenericType;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@RequestScoped
@Path("courses/{courseCode}/{editionCode}/groups/{groupNumber : \\d+}/assignments")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class ProjectAssignmentsResource extends Resource {

    private final TemplateEngine templateEngine;
    private final User currentUser;
    private final BuildResults buildResults;
    private final Group group;
    private final Commits commits;
    private final RepositoryEntity repositoryEntity;
    private final RepositoriesApi repositoriesApi;
    private final Deliveries deliveries;
    private final DeliveriesBackend deliveriesBackend;
    private final Assignments assignments;
    private final ReviewMailer reviewMailer;

    @Inject
    public ProjectAssignmentsResource(final TemplateEngine templateEngine,
                                      final @Named("current.user") User currentUser,
                                      final @Named("current.group") Group group,
                                      final BuildResults buildResults,
                                      final Deliveries deliveries,
                                      final RepositoriesApi repositoriesApi,
                                      final DeliveriesBackend deliveriesBackend,
                                      final Assignments assignments,
                                      final Commits commits,
                                      final ReviewMailer reviewMailer) {

        this.templateEngine = templateEngine;
        this.group = group;
        this.repositoryEntity = group.getRepository();
        this.currentUser = currentUser;
        this.buildResults = buildResults;
        this.deliveries = deliveries;
        this.repositoriesApi = repositoriesApi;
        this.deliveriesBackend = deliveriesBackend;
        this.assignments = assignments;
        this.reviewMailer = reviewMailer;
        this.commits = commits;
    }

	protected Map<String, Object> getBaseParameters() {
		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		parameters.put("group", group);
		parameters.put("course", group.getCourseEdition());
		parameters.put("repositoryEntity", repositoryEntity);
		return parameters;
	}


	/**
     * Get assignment overview for project
     * @param request the current HttpServletRequest
     * @return rendered assignment overview
     * @throws java.io.IOException if an I/O error occurs
     */
    @GET
    @Transactional
    public Response getAssignmentsOverview(@Context HttpServletRequest request) throws IOException, ApiError {
        RepositoryApi repositoryApi = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());
        RepositoryModel repositoryModel = repositoryApi.getRepositoryModel();

        Map<String, Object> parameters = getBaseParameters();
        parameters.put("repository", repositoryModel);
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
                                      @PathParam("assignmentId") long assignmentId)
            throws IOException, ApiError {

        Assignment assignment = assignments.find(group.getCourse(), assignmentId);
        RepositoryApi repositoryApi = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());
        RepositoryModel repositoryModel = repositoryApi.getRepositoryModel();

        List<Delivery> myDeliveries = deliveries.getDeliveries(assignment, group);
        Map<String, Object> parameters = getBaseParameters();
        parameters.put("repository", repositoryModel);
        parameters.put("assignment", assignment);
        parameters.put("myDeliveries", myDeliveries);
        parameters.put("canSubmit", !deliveries.lastDeliveryIsApprovedOrDisapproved(assignment, group));
        parameters.put("recentCommits", repositoryApi.getBranch("master").retrieveCommitsInBranch(0, 25).getCommits());

        Collection<String> commitIds = myDeliveries.stream()
                .map(Delivery::getCommit)
				.filter(Objects::nonNull)
				.map(Commit::getCommitId)
                .collect(Collectors.toSet());

	        parameters.put("builds", buildResults.findBuildResults(repositoryEntity, commitIds));

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
                                   @PathParam("assignmentId") long assignmentId,
                                   MultipartFormDataInput formData) throws IOException, ApiError {

        Map<String, List<InputPart>> formDataMap = formData.getFormDataMap();
        String commitId = extractString(formDataMap, "commit-id");
        String notes = extractString(formDataMap, "notes");

        Assignment assignment = assignments.find(group.getCourse(), assignmentId);
        Delivery delivery = new Delivery();
        delivery.setAssignment(assignment);

        delivery.setCommit(commits.ensureExists(repositoryEntity, commitId));
        delivery.setNotes(notes);
        delivery.setGroup(group);
        deliveriesBackend.deliver(delivery);

        addAttachments(formDataMap, delivery);

        if(!Strings.isNullOrEmpty(commitId)) {
            tagAssignmentDelivery(commitId, assignment, delivery);
        }

        return redirect(request.getRequestURI());
    }

    private void addAttachments(Map<String, List<InputPart>> formDataMap, Delivery delivery) throws IOException, ApiError {
        List<InputPart> attachments = formDataMap.get("file-attachment");
        for(InputPart attachment : attachments) {
            String fileName = extractFilename(attachment);
            if(fileName.isEmpty()) continue;
            InputStream in = attachment.getBody(new GenericType<InputStream>() {});
            deliveriesBackend.attach(delivery, fileName, in);
        }
    }

    private void tagAssignmentDelivery(String commitId, Assignment assignment, Delivery delivery) {
        try {
            RepositoryApi repositoryApi = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());
            CommitModel commitModel = new CommitModel();
            commitModel.setCommit(commitId);

            TagModel tagModel = new TagModel();
            int assignmentNumber = assignmentNumber(assignment);
            int deliveryNumber = deliveryNumber(assignment);
            tagModel.setName(String.format("Assignment-%d.%d", assignmentNumber, deliveryNumber));
            tagModel.setCommit(commitModel);
            repositoryApi.addTag(tagModel);
        }
        catch (ClientErrorException e) {
            log.warn("Failed to tag delivery {}", e, delivery);
        }
    }

    private int deliveryNumber(Assignment assignment) {
        return deliveries.getDeliveries(assignment, group).size();
    }

    private int assignmentNumber(Assignment assignment) {
        return group.getCourse().getAssignments().indexOf(assignment) + 1;
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
            throws ApiError, IOException {

        if(!(currentUser.isAdmin() || currentUser.isAssisting(group.getCourse()))) {
            throw new UnauthorizedException();
        }

        RepositoryApi repositoryApi = repositoriesApi.getRepository(repositoryEntity.getRepositoryName());
        Delivery delivery = deliveries.find(group, deliveryId);

        Map<String, Object> parameters = getBaseParameters();
        parameters.put("delivery", delivery);
        parameters.put("assignment", delivery.getAssignment());
        parameters.put("deliveryStates", Delivery.State.values());
        parameters.put("repository", repositoryApi.getRepositoryModel());

        List<String> commitIds = delivery.getCommit() != null ?
			Lists.newArrayList(delivery.getCommit().getCommitId()) : Collections.emptyList();
        parameters.put("builds", buildResults.findBuildResults(repositoryEntity, commitIds));

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

        Double gradeValue = grade.isEmpty() ? null : Double.valueOf(grade);
        Delivery delivery = deliveries.find(group, deliveryId);
        Delivery.Review review = new Delivery.Review();
        review.setState(state);
        review.setGrade(gradeValue);
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
