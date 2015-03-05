package nl.tudelft.ewi.devhub.server.backend;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.Config;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jgmeligmeyling on 05/03/15.
 */
@Slf4j
public class StorageBackend {

    private final File rootFolder;

    @Inject
    public StorageBackend(Config config) {
        rootFolder = config.getStorageFolder();
        rootFolder.mkdirs();
    }

    /**
     * Store a file in the StorageBackend
     * @param path relative folder path of file
     * @param fileName filename for file
     * @param in InputStream providing the file contents
     * @throws IOException if an I/O error occurs
     */
    public String store(String path, String fileName, InputStream in) throws IOException {
        File folder = new File(rootFolder, path);
        folder.mkdirs();
        File file = new File(folder, fileName);
        FileUtils.copyInputStreamToFile(in, file);
        return path.concat(File.separator).concat(fileName);
    }

    /**
     * Remove a file in the StorageBackend
     * @param path relative folder path of file
     * @param fileName filename for file
     * @throws IOException if an I/O error occurs
     */
    public void remove(String path, String fileName) throws IOException {
        File folder = new File(rootFolder, path);
        if(folder.exists()) {
            File file = new File(folder, fileName);
            FileUtils.forceDelete(file);
        }
    }

    /**
     * Remove a file in the StorageBackend
     * @param path relative folder path of file
     * @param fileName filename for file
     */
    public void removeSilently(String path, String fileName) {
        try {
            remove(path, fileName);
        }
        catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }

}
