package fi.celssi.chatdm.ChatDM;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class JournalOracle {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Path charactersDir;
    private final Path adventuresDir;

    public JournalOracle() {
        // Use user home directory to avoid permission issues
        String userHome = System.getProperty("user.home");
        Path baseDir = Paths.get(userHome, ".chatdm", "journal");
        this.charactersDir = baseDir.resolve("characters");
        this.adventuresDir = baseDir.resolve("adventures");
    }

    @PostConstruct
    public void init() throws IOException {
        // Create directories if they don't exist
        Files.createDirectories(charactersDir);
        Files.createDirectories(adventuresDir);
    }

    @Tool(name = "ChatDM_save_character", description = """
            Save a character to a text file for reuse across adventures.
            Parameters:
            - characterName: Required. The name of the character (will be used as filename)
            - gameSystem: Required. The game system (e.g., 'brambletrek')
            - characterData: Required. Character information (stats, description, etc.)

            The character will be saved as a .txt file in the characters directory.
            """)
    public String saveCharacter(String characterName, String gameSystem, String characterData) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return "Error: Character name is required";
        }
        if (gameSystem == null || gameSystem.trim().isEmpty()) {
            return "Error: Game system is required";
        }
        if (characterData == null || characterData.trim().isEmpty()) {
            return "Error: Character data is required";
        }

        try {
            String sanitizedName = sanitizeFilename(characterName);
            Path characterPath = charactersDir.resolve(sanitizedName + ".txt");

            String content = String.format("""
                    CHARACTER: %s
                    GAME SYSTEM: %s
                    CREATED: %s

                    %s
                    """, characterName, gameSystem, LocalDateTime.now().format(DATE_FORMAT), characterData);

            Files.writeString(characterPath, content);
            return String.format("Character '%s' saved successfully to %s", characterName, characterPath);
        } catch (IOException e) {
            return "Error saving character: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_load_character", description = """
            Load a previously saved character from file.
            Parameters:
            - characterName: Required. The name of the character to load

            Returns the character data from the file.
            """)
    public String loadCharacter(String characterName) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return "Error: Character name is required";
        }

        try {
            String sanitizedName = sanitizeFilename(characterName);
            Path characterPath = charactersDir.resolve(sanitizedName + ".txt");

            if (!Files.exists(characterPath)) {
                return String.format("Error: Character '%s' not found. Use ChatDM_list_characters to see available characters.", characterName);
            }

            return Files.readString(characterPath);
        } catch (IOException e) {
            return "Error loading character: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_characters", description = """
            List all saved characters.
            Returns a list of all character names and their game systems.
            """)
    public String listCharacters() {
        try {
            Path charactersPath = charactersDir;

            if (!Files.exists(charactersPath)) {
                return "No characters directory found.";
            }

            try (Stream<Path> paths = Files.list(charactersPath)) {
                List<String> characters = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".txt"))
                        .map(path -> {
                            try {
                                String content = Files.readString(path);
                                String name = extractValue(content, "CHARACTER:");
                                String gameSystem = extractValue(content, "GAME SYSTEM:");
                                return String.format("  - %s [%s]", name, gameSystem);
                            } catch (IOException e) {
                                return "  - " + path.getFileName().toString() + " [error reading]";
                            }
                        })
                        .collect(Collectors.toList());

                if (characters.isEmpty()) {
                    return "No characters saved yet.";
                }

                return "Saved Characters:\n" + String.join("\n", characters);
            }
        } catch (IOException e) {
            return "Error listing characters: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_start_adventure", description = """
            Start a new adventure journal.
            Parameters:
            - adventureName: Required. Name of the adventure
            - gameSystem: Required. The game system being used
            - characters: Optional. Comma-separated list of character names participating
            - description: Optional. Brief description of the adventure

            Creates a new markdown file for the adventure log.
            """)
    public String startAdventure(String adventureName, String gameSystem, String characters, String description) {
        if (adventureName == null || adventureName.trim().isEmpty()) {
            return "Error: Adventure name is required";
        }
        if (gameSystem == null || gameSystem.trim().isEmpty()) {
            return "Error: Game system is required";
        }

        try {
            String sanitizedName = sanitizeFilename(adventureName);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path adventurePath = adventuresDir.resolve(timestamp + "_" + sanitizedName + ".md");

            StringBuilder content = new StringBuilder();
            content.append("# ").append(adventureName).append("\n\n");
            content.append("**Game System:** ").append(gameSystem).append("\n\n");
            content.append("**Started:** ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n\n");

            if (characters != null && !characters.trim().isEmpty()) {
                content.append("**Characters:** ").append(characters).append("\n\n");
            }

            if (description != null && !description.trim().isEmpty()) {
                content.append("## Description\n\n");
                content.append(description).append("\n\n");
            }

            content.append("## Adventure Log\n\n");

            Files.writeString(adventurePath, content.toString());
            return String.format("Adventure '%s' started. Log file: %s", adventureName, adventurePath);
        } catch (IOException e) {
            return "Error starting adventure: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_log_event", description = """
            Log an event to the current adventure journal.
            Parameters:
            - adventureName: Required. Name of the adventure (must match the adventure started)
            - event: Required. Description of what happened

            Appends the event with a timestamp to the adventure log.
            """)
    public String logEvent(String adventureName, String event) {
        if (adventureName == null || adventureName.trim().isEmpty()) {
            return "Error: Adventure name is required";
        }
        if (event == null || event.trim().isEmpty()) {
            return "Error: Event description is required";
        }

        try {
            String sanitizedName = sanitizeFilename(adventureName);
            Path adventurePath = findLatestAdventure(sanitizedName);

            if (adventurePath == null) {
                return String.format("Error: No adventure found with name '%s'. Use ChatDM_start_adventure first.", adventureName);
            }

            String logEntry = String.format("### %s\n\n%s\n\n",
                    LocalDateTime.now().format(DATE_FORMAT), event);

            Files.writeString(adventurePath, logEntry, StandardOpenOption.APPEND);
            return String.format("Event logged to '%s'", adventureName);
        } catch (IOException e) {
            return "Error logging event: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_adventures", description = """
            List all adventure journals.
            Returns a list of all adventures with their start dates and game systems.
            """)
    public String listAdventures() {
        try {
            Path adventuresPath = adventuresDir;

            if (!Files.exists(adventuresPath)) {
                return "No adventures directory found.";
            }

            try (Stream<Path> paths = Files.list(adventuresPath)) {
                List<String> adventures = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".md"))
                        .sorted((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()))
                        .map(path -> {
                            try {
                                String content = Files.readString(path);
                                String name = extractMarkdownTitle(content);
                                String gameSystem = extractValue(content, "**Game System:**");
                                String started = extractValue(content, "**Started:**");
                                return String.format("  - %s [%s] - %s", name, gameSystem, started);
                            } catch (IOException e) {
                                return "  - " + path.getFileName().toString() + " [error reading]";
                            }
                        })
                        .collect(Collectors.toList());

                if (adventures.isEmpty()) {
                    return "No adventures logged yet.";
                }

                return "Adventure Journals:\n" + String.join("\n", adventures);
            }
        } catch (IOException e) {
            return "Error listing adventures: " + e.getMessage();
        }
    }

    private Path findLatestAdventure(String sanitizedName) throws IOException {
        Path adventuresPath = adventuresDir;

        if (!Files.exists(adventuresPath)) {
            return null;
        }

        try (Stream<Path> paths = Files.list(adventuresPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith("_" + sanitizedName + ".md"))
                    .sorted((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()))
                    .findFirst()
                    .orElse(null);
        }
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_]", "_").toLowerCase();
    }

    private String extractValue(String content, String prefix) {
        int start = content.indexOf(prefix);
        if (start == -1) return "Unknown";

        start += prefix.length();
        int end = content.indexOf("\n", start);
        if (end == -1) end = content.length();

        return content.substring(start, end).trim();
    }

    private String extractMarkdownTitle(String content) {
        int start = content.indexOf("# ");
        if (start == -1) return "Untitled";

        start += 2;
        int end = content.indexOf("\n", start);
        if (end == -1) end = content.length();

        return content.substring(start, end).trim();
    }
}
