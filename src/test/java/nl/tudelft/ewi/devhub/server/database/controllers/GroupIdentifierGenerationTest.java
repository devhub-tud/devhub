package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import com.google.inject.persist.jpa.JpaPersistModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.PersistenceConfiguration;
import nl.tudelft.ewi.devhub.server.database.embeddables.TimeSpan;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Jan-Willem on 8/15/2015.
 */
@Slf4j
public class GroupIdentifierGenerationTest {

	public static int AMOUNT_OF_USERS = 100;
	public static int AMOUNT_OF_COURSE_EDITIONS = 2;
	public static int AMOUNT_OF_GROUP_MEMBERS = 2;

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

	private final Injector injector = Guice.createInjector(new DbModule());

	private static ExecutorService executorService;

	@BeforeClass
	public static void createExecutorService() {
		executorService = Executors.newScheduledThreadPool(40);
	}

	@AfterClass
	public static void stopExecutor() {
		executorService.shutdownNow();
	}


	@Test
	@Ignore
	public void test() throws Exception {
		List<User> users = performInTransaction(TaskInTransaction::createUsers).get();
		List<CourseEdition> courseEditions = performInTransaction(TaskInTransaction::createCourseEditions).get();

		List<Future<Group>> groupFutures = courseEditions.stream()
			.flatMap(courseEdition -> {
				Deque<User> userQueue = Queues.newArrayDeque(users);
				List<Group> createdGroups = Lists.newArrayList();

				while (!userQueue.isEmpty()) {
					Set<User> groupMembers = Sets.newHashSet();
					while (groupMembers.size() < AMOUNT_OF_GROUP_MEMBERS && !userQueue.isEmpty()) {
						groupMembers.add(userQueue.removeFirst());
					}
					Group group = new Group();
					group.setCourseEdition(courseEdition);
					group.setMembers(groupMembers);
					createdGroups.add(group);
				}
				return createdGroups.stream();
			})
			.parallel()
			.map(detachedGroup -> performInTransaction(taskInTransaction -> taskInTransaction.createGroup(detachedGroup)))
			.collect(Collectors.toList());

		SortedSet<Group> sortedGroups = Sets.newTreeSet();
		for(Future<Group> future : groupFutures) {
			sortedGroups.add(future.get());
		}

		for(Group group : sortedGroups) {
			log.info("Group : {}", group);
		}

	}

	protected <T> Future<T> performInTransaction(final ActionInTransaction<T> action) {
		return executorService.submit(() -> {
			Injector sub = injector.createChildInjector();
			UnitOfWork unitOfWork = sub.getInstance(UnitOfWork.class);
			try {
				unitOfWork.begin();
				TaskInTransaction taskInTransaction = sub.getInstance(TaskInTransaction.class);
				return action.call(taskInTransaction);
			} finally {
				unitOfWork.end();
			}
		});
	}

	@FunctionalInterface
	interface ActionInTransaction<T> {

		T call(TaskInTransaction taskInTransaction);

	}

	protected static class TaskInTransaction {

		@Inject
		private Users users;

		@Inject
		private Groups groups;

		@Inject
		private Courses courses;

		@SneakyThrows
		@Transactional
		protected Group createGroup(Group detached) {
			CourseEdition nonDetachedCourseEdition = courses.find(detached.getCourseEdition().getId());
			Set<User> nonDetachedUsers = detached.getMembers().stream()
				.map(User::getId).map(users::find)
				.collect(Collectors.toSet());

			Group group = new Group();
			group.setCourseEdition(nonDetachedCourseEdition);
			group.setMembers(nonDetachedUsers);
			Thread.sleep(Math.round(Math.random() * 1000));
			return groups.persist(group);
		}

		@Transactional
		protected User createUser() {
			User user = new User();
			user.setNetId(randomString());
			user.setName(randomString());
			return users.persist(user);
		}

		@Transactional
		protected List<User> createUsers() {
			return Stream.generate(this::createUser)
				.limit(AMOUNT_OF_USERS)
				.collect(Collectors.toList());
		}

		@Transactional
		protected CourseEdition createCourseEdition() {
			Course course = new Course();
			course.setCode(randomString().substring(0, 6));
			course.setName(randomString());
			CourseEdition courseEdition = new CourseEdition();
			courseEdition.setCourse(course);
			courseEdition.setTemplateRepositoryUrl(randomString());
			courseEdition.setMinGroupSize(1);
			courseEdition.setMaxGroupSize(8);
			courseEdition.setTimeSpan(new TimeSpan(new Date(), null));
			return courses.persist(courseEdition);
		}

		@Transactional
		protected List<CourseEdition> createCourseEditions() {
			return Stream.generate(this::createCourseEdition)
				.limit(AMOUNT_OF_COURSE_EDITIONS)
				.collect(Collectors.toList());
		}

	}

	private static final Random random = new Random();

	protected static String randomString() {
		return new BigInteger(130, random).toString(32);
	}


}
