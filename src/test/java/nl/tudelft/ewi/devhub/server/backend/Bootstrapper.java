package nl.tudelft.ewi.devhub.server.backend;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.CourseAssistants;
import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.GroupMemberships;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseAssistant;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupMembership;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.git.client.GitServerClientMock;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;
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
	}
	
	@Data
	static class BGroup {
		private Long groupNumber;
		private Integer buildTimeout;
		private String templateRepositoryUrl;
		private List<String> members;
	}
	
	private final Users users;
	private final Courses courses;
	private final CourseAssistants assistants;
	private final Groups groups;
	private final GroupMemberships memberships;
	private final MockedAuthenticationBackend authBackend;
	private final ObjectMapper mapper;
	private final GitServerClientMock gitClient;

	@Inject
	Bootstrapper(Users users, Courses courses, CourseAssistants assistants, Groups groups, 
			GroupMemberships memberships, MockedAuthenticationBackend authBackend, ObjectMapper mapper,
			GitServerClientMock gitClient) {
		
		this.users = users;
		this.courses = courses;
		this.assistants = assistants;
		this.groups = groups;
		this.memberships = memberships;
		this.authBackend = authBackend;
		this.mapper = mapper;
		this.gitClient = gitClient;
	}
	
	@Transactional
	public void prepare(String path) throws IOException {
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
			Course entity = new Course();
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
			
			GroupModel groupModel = new GroupModel();
			groupModel.setName(entity.getCode());
			
			gitClient.groups()
					.create(groupModel);
			
			for (String assistantNetId : course.getAssistants()) {
				User assistantUser = userMapping.get(assistantNetId);
				
				CourseAssistant assistant = new CourseAssistant();
				assistant.setCourse(entity);
				assistant.setUser(assistantUser);
				assistants.persist(assistant);
				
				UserModel userModel = gitClient.users()
						.ensureExists(assistantNetId);
				
				gitClient.groups()
						.groupMembers(groupModel)
						.addMember(userModel);
				
				log.debug("    Persisted assistant: " + assistantUser.getNetId());
			}
			
			for (BGroup group : course.getGroups()) {
				Group groupEntity = new Group();
				groupEntity.setCourse(entity);
				groupEntity.setGroupNumber(group.getGroupNumber());
				groupEntity.setBuildTimeout(group.getBuildTimeout());
				groupEntity.setRepositoryName("courses/" + entity.getCode() + "/group-" + group.getGroupNumber());
				groups.persist(groupEntity);
				
				log.debug("    Persisted group: " + groupEntity.getGroupName());

				CreateRepositoryModel repositoryModel = new CreateRepositoryModel();
				repositoryModel.setName(groupEntity.getRepositoryName());
				repositoryModel.setTemplateRepository(group.getTemplateRepositoryUrl());
				
				Map<String, Level> permissions = Maps.newHashMap();
				permissions.put(groupModel.getName(), Level.ADMIN);
				
				for (String member : group.getMembers()) {
					User memberUser = userMapping.get(member);
					
					GroupMembership membership = new GroupMembership();
					membership.setGroup(groupEntity);
					membership.setUser(memberUser);
					memberships.persist(membership);
					
					permissions.put(member, Level.READ_WRITE);
					
					log.debug("        Persisted member: " + memberUser.getNetId());
				}
				
				repositoryModel.setPermissions(permissions);
				
				gitClient.repositories()
						.create(repositoryModel);
			}
		}
	}
}
