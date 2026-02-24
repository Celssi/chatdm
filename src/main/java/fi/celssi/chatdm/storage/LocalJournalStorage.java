package fi.celssi.chatdm.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

@Component
@Profile("!cloud & !gcs")
public class LocalJournalStorage implements JournalStorage {

    private final Path baseDir;

    public LocalJournalStorage(@Value("${chatdm.journal.base-path:}") String basePath) {
        if (basePath != null && !basePath.isEmpty() && !basePath.startsWith("gs://")) {
            this.baseDir = Paths.get(basePath);
        } else {
            this.baseDir = Paths.get(System.getProperty("user.home"), ".chatdm", "journal");
        }
    }

    @PostConstruct
    public void init() throws IOException {
        for (String sub : List.of("characters", "adventures", "npcs", "campaigns", "locations", "books")) {
            Files.createDirectories(baseDir.resolve(sub));
        }
    }

    @Override
    public String read(String subDir, String fileName) throws IOException {
        Path path = baseDir.resolve(subDir).resolve(fileName);
        if (!Files.exists(path)) {
            return null;
        }
        return Files.readString(path);
    }

    @Override
    public void write(String subDir, String fileName, String content) throws IOException {
        Path path = baseDir.resolve(subDir).resolve(fileName);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    @Override
    public boolean exists(String subDir, String fileName) throws IOException {
        return Files.exists(baseDir.resolve(subDir).resolve(fileName));
    }

    @Override
    public List<String> list(String subDir) throws IOException {
        Path dir = baseDir.resolve(subDir);
        if (!Files.exists(dir)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.list(dir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .toList();
        }
    }

    @Override
    public void append(String subDir, String fileName, String content) throws IOException {
        Path path = baseDir.resolve(subDir).resolve(fileName);
        Files.writeString(path, content, StandardOpenOption.APPEND);
    }

    @Override
    public void delete(String subDir, String fileName) throws IOException {
        Path path = baseDir.resolve(subDir).resolve(fileName);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }
}
