package nl.tudelft.ewi.devhub.server.backend.warnings;

import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.IllegalFileWarning;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.EntryType;

import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(MockitoJUnitRunner.class)
public class IllegalFileWarningTest {

    private final static String COMMIT_ID = "abcd";
    private final Map<String,EntryType> directory1 = new HashMap<>();



    private Group group;
	private GroupRepository groupRepository;
    private CourseEdition course;
	private nl.tudelft.ewi.devhub.server.database.entities.Commit commitEntity;

    @Mock private RepositoriesApi repositories;
    @Mock private RepositoryApi repository;
    @Mock private CommitApi commitApi;
    @Mock private DetailedCommitModel commitModel;
    @InjectMocks  private IllegalFileWarningGenerator generator;

    private IllegalFileWarning warning;

    @Before
    public void beforeTest() throws Exception {
		course = new CourseEdition();
		group = new Group();
		group.setCourseEdition(course);
		groupRepository = new GroupRepository();
		groupRepository.setRepositoryName("");
		groupRepository.setGroup(group);
		group.setRepository(groupRepository);
		commitEntity = new nl.tudelft.ewi.devhub.server.database.entities.Commit();
		commitEntity.setCommitId(COMMIT_ID);
		commitEntity.setRepository(groupRepository);


        when(repositories.getRepository(anyString())).thenReturn(repository);
        when(repository.getCommit(COMMIT_ID)).thenReturn(commitApi);
        when(commitApi.showTree("")).thenReturn(directory1);

        directory1.clear();

        warning = new IllegalFileWarning();
        warning.setCommit(commitEntity);
    }

    @Test
    public void TestIngoredFile(){
        directory1.put("types.class",EntryType.TEXT);
        Collection<IllegalFileWarning> warnings = generator.generateWarnings(commitEntity, null);
        warning.setFileName("types.class");
        assertEquals(warning,warnings.stream().findFirst().get());
    }

    @Test
    public void TestNotIngoredFile(){
        directory1.put("types.java",EntryType.TEXT);
        Collection<IllegalFileWarning> warnings = generator.generateWarnings(commitEntity, null);
        IllegalFileWarning warning = new IllegalFileWarning();
        warning.setFileName("types.java");
        assertEquals(Collections.emptySet(), warnings);
    }

    @Test
    public void TestIgnoredFolder(){
        directory1.put("bin/",EntryType.FOLDER);
        Collection<IllegalFileWarning> warnings = generator.generateWarnings(commitEntity,null);
        warning.setFileName("bin/");
        assertEquals(warning ,warnings.stream().findFirst().get());
    }

}
