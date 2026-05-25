package fi.celssi.chatdm.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * In-memory JournalStorage for unit tests.
 */
public class InMemoryJournalStorage implements JournalStorage {

    private final Map<String, String> files = new HashMap<>();

    private static String key(String subDir, String fileName) {
        return subDir + "/" + fileName;
    }

    @Override
    public String read(String subDir, String fileName) {
        return files.get(key(subDir, fileName));
    }

    @Override
    public void write(String subDir, String fileName, String content) {
        files.put(key(subDir, fileName), content);
    }

    @Override
    public boolean exists(String subDir, String fileName) {
        return files.containsKey(key(subDir, fileName));
    }

    @Override
    public List<String> list(String subDir) throws IOException {
        String prefix = subDir + "/";
        Set<String> names = new HashSet<>();
        for (String k : files.keySet()) {
            if (k.startsWith(prefix)) {
                String rest = k.substring(prefix.length());
                if (!rest.contains("/")) {
                    names.add(rest);
                }
            }
        }
        return new ArrayList<>(names);
    }

    @Override
    public List<String> listSubdirs(String subDir) throws IOException {
        String prefix = subDir + "/";
        Set<String> subdirs = new HashSet<>();
        for (String k : files.keySet()) {
            if (k.startsWith(prefix)) {
                String rest = k.substring(prefix.length());
                int slash = rest.indexOf('/');
                if (slash > 0) {
                    subdirs.add(rest.substring(0, slash));
                }
            }
        }
        return subdirs.stream().sorted().toList();
    }

    @Override
    public void append(String subDir, String fileName, String content) throws IOException {
        String existing = read(subDir, fileName);
        write(subDir, fileName, (existing != null ? existing : "") + content);
    }

    @Override
    public void delete(String subDir, String fileName) throws IOException {
        files.remove(key(subDir, fileName));
    }
}
