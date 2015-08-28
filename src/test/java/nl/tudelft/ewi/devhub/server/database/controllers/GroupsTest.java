package nl.tudelft.ewi.devhub.server.database.controllers;

import lombok.Getter;
import nl.tudelft.ewi.devhub.server.backend.PersistedBackendTest;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class GroupsTest extends PersistedBackendTest {

	@Inject @Getter private Users users;
	@Inject @Getter private Groups groups;
	@Inject @Getter private CourseEditions courses;

	private CourseEdition courseEdition;

	@Before
	public void setUp() {
		courseEdition = createCourseEdition();
	}

	@Test(expected = PersistenceException.class)
	public void testInsertGroupWithoutCourse() {
		Group group = new Group();
		groups.persist(group);
	}
	
	@Test
	public void testAutomaticGroupNumberGeneration() {
		Group group = new Group();
		group.setCourseEdition(courseEdition);
		groups.persist(group);
	}
	
	@Test
	public void testInsertGroupWithRepository() {
		Group group = new Group();
		group.setCourseEdition(courseEdition);
		groups.persist(group);

		GroupRepository groupRepository = new GroupRepository();
		groupRepository.setRepositoryName(courseEdition.createRepositoryName(group).toASCIIString());
		group.setRepository(groupRepository);
		groups.merge(group);
	}
	
	@Test(expected=PersistenceException.class)
	public void testInsertGroupWithSameRepository() {
		Group group = new Group();
		group.setCourseEdition(courseEdition);
		groups.persist(group);
		
		Group otherGroup = new Group();
		otherGroup.setCourseEdition(group.getCourseEdition());
		groups.persist(otherGroup);

		GroupRepository groupRepository = new GroupRepository();
		groupRepository.setRepositoryName(courseEdition.createRepositoryName(group).toASCIIString());

		group.setRepository(groupRepository);
		groups.merge(group);

		otherGroup.setRepository(groupRepository);
		groups.merge(otherGroup);
	}

	@Test(expected=PersistenceException.class)
	@Ignore("The current implementation of FKSegmentedIdentifierGenerator will always override a manually set identifier")
	public void testUnableToInsertWithSameGroupNumber() {
		Group group = new Group();
		group.setCourseEdition(courseEdition);
		groups.persist(group);
		
		Group otherGroup = new Group();
		otherGroup.setCourseEdition(group.getCourseEdition());
		otherGroup.setGroupNumber(group.getGroupNumber());
		groups.persist(otherGroup);
	}

	@Test
	public void testListPersistedGroup() {
		Group group = new Group();
		group.setCourseEdition(courseEdition);
		groups.persist(group);
		assertThat(groups.find(courseEdition), hasItem(group));
	}

	@Test
	public void testFindByGroupNumber() {
		Group group = new Group();
		group.setCourseEdition(courseEdition);
		groups.persist(group);
		assertEquals(group, groups.find(courseEdition, group.getGroupNumber()));
	}
	
	@Test
	public void testFindByRepoName() {
		Group group = new Group();
		group.setCourseEdition(courseEdition);
		groups.persist(group);

		GroupRepository groupRepository = new GroupRepository();
		String repoName = courseEdition.createRepositoryName(group).toASCIIString();
		groupRepository.setRepositoryName(repoName);
		group.setRepository(groupRepository);
		groups.merge(group);

		assertEquals(group, groups.findByRepoName(repoName));
	}
	
}
