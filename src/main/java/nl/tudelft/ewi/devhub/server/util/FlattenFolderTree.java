package nl.tudelft.ewi.devhub.server.util;

import lombok.RequiredArgsConstructor;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.web.api.CommitApi;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RequiredArgsConstructor
public class FlattenFolderTree {

    public static final String PATH_SEPARATOR = "/";

    private final CommitApi commitApi;

    /**
     * This method flattens folder structures that contain only single folders, such as {@code src/main/java}.
     * @return The flattened tree map.
     */
    public Map<String, EntryType> resolveEntries() {
        return resolveEntries(CommitApi.EMPTY_PATH);
    }


    /**
     * This method flattens folder structures that contain only single folders, such as {@code src/main/java}.
     * @param basePath Requested path to get the tree structure for.
     * @return The flattened tree map.
     */
    public Map<String, EntryType> resolveEntries(final String basePath) {
        return resolveEntries(basePath, CommitApi.EMPTY_PATH);
    }

    private Map<String, EntryType> resolveEntries(final String basePath, final String suffix) {
        final String path = suffix.isEmpty() ? basePath : basePath.isEmpty() ? suffix : basePath + PATH_SEPARATOR + suffix;
        return commitApi.showTree(path).entrySet().stream()
            .map(entry -> {
                if (entry.getValue().equals(EntryType.FOLDER)){
                    Map<String, EntryType> subEntries = resolveEntries(basePath, suffix + entry.getKey());
                    if (subEntries.size() == 1 && subEntries.values().stream().findFirst().get().equals(EntryType.FOLDER)) {
                        return new HashMap.SimpleEntry<>(
                            entry.getKey() + subEntries.keySet().stream().findFirst().get(),
                            EntryType.FOLDER
                        );
                    }
                }
                return entry;
            })
            .sequential()
            .collect(Collectors.toMap(
                Entry::getKey,
                Entry::getValue,
                (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                TreeMap::new
            ));
    }

}
