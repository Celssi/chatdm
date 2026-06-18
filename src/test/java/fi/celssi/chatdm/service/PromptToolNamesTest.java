package fi.celssi.chatdm.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ensures game prompt texts reference only registered MCP tool names.
 */
class PromptToolNamesTest {

    private static final Pattern CHATDM_TOOL = Pattern.compile("ChatDM_[a-z0-9_]+");
    private static final Set<String> FORBIDDEN = Set.of(
            "journal_load_character",
            "journal_load_adventure",
            "journal_create",
            "ChatDM_draw_cards",
            "ChatDM_list_characters_by_campaign",
            "ChatDM_list_npcs_by_campaign",
            "ChatDM_list_locations_by_campaign"
    );

    @Test
    void promptTextsReferenceOnlyRegisteredTools() throws IOException {
        Set<String> registeredTools = collectRegisteredToolNames();
        Set<String> unknown = new HashSet<>();
        Set<String> forbiddenFound = new HashSet<>();

        Path serviceDir = Paths.get("src/main/java/fi/celssi/chatdm/service");
        try (Stream<Path> files = Files.walk(serviceDir)) {
            files.filter(p -> p.getFileName().toString().endsWith("PromptsOracle.java"))
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);
                            for (String bad : FORBIDDEN) {
                                if (content.contains(bad)) {
                                    forbiddenFound.add(bad + " in " + path.getFileName());
                                }
                            }
                            Matcher m = CHATDM_TOOL.matcher(content);
                            while (m.find()) {
                                String tool = m.group();
                                if (!registeredTools.contains(tool)) {
                                    unknown.add(tool + " (in " + path.getFileName() + ")");
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        assertTrue(forbiddenFound.isEmpty(),
                "Forbidden legacy tool names found: " + forbiddenFound);
        assertTrue(unknown.isEmpty(),
                "Unknown ChatDM tool names in prompts: " + unknown);
    }

    private Set<String> collectRegisteredToolNames() throws IOException {
        Set<String> names = new HashSet<>();
        Path root = Paths.get("src/main/java");
        Pattern p = Pattern.compile("@Tool\\(name\\s*=\\s*\"([^\"]+)\"");
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(f -> f.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            Matcher m = p.matcher(Files.readString(path));
                            while (m.find()) {
                                names.add(m.group(1));
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        return names;
    }

    private String pathToClassName(Path path) {
        String s = path.toString().replace('\\', '/');
        int idx = s.indexOf("src/main/java/");
        String rel = s.substring(idx + "src/main/java/".length());
        return rel.replace('/', '.').replace(".java", "");
    }
}
