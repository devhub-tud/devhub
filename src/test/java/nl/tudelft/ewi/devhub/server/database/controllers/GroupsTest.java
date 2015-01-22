package nl.tudelft.ewi.devhub.server.database.controllers;

import java.util.Random;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
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
	
	@Test(expected=ConstraintViolationException.class)
	public void testInsertGroupWithoutCourse() {
		Group group = new Group();
		group.setRepositoryName("courses/ti1705/group-1");
		group.setGroupNumber(5l);
		groups.persist(group);
	}
	
	@Test(expected=ConstraintViolationException.class)
	public void testInsertGroupWithoutGroupNumber() {
		Course course = getTestCourse();
		Group group = new Group();
		group.setRepositoryName("courses/ti1705/group-1");
		group.setCourse(course);
		groups.persist(group);
	}
	
	@Test(expected=ConstraintViolationException.class)
	public void testInsertGroupWithoutRepositoryName() {
		Course course = getTestCourse();
		Group group = new Group();
		group.setGroupNumber(6l);
		group.setCourse(course);
		groups.persist(group);
	}
	
	@Test(expected=PersistenceException.class)
	public void testUnableToInsertWithSameId() {
		Group group = createGroup();
		groups.persist(group);
		
		Group otherGroup = new Group();
		otherGroup.setCourse(group.getCourse());
		otherGroup.setGroupId(group.getGroupId());
		otherGroup.setGroupNumber(random.nextLong());
		otherGroup.setRepositoryName(String.format("courses/%s/group-%s",
				group.getGroupNumber(), group.getCourse().getName()));
		groups.persist(otherGroup);
	}
	
	@Test(expected=PersistenceException.class)
	public void testUnableToInsertWithSameRepoName() {
		Group group = createGroup();
		groups.persist(group);
		
		Group otherGroup = new Group();
		otherGroup.setCourse(group.getCourse());
		otherGroup.setGroupNumber(random.nextLong());
		otherGroup.setRepositoryName(group.getRepositoryName());
		groups.persist(otherGroup);
	}
	
	@Test(expected=PersistenceException.class)
	public void testUnableToInsertWithSameGroupNumber() {
		Group group = createGroup();
		groups.persist(group);
		
		Group otherGroup = new Group();
		otherGroup.setCourse(group.getCourse());
		otherGroup.setGroupNumber(group.getGroupNumber());
		otherGroup.setRepositoryName(group.getRepositoryName().concat("B"));
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
		Course course = group.getCourse();
		groups.persist(group);
		assertThat(groups.find(course), hasItem(group));
	}
	
	@Test
	public void testFindById() {
		Group group = createGroup();
		groups.persist(group);
		assertEquals(group, groups.find(group.getGroupId()));
	}
	
	@Test
	public void testFindByGroupNumber() {
		Group group = createGroup();
		Course course = group.getCourse();
		groups.persist(group);
		assertEquals(group, groups.find(course, group.getGroupNumber()));
	}
	
	@Test
	public void testFindByRepoName() {
		Group group = createGroup();
		String repoName = group.getRepositoryName();
		groups.persist(group);
		assertEquals(group, groups.findByRepoName(repoName));
	}
	
	protected Group createGroup() {
		Group group = new Group();
		Course course = getTestCourse();
		group.setGroupNumber(random.nextLong());
		group.setCourse(course);
		group.setRepositoryName(String.format("courses/%s/group-%s", group.getGroupNumber(), course.getName()));
		return group;
	}
	
	protected Course getTestCourse() {
		return courses.find("TI1705");
	}
	
}
