package nl.tudelft.ewi.devhub.server.backend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.*;
import nl.tudelft.ewi.devhub.server.database.embeddables.TimeSpan;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery.Review;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.builds.MavenBuildInstructionEntity;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.Notification;
import nl.tudelft.ewi.devhub.server.database.entities.notifications.NotificationsToUsers;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.models.UserModel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.git.web.api.GroupsApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class Bootstrapper {

	@Data
	static class BState {
		private List<BUser> users;
		private List<BCourse> courses;
	}
	
	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	static class BUser {
		private String name;
		private String email;
		private String netId;
		private String studentNumber;
		private String password;
		private boolean admin;
	}

	@Data
	static class BCourse {
		private String code;
		private String name;
		private List<BCourseEdition> editions;
	}
	
	@Data
	static class BCourseEdition {
		private String code;
		private String templateRepositoryUrl;
		private boolean started;
		private boolean ended;
		private int minGroupSize;
		private int maxGroupSize;
		private Integer buildTimeout;
		private List<String> assistants;
		private List<BGroup> groups;
		private List<Assignment> assignments;
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
	private final CourseEditions courseEditions;
	private final Groups groups;
	private final MockedAuthenticationBackend authBackend;
	private final ObjectMapper mapper;
	private final RepositoriesApi repositoriesApi;
	private final GroupsApi groupsApi;
	private final ProjectsBackend projects;
	private final Deliveries deliveries;
    private final Assignments assignments;
    private final NotificationController notificationController;
    private final NotificationUserController notificationUserController;

	@Inject
	Bootstrapper(Users users, Courses courses, CourseEditions courseEditions, Groups groups,
			MockedAuthenticationBackend authBackend, ObjectMapper mapper,
			RepositoriesApi repositoriesApi, ProjectsBackend projects, Assignments assignments,
			Deliveries deliveries, GroupsApi groupsApi, NotificationController notificationController,
				 NotificationUserController notificationsUserController) {
		
		this.users = users;
		this.courses = courses;
		this.courseEditions = courseEditions;
		this.groups = groups;
		this.authBackend = authBackend;
		this.mapper = mapper;
		this.repositoriesApi = repositoriesApi;
		this.projects = projects;
		this.assignments = assignments;
		this.deliveries = deliveries;
		this.groupsApi = groupsApi;
		this.notificationController = notificationController;
		this.notificationUserController = notificationsUserController;
	}
	
	@Transactional
	public void prepare(String path) throws IOException, ApiError, URISyntaxException {
		InputStream inputStream = Bootstrapper.class.getResourceAsStream(path);
		BState state = mapper.readValue(inputStream, BState.class);
		
		Map<String, User> userMapping = Maps.newHashMap();
		for (BUser user : state.getUsers()) {
			User entity = new User();
			entity.setName(user.getName());
			entity.setEmail(user.getEmail());
			entity.setNetId(user.getNetId());
			entity.setStudentNumber(user.getStudentNumber());
			entity.setAdmin(user.isAdmin());
			users.persist(entity);
			
			authBackend.addUser(user.getNetId(), user.getPassword(), user.isAdmin());
			userMapping.put(entity.getNetId(), entity);
			log.debug("Persisted user: " + entity.getNetId());
		}
		
		for (BCourse course : state.getCourses()) {
			Course courseEntity;

			try {
				courseEntity = courses.find(course.getCode());
				log.debug("Course already existing in database: {}", courseEntity);
			}
			catch (EntityNotFoundException e) {
				courseEntity = new Course();
				courseEntity.setCode(course.getCode().toLowerCase());
				courseEntity.setName(course.getName());
				courses.persist(courseEntity);

				log.debug("Persisted course: " + courseEntity.getCode());
			}

			for (BCourseEdition bCourseEdition : course.getEditions()) {
				CourseEdition entity;

				try {
					entity = courseEditions.getActiveCourseEdition(courseEntity);
					log.debug("CourseEdition already existing in database: {}", entity);
				}
				catch (EntityNotFoundException e) {
					entity = new CourseEdition();
					entity.setCourse(courseEntity);
					entity.setCode(bCourseEdition.getCode());
					entity.setTemplateRepositoryUrl(bCourseEdition.getTemplateRepositoryUrl());
					entity.setTimeSpan(new TimeSpan(
						bCourseEdition.isStarted() ? new Date() : null,
						bCourseEdition.isEnded() ? new Date() : null));

					entity.setMinGroupSize(bCourseEdition.getMinGroupSize());
					entity.setMaxGroupSize(bCourseEdition.getMaxGroupSize());
					entity.setAssistants(Sets.newHashSet());
					courseEditions.persist(entity);

					log.debug("Persisted course: " + entity.getCode());
				}

				Map<Long, Assignment> assignmentEntities = new HashMap<Long, Assignment>();

				for(Assignment assignment : bCourseEdition.getAssignments()) {
					assignment.setCourseEdition(entity);
					assignment.setName(assignment.getName());
					assignments.merge(assignment);
					log.debug("Persistted assignment {} in {}", assignment.getName(), course.getCode());

					// Store for later use to insert deliveries
					assignmentEntities.put(assignment.getAssignmentId(), assignment);
				}

				GroupModel courseGroupModel = new GroupModel();
				courseGroupModel.setName(projects.gitoliteAssistantGroupName(entity));
				courseGroupModel.setMembers(Lists.<IdentifiableModel>newArrayList());

				for (String assistantNetId : bCourseEdition.getAssistants()) {
					User assistantUser = userMapping.get(assistantNetId);
					entity.getAssistants().add(assistantUser);
					courseEditions.merge(entity);

					UserModel userModel = new UserModel();
					userModel.setName(assistantNetId);
					courseGroupModel.getMembers().add(userModel);

					log.debug("    Persisted assistant: " + assistantUser.getNetId());
				}

				groupsApi.create(courseGroupModel);

				Notification notification = new Notification();
				notification.setEvent("PR");
				notification.setMessage("Some message");
				notification.setSender(userMapping.get("admin1"));
				//notification.setLink(URI.create("thelink"));
				notification.setLink(new URI("http://localhost:50001/"));
				notificationController.persist(notification);


				Notification notification2 = new Notification();
				notification2.setEvent("PR");
				notification2.setMessage("Some message");
				notification2.setSender(userMapping.get("admin1"));
				notification2.setLink(new URI("http://localhost:50001/"));
				notificationController.persist(notification2);

				NotificationsToUsers notificationsToUsers1 = new NotificationsToUsers();
				notificationsToUsers1.setNotification(notification);
				notificationsToUsers1.setUser(userMapping.get("admin1"));
				notificationsToUsers1.setRead(true);

				notificationUserController.persist(notificationsToUsers1);

				NotificationsToUsers notificationsToUsers2 = new NotificationsToUsers();
				notificationsToUsers2.setNotification(notification2);
				notificationsToUsers2.setUser(userMapping.get("admin1"));
				notificationsToUsers2.setRead(false);

				notificationUserController.persist(notificationsToUsers2);

				for (BGroup group : bCourseEdition.getGroups()) {
					prepareGroup(userMapping, entity, assignmentEntities, group);
				}
			}
		}
	}

	private void prepareGroup(Map<String, User> userMapping, CourseEdition entity, Map<Long, Assignment> assignmentEntities, BGroup group) throws ApiError {
		Group groupEntity = new Group();
		groupEntity.setCourseEdition(entity);
		groupEntity.setMembers(Sets.newHashSet());
		groupEntity.setGroupNumber(group.getGroupNumber());

		MavenBuildInstructionEntity mavenBuildInstructionEntity = new MavenBuildInstructionEntity();
		mavenBuildInstructionEntity.setCheckstyle(true);
		mavenBuildInstructionEntity.setFindbugs(true);
		mavenBuildInstructionEntity.setPmd(true);
		mavenBuildInstructionEntity.setWithDisplay(true);
		mavenBuildInstructionEntity.setBuildTimeout(600);
		mavenBuildInstructionEntity.setCommand("test");

		GroupRepository groupRepository = new GroupRepository();
		groupRepository.setRepositoryName(entity.createRepositoryName(groupEntity).toASCIIString());
		groupRepository.setBuildInstruction(mavenBuildInstructionEntity);
		groupEntity.setRepository(groupRepository);

		groups.persist(groupEntity);

		log.debug("    Persisted group: " + groupEntity.getGroupName());

		Set<User> members = groupEntity.getMembers();

		for (String member : group.getMembers()) {
			User memberUser = userMapping.get(member);
			members.add(memberUser);

			groups.merge(groupEntity);
			log.debug("        Persisted member: " + memberUser.getNetId());
		}

		try {
			// Allow cached versions of the repository
			repositoriesApi.getRepository(groupRepository.getRepositoryName());
			log.info("Repository {} already exists", groupRepository.getRepositoryName());
		}
		catch (Exception e) {
			projects.provisionRepository(groupEntity, members);
		}

		for (BDelivery delivery : group.getDeliveries()) {
			Delivery deliveryEntity = new Delivery();
			deliveryEntity.setAssignment(assignmentEntities.get(delivery.getAssignmentId()));
			deliveryEntity.setGroup(groupEntity);
			deliveryEntity.setCreatedUser(userMapping.get(delivery.getCreatedUserName()));
			deliveryEntity.setStudents(Sets.newHashSet(groupEntity.getMembers()));

			BReview review;
			if ((review = delivery.getReview()) != null) {
				Review reviewEntity = new Review();
				reviewEntity.setState(Delivery.State.valueOf(review.getState()));
				reviewEntity.setGrade(review.getGrade());
				reviewEntity.setReviewUser(userMapping.get(review.getReviewedUserName()));
				reviewEntity.setReviewTime(new Date(System.currentTimeMillis()));
				deliveryEntity.setReview(reviewEntity);

				log.info("                Set review for delivery: " + groupEntity.getGroupNumber());
			}

			deliveries.persist(deliveryEntity);

			log.debug("        Persisted delivery for group: " + groupEntity.getGroupNumber());
		}
	}
}
