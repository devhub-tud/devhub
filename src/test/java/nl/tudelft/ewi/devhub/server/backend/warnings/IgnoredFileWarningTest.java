package nl.tudelft.ewi.devhub.server.backend.warnings;

import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.IgnoredFileWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.IllegalFileWarning;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffContext;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffFile;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffLine;
import nl.tudelft.ewi.git.models.ChangeType;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.EntryType;

import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(MockitoJUnitRunner.class)
public class IgnoredFileWarningTest {

    private final static String COMMIT_ID = "abcd";
    private Group group;
	private GroupRepository groupRepository;
    private CourseEdition course;
	private nl.tudelft.ewi.devhub.server.database.entities.Commit commitEntity;

    @Mock private RepositoriesApi repositories;
    @Mock private RepositoryApi repository;
    @Mock private CommitApi commitApi;
    @Mock private DetailedCommitModel commitModel;
    @Mock private DiffModel diffModel;
    @InjectMocks  private IgnoredFileWarningGenerator generator;
    
    private DiffFile<DiffContext<DiffLine>> diffFile;
    private List<DiffFile<DiffContext<DiffLine>>> diffValues;
    
    private IgnoredFileWarning warning;

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

    	diffFile = new DiffFile<>();
    	diffFile.setType(ChangeType.ADD);

        when(repositories.getRepository(anyString())).thenReturn(repository);
        when(repository.getCommit(COMMIT_ID)).thenReturn(commitApi);
        when(commitApi.diff()).thenReturn(diffModel);

        warning = new IgnoredFileWarning();
        warning.setRepository(groupRepository);
        warning.setCommit(commitEntity);

    }

    @SuppressWarnings("unchecked")
	@Test
    public void TestIngoredFile(){
    	diffFile.setNewPath("documentation.docx");
    	diffValues = Lists.newArrayList(diffFile);
		when(diffModel.getDiffs()).thenReturn(diffValues);
    	
        Set<IgnoredFileWarning> warnings = generator.generateWarnings(commitEntity, null);
        
        warning.setFileName("documentation.docx");
        assertEquals(warning,warnings.stream().findFirst().get());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void TestNotIngoredFile(){
    	diffFile.setNewPath("README.md");
    	diffValues = Lists.newArrayList(diffFile);
		when(diffModel.getDiffs()).thenReturn(diffValues);
    	
        Set<IgnoredFileWarning> warnings = generator.generateWarnings(commitEntity, null);
        warning.setFileName("README.md");
        assertEquals(Collections.emptySet(), warnings);
    }

}
