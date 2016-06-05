package nl.tudelft.ewi.devhub.server.util;

import com.google.common.collect.Maps;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.web.api.CommitApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by Douwe Koopmans on 31-5-16.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlattenFolderTreeTest {
    @InjectMocks private FlattenFolderTree ffTree;
    @Mock private CommitApi commitApi;

    @Before
    public void setUp() throws Exception {
        final Map<String, EntryType> rootTree = Maps.newLinkedHashMap();
        rootTree.put("README", EntryType.TEXT);
        rootTree.put("foobar", EntryType.TEXT);
        rootTree.put("src/", EntryType.FOLDER);

        final LinkedHashMap<String, EntryType> srcFolderTree = Maps.newLinkedHashMap();
        srcFolderTree.put("main/", EntryType.FOLDER);

        final LinkedHashMap<String, EntryType> mainFolderTree = Maps.newLinkedHashMap();
        mainFolderTree.put("java/", EntryType.FOLDER);
        mainFolderTree.put("resources/", EntryType.FOLDER);

        final LinkedHashMap<String, EntryType> resourcesFolderTree = Maps.newLinkedHashMap();
        resourcesFolderTree.put("logback.xml", EntryType.TEXT);

        final LinkedHashMap<String, EntryType> javaFolderTree = Maps.newLinkedHashMap();
        javaFolderTree.put("com/", EntryType.FOLDER);

        final LinkedHashMap<String, EntryType> comFolderTree = Maps.newLinkedHashMap();
        comFolderTree.put("foo/", EntryType.FOLDER);

        final LinkedHashMap<String, EntryType> fooFolderTree = Maps.newLinkedHashMap();
        fooFolderTree.put("bar/", EntryType.FOLDER);

        final LinkedHashMap<String, EntryType> barFolderTree = Maps.newLinkedHashMap();
        barFolderTree.put("Bar.java", EntryType.TEXT);

        when(commitApi.showTree()).thenReturn(rootTree);
        when(commitApi.showTree(CommitApi.EMPTY_PATH)).thenReturn(rootTree);
        when(commitApi.showTree("src/")).thenReturn(srcFolderTree);
        when(commitApi.showTree("src/main/")).thenReturn(mainFolderTree);
        when(commitApi.showTree("src/main/java/")).thenReturn(javaFolderTree);
        when(commitApi.showTree("src/main/resources/")).thenReturn(resourcesFolderTree);
        when(commitApi.showTree("src/main/java/com/")).thenReturn(comFolderTree);
        when(commitApi.showTree("src/main/java/com/foo/")).thenReturn(fooFolderTree);
        when(commitApi.showTree("src/main/java/com/foo/bar/")).thenReturn(barFolderTree);
    }

    @Test
    public void testRoot() {
        final TreeMap<String, EntryType> expectedEntries = new TreeMap<>();
        expectedEntries.put("README", EntryType.TEXT);
        expectedEntries.put("foobar", EntryType.TEXT);
        expectedEntries.put("src/main/", EntryType.FOLDER);

        assertEquals(expectedEntries, ffTree.resolveEntries());
    }

    @Test
    public void testSimple() throws Exception {
        final TreeMap<String, EntryType> expectedEntries = new TreeMap<>();
        expectedEntries.put("java/", EntryType.FOLDER);
        expectedEntries.put("resources/", EntryType.FOLDER);

        assertEquals(expectedEntries,ffTree.resolveEntries("src/main/"));
    }
}