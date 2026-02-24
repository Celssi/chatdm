package fi.celssi.chatdm.storage;

import java.io.IOException;
import java.util.List;

/**
 * Abstraction for journal data storage (characters, adventures, NPCs, campaigns, locations).
 * Supports local filesystem and GCS backends.
 */
public interface JournalStorage {

    /**
     * Read file content.
     *
     * @param subDir  Subdirectory (e.g. "characters", "adventures")
     * @param fileName File name (e.g. "my_character.txt")
     * @return File content, or null if not found
     */
    String read(String subDir, String fileName) throws IOException;

    /**
     * Write file content.
     */
    void write(String subDir, String fileName, String content) throws IOException;

    /**
     * Check if file exists.
     */
    boolean exists(String subDir, String fileName) throws IOException;

    /**
     * List file names in a subdirectory (non-recursive).
     *
     * @param subDir Subdirectory to list
     * @return List of file names (not paths)
     */
    List<String> list(String subDir) throws IOException;

    /**
     * Append content to an existing file.
     */
    void append(String subDir, String fileName, String content) throws IOException;
}
