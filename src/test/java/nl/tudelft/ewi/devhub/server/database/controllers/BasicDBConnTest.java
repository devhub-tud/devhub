package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.persist.jpa.JpaPersistModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.PersistenceConfiguration;
import nl.tudelft.ewi.devhub.server.database.embeddables.TimeSpan;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Created by Jan-Willem on 8/15/2015.
 */
@Slf4j
@RunWith(JukitoRunner.class)
@UseModules(BasicDBConnTest.DbModule.class)
public class BasicDBConnTest {

	public static class DbModule extends AbstractModule {

		@Override
		@SneakyThrows
		protected void configure() {
			JpaPersistModule jpaModule = new JpaPersistModule("default");
			Properties properties = PersistenceConfiguration.load();
			properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
			properties.setProperty("hibernate.show_sql", "true");
			jpaModule.properties(properties);

			install(jpaModule);
			bind(TestDatabaseModule.JPAInitializer.class).asEagerSingleton();
		}

	}

	@Inject
	private Users users;

	@Inject
	private Courses courses;

	@Inject
	private Groups groups;

	@Test
	public void test(){
		User user = new User();
		user.setEmail("jgmeligmeyling@devhub");
		user.setAdmin(true);
		user.setName("Jan-Willem Gmelig Meyling");
		user.setNetId("jgmeligmeyling");
		user.setStudentNumber("12345");
		users.persist(user);

		User actual = users.findByNetId("jgmeligmeyling");
		assertEquals(user, actual);

		Course course = new Course();
		course.setName("Software Kwaliteit & Testen");
		course.setCode("ti1706");

		CourseEdition courseEdition = new CourseEdition();
		courseEdition.setCourse(course);
		courseEdition.setTimeSpan(new TimeSpan(new Date(), null));
		courseEdition.setMaxGroupSize(2);
		courseEdition.setMinGroupSize(1);
		courseEdition.setTemplateRepositoryUrl("lupa");
		courseEdition.setAssistants(Sets.newHashSet());
		courses.persist(courseEdition);

		assertEquals(courses.find("ti1706"), courseEdition);

		Group group = new Group();
		group.setCourseEdition(courseEdition);
		group.setMembers(Sets.newHashSet(actual));
		GroupRepository groupRepository = new GroupRepository();
		groupRepository.setRepositoryName("test");
		group.setRepository(groupRepository);
		groups.persist(group);

		User user2 = new User();
		user2.setEmail("lclark@devhub");
		user2.setAdmin(true);
		user2.setName("Liam Clark");
		user2.setNetId("lclark");
		user2.setStudentNumber("54321");
		users.persist(user2);

		group.getMembers().add(user2);
		groups.merge(group);

		courseEdition.getAssistants().add(user2);
		courses.merge(courseEdition);

		User carsten = new User();
		carsten.setEmail("carsten@devhub");
		carsten.setAdmin(true);
		carsten.setName("Carsten");
		carsten.setNetId("carsten");
		carsten.setStudentNumber("64321");
		users.persist(carsten);

		Group groupB = new Group();
		groupB.setCourseEdition(courseEdition);
		groupB.setMembers(Sets.newHashSet(carsten));
		GroupRepository groupRepositoryB = new GroupRepository();
		groupRepositoryB.setRepositoryName("test/b");
		groupB.setRepository(groupRepositoryB);
		groups.persist(groupB);

		groupB.getMembers().add(carsten);
		groups.merge(groupB);

		CourseEdition courseEdition2 = new CourseEdition();
		courseEdition2.setCourse(course);
		courseEdition2.setTimeSpan(new TimeSpan(new Date(), null));
		courseEdition2.setMaxGroupSize(2);
		courseEdition2.setMinGroupSize(1);
		courseEdition2.setTemplateRepositoryUrl("lupa");
		courseEdition2.setAssistants(Sets.newHashSet());
		courses.persist(courseEdition2);


		Group groupC = new Group();
		groupC.setCourseEdition(courseEdition2);
		groupC.setMembers(Sets.newHashSet(carsten));
		GroupRepository groupRepositoryC = new GroupRepository();
		groupRepositoryC.setRepositoryName("test/c");
		groupC.setRepository(groupRepositoryC);
		groupC.setMembers(Sets.newHashSet(carsten));
		groups.persist(groupC);

		log.info("Group 1: {}", group);
		log.info("Group 2: {}", groupB);
		log.info("Group 3: {}", groupC);
	}

}
