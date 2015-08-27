package nl.tudelft.ewi.devhub.server.database.controllers;

import java.util.Random;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class GroupsTest {
	
	@Inject
	private Random random;
	
	@Inject
	private Groups groups;
	
	@Inject
	private Courses courses;
	
	@Test(expected = PersistenceException.class)
	public void testInsertGroupWithoutCourse() {
		Group group = new Group();
		groups.persist(group);
	}
	
	@Test
	public void testAutomaticGroupNumberGeneration() {
		CourseEdition course = getTestCourse();
		Group group = new Group();
		group.setCourseEdition(course);
		groups.persist(group);
	}
	
	@Test
	public void testInsertGroupWithRepository() {
		CourseEdition course = getTestCourse();
		Group group = new Group();
		group.setCourseEdition(course);
		groups.persist(group);

		GroupRepository groupRepository = new GroupRepository();
		groupRepository.setRepositoryName(course.createRepositoryName(group).toASCIIString());
		group.setRepository(groupRepository);
		groups.merge(group);
	}
	
	@Test(expected=PersistenceException.class)
	public void testInsertGroupWithSameRepository() {
		CourseEdition course = getTestCourse();
		Group group = new Group();
		group.setCourseEdition(course);
		groups.persist(group);
		
		Group otherGroup = createGroup();
		otherGroup.setCourseEdition(group.getCourseEdition());
		groups.persist(otherGroup);

		GroupRepository groupRepository = new GroupRepository();
		groupRepository.setRepositoryName(course.createRepositoryName(group).toASCIIString());

		group.setRepository(groupRepository);
		groups.merge(group);

		otherGroup.setRepository(groupRepository);
		groups.merge(otherGroup);
	}

	@Test(expected=PersistenceException.class)
	@Ignore("The current implementation of FKSegmentedIdentifierGenerator will always override a manually set identifier")
	public void testUnableToInsertWithSameGroupNumber() {
		Group group = createGroup();
		groups.persist(group);
		
		Group otherGroup = createGroup();
		otherGroup.setCourseEdition(group.getCourseEdition());
		otherGroup.setGroupNumber(group.getGroupNumber());
		groups.persist(otherGroup);
	}
	
	@Test
	public void testPersistGroup() {
		Group group = createGroup();
		groups.persist(group);
	}
	
	@Test
	public void testListPersistedGroup() {
		Group group = createGroup();
		CourseEdition course = group.getCourse();
		groups.persist(group);
		assertThat(groups.find(course), hasItem(group));
	}

	@Test
	public void testFindByGroupNumber() {
		Group group = createGroup();
		CourseEdition course = group.getCourse();
		groups.persist(group);
		assertEquals(group, groups.find(course, group.getGroupNumber()));
	}
	
	@Test
	public void testFindByRepoName() {
		CourseEdition course = getTestCourse();
		Group group = new Group();
		group.setCourseEdition(course);
		groups.persist(group);

		GroupRepository groupRepository = new GroupRepository();
		String repoName = course.createRepositoryName(group).toASCIIString();
		groupRepository.setRepositoryName(repoName);
		group.setRepository(groupRepository);
		groups.merge(group);

		assertEquals(group, groups.findByRepoName(repoName));
	}
	
	protected Group createGroup() {
		Group group = new Group();
		CourseEdition course = getTestCourse();
		group.setCourseEdition(course);
		return group;
	}
	
	protected CourseEdition getTestCourse() {
		return courses.find("TI1705");
	}
	
}
