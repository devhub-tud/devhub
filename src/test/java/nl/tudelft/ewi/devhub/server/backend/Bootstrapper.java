package nl.tudelft.ewi.devhub.server.backend;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.*;
import nl.tudelft.ewi.devhub.server.database.entities.*;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery.Review;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.models.UserModel;

@Slf4j
public class Bootstrapper {

	@Data
	static class BState {
		private List<BUser> users;
		private List<BCourse> courses;
	}
	
	@Data
	static class BUser {
		private String name;
		private String email;
		private String netId;
		private String password;
		private boolean admin;
	}
	
	@Data
	static class BCourse {
		private String code;
		private String name;
		private String templateRepositoryUrl;
		private boolean started;
		private boolean ended;
		private int minGroupSize;
		private int maxGroupSize;
		private Integer buildTimeout;
		private List<String> assistants;
		private List<BGroup> groups;
        private List<BAssignment> assignments;
	}

    @Data
    static class BAssignment {
        private Long id;
        private String name;
    }
	
	@Data
	static class BGroup {
		private Long groupNumber;
		private Integer buildTimeout;
		private String templateRepositoryUrl;
		private List<String> members;
		private List<BDelivery> deliveries;
	}
	
	@Data
	static class BDelivery {
		private long assignmentId;
		private String createdUserName;
		private BReview review;
	}
	
	@Data
	static class BReview {
		private String state;
		private double grade;
		private String reviewedUserName;
	}
	
	private final Users users;
	private final Courses courses;
	private final CourseAssistants assistants;
	private final Groups groups;
	private final GroupMemberships memberships;
	private final MockedAuthenticationBackend authBackend;
	private final ObjectMapper mapper;
	private final GitServerClient gitClient;
	private final ProjectsBackend projects;
	private final Deliveries deliveries;
    private final Assignments assignments;

	@Inject
	Bootstrapper(Users users, Courses courses, CourseAssistants assistants, Groups groups, 
			GroupMemberships memberships, MockedAuthenticationBackend authBackend, ObjectMapper mapper,
			GitServerClient gitClient, ProjectsBackend projects, Assignments assignments,
			Deliveries deliveries) {
		
		this.users = users;
		this.courses = courses;
		this.assistants = assistants;
		this.groups = groups;
		this.memberships = memberships;
		this.authBackend = authBackend;
		this.mapper = mapper;
		this.gitClient = gitClient;
		this.projects = projects;
        this.assignments = assignments;
        this.deliveries = deliveries;
	}
	
	@Transactional
	public void prepare(String path) throws IOException, ApiError, GitClientException {
		InputStream inputStream = Bootstrapper.class.getResourceAsStream(path);
		BState state = mapper.readValue(inputStream, BState.class);
		
		Map<String, User> userMapping = Maps.newHashMap();
		for (BUser user : state.getUsers()) {
			User entity = new User();
			entity.setName(user.getName());
			entity.setEmail(user.getEmail());
			entity.setNetId(user.getNetId());
			entity.setAdmin(user.isAdmin());
			users.persist(entity);
			
			authBackend.addUser(user.getNetId(), user.getPassword(), user.isAdmin());
			userMapping.put(entity.getNetId(), entity);
			log.debug("Persisted user: " + entity.getNetId());
		}
		
		for (BCourse course : state.getCourses()) {
			CourseEdition entity;

			try {
				entity = courses.find(course.getCode());
				log.debug("CourseEdition already existing in database: " + entity.getCode());
			}
			catch (Exception e) {
				entity = new CourseEdition();
				entity.setCode(course.getCode().toLowerCase());
				entity.setName(course.getName());
				entity.setTemplateRepositoryUrl(course.getTemplateRepositoryUrl());
				entity.setStart(course.isStarted() ? new Date() : null);
				entity.setEnd(course.isEnded() ? new Date() : null);
				entity.setMinGroupSize(course.getMinGroupSize());
				entity.setMaxGroupSize(course.getMaxGroupSize());
				entity.setBuildTimeout(course.getBuildTimeout());
				courses.persist(entity);

				log.debug("Persisted course: " + entity.getCode());
			}
			
			Map<Long, Assignment> assignmentEntities = new HashMap<Long, Assignment>();

            for(BAssignment assignment : course.getAssignments()) {
                Assignment assignmentEntity = new Assignment();
                assignmentEntity.setCourse(entity);
                assignmentEntity.setName(assignment.getName());
                assignmentEntity.setAssignmentId(assignment.getId());
                assignments.persist(assignmentEntity);
                log.debug("Persistted assignment {} in {}", assignmentEntity.getName(), course.getCode());
                
                // Store for later use to insert deliveries
                assignmentEntities.put(assignmentEntity.getAssignmentId(), assignmentEntity);
            }

			GroupModel courseGroupModel = new GroupModel();
			courseGroupModel.setName("@" + entity.getCode().toLowerCase());
			courseGroupModel.setMembers(Lists.<IdentifiableModel> newArrayList());
			
			
			for (String assistantNetId : course.getAssistants()) {
				User assistantUser = userMapping.get(assistantNetId);
				
				CourseAssistant assistant = new CourseAssistant();
				assistant.setCourse(entity);
				assistant.setUser(assistantUser);
				assistants.persist(assistant);
				
				UserModel userModel = gitClient.users()
						.ensureExists(assistantNetId);
				
				courseGroupModel.getMembers().add(userModel);
				
				log.debug("    Persisted assistant: " + assistantUser.getNetId());
			}
			
			courseGroupModel = gitClient.groups().ensureExists(courseGroupModel);
			
			for (BGroup group : course.getGroups()) {
				Group groupEntity = new Group();
				groupEntity.setCourse(entity);
				groupEntity.setGroupNumber(group.getGroupNumber());
				groupEntity.setBuildTimeout(group.getBuildTimeout());
				groupEntity.setRepositoryName("courses/"
						+ entity.getCode().toLowerCase() + "/group-"
						+ group.getGroupNumber());
				groups.persist(groupEntity);
				
				log.debug("    Persisted group: " + groupEntity.getGroupName());

				List<User> members = Lists.<User> newArrayList();
				
				for (String member : group.getMembers()) {
					User memberUser = userMapping.get(member);
					members.add(memberUser);
					
					GroupMembership membership = new GroupMembership();
					membership.setGroup(groupEntity);
					membership.setUser(memberUser);
					memberships.persist(membership);
					
					log.debug("        Persisted member: " + memberUser.getNetId());
				}
				
				try {
					// Allow cached versions of the repository
					gitClient.repositories().retrieve(groupEntity.getRepositoryName());
					log.info("Repository {} already exists", groupEntity.getRepositoryName());
				}
				catch (Exception e) {
					projects.provisionRepository(groupEntity, members);
				}
				
				for (BDelivery delivery : group.getDeliveries()) {
					Delivery deliveryEntity = new Delivery();
					deliveryEntity.setAssignment(assignmentEntities.get(delivery.getAssignmentId()));
					deliveryEntity.setCreated(new Date());
					deliveryEntity.setGroup(groupEntity);
					deliveryEntity.setCreatedUser(userMapping.get(delivery.getCreatedUserName()));
					
					BReview review;
					if ((review = delivery.getReview()) != null) {
						Review reviewEntity = new Review();
						reviewEntity.setState(Delivery.State.valueOf(review.getState()));
						reviewEntity.setGrade(review.getGrade());
						reviewEntity.setReviewUser(userMapping.get(review.getReviewedUserName()));
						reviewEntity.setReviewTime(new Date(System.currentTimeMillis()));
						deliveryEntity.setReview(reviewEntity);
						
						log.info("                Set review for delivery: " + groupEntity.getGroupId());
					}
					
					deliveries.persist(deliveryEntity);
					
					log.debug("        Persisted delivery for group: " + groupEntity.getGroupId());
				}
			}
		}
	}
}
