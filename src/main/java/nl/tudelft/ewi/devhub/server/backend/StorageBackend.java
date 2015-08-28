package nl.tudelft.ewi.devhub.server.backend;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.Config;

import com.google.inject.Inject;

import org.apache.commons.io.FileUtils;

import javax.ws.rs.NotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * The {@code StorageBackend} is used for uploading to and downloading from Devhub
 *
 * @author Jan-Willem Gmelig Meyling
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
        log.info("Created file {}/{}", path, fileName);
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
            log.info("Deleted file {}/{}", path, fileName);
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

    /**
     * Get a file in the StorageBackend
     *  <strong>
     *      REMEMBER THAT THROUGH THIS METHOD YOU CAN RETRIEVE
     *      EVERY FILE IN THE STATIC STORAGE. BE SURE TO CHECK
     *      IF THE USER IS ALLOWED TO VIEW THE PARTICULAR FILE.
     *  </strong>
     * @param attachmentPath relative path of file
     * @return the file
     * @throws NotFoundException if the file could not be found
     */
    public File getFile(String attachmentPath) throws NotFoundException {
        File file = new File(rootFolder, attachmentPath);
        if(!file.exists()) {
            throw new NotFoundException();
        }
        return file;
    }

}
