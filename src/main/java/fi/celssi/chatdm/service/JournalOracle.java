package fi.celssi.chatdm.service;

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
    private final Path npcsDir;
    private final Path plotsDir;

    public JournalOracle() {
        // Use user home directory to avoid permission issues
        String userHome = System.getProperty("user.home");
        Path baseDir = Paths.get(userHome, ".chatdm", "journal");
        this.charactersDir = baseDir.resolve("characters");
        this.adventuresDir = baseDir.resolve("adventures");
        this.npcsDir = baseDir.resolve("npcs");
        this.plotsDir = baseDir.resolve("plots");
    }

    @PostConstruct
    public void init() throws IOException {
        // Create directories if they don't exist
        Files.createDirectories(charactersDir);
        Files.createDirectories(adventuresDir);
        Files.createDirectories(npcsDir);
        Files.createDirectories(plotsDir);
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

    @Tool(name = "ChatDM_read_adventure", description = """
            Read a single adventure journal by name.
            Parameters:
            - adventureName: Required. The name of the adventure to read

            Returns the complete adventure journal content including all logged events.
            """)
    public String readAdventure(String adventureName) {
        if (adventureName == null || adventureName.trim().isEmpty()) {
            return "Error: Adventure name is required";
        }

        try {
            String sanitizedName = sanitizeFilename(adventureName);
            Path adventurePath = findLatestAdventure(sanitizedName);

            if (adventurePath == null) {
                return String.format("Error: No adventure found with name '%s'. Use ChatDM_list_adventures to see available adventures.", adventureName);
            }

            return Files.readString(adventurePath);
        } catch (IOException e) {
            return "Error reading adventure: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_save_npc", description = """
            Save an NPC to the catalog for an adventure.
            
            WHEN TO USE: When introducing a new NPC that players might interact with again.
            
            Parameters:
            - adventureName: Required. The name of the adventure this NPC belongs to
            - npcName: Required. The name of the NPC
            - npcData: Required. Comprehensive NPC information including:
              * Physical Description: Age, appearance, clothing, distinctive features
              * Personality: Traits, quirks, speech patterns, mannerisms
              * Motivations: Goals, fears, desires, what drives them
              * Background: Occupation, history, relationships, secrets
              * Role in Story: How they fit into the adventure/campaign
              * Relationships: Connections to other NPCs, factions, locations
              * Current Status: Where they are, what they're doing, mood
            
            EXAMPLE npcData:
            "Elder Willow - Ancient tree spirit, appears as weathered oak with wise eyes.
            Speaks slowly with rustling leaves. Motivated by protecting the grove from corruption.
            Knows ancient secrets about the forest curse. Currently worried about dark magic spreading.
            Allies with the druid circle, enemies of the shadow cult."
            
            The NPC will be saved as a .txt file linked to the adventure.
            """)
    public String saveNpc(String adventureName, String npcName, String npcData) {
        if (adventureName == null || adventureName.trim().isEmpty()) {
            return "Error: Adventure name is required";
        }
        if (npcName == null || npcName.trim().isEmpty()) {
            return "Error: NPC name is required";
        }
        if (npcData == null || npcData.trim().isEmpty()) {
            return "Error: NPC data is required";
        }

        try {
            String sanitizedAdventure = sanitizeFilename(adventureName);
            String sanitizedNpc = sanitizeFilename(npcName);
            Path npcPath = npcsDir.resolve(sanitizedAdventure + "_" + sanitizedNpc + ".txt");

            String content = String.format("""
                    ADVENTURE: %s
                    NPC: %s
                    CREATED: %s
                    LAST_UPDATED: %s

                    %s
                    """, adventureName, npcName, LocalDateTime.now().format(DATE_FORMAT), 
                    LocalDateTime.now().format(DATE_FORMAT), npcData);

            Files.writeString(npcPath, content);
            return String.format("NPC '%s' saved for adventure '%s'", npcName, adventureName);
        } catch (IOException e) {
            return "Error saving NPC: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_load_npc", description = """
            Load an NPC from the catalog to maintain consistency.
            
            WHEN TO USE: Before NPC interactions to ensure consistent portrayal.
            
            Parameters:
            - adventureName: Required. The name of the adventure
            - npcName: Required. The name of the NPC to load

            Returns the complete NPC data from the file for reference during roleplay.
            """)
    public String loadNpc(String adventureName, String npcName) {
        if (adventureName == null || adventureName.trim().isEmpty()) {
            return "Error: Adventure name is required";
        }
        if (npcName == null || npcName.trim().isEmpty()) {
            return "Error: NPC name is required";
        }

        try {
            String sanitizedAdventure = sanitizeFilename(adventureName);
            String sanitizedNpc = sanitizeFilename(npcName);
            Path npcPath = npcsDir.resolve(sanitizedAdventure + "_" + sanitizedNpc + ".txt");

            if (!Files.exists(npcPath)) {
                return String.format("Error: NPC '%s' not found for adventure '%s'. Use ChatDM_list_npcs to see available NPCs.", npcName, adventureName);
            }

            return Files.readString(npcPath);
        } catch (IOException e) {
            return "Error loading NPC: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_update_npc", description = """
            Update an existing NPC in the catalog.
            
            WHEN TO USE: When NPC status changes significantly:
            - After major story events affecting the NPC
            - When relationships with PCs evolve
            - If motivations or allegiances shift
            - When NPC gains/loses important information
            - After combat or traumatic experiences
            
            Parameters:
            - adventureName: Required. The name of the adventure
            - npcName: Required. The name of the NPC to update
            - npcData: Required. Complete updated NPC information (not just changes)
            
            IMPORTANT: Provide the full updated NPC data, not just what changed.
            This ensures the NPC file remains complete and self-contained.
            
            Updates the NPC data and timestamp while preserving creation date.
            """)
    public String updateNpc(String adventureName, String npcName, String npcData) {
        if (adventureName == null || adventureName.trim().isEmpty()) {
            return "Error: Adventure name is required";
        }
        if (npcName == null || npcName.trim().isEmpty()) {
            return "Error: NPC name is required";
        }
        if (npcData == null || npcData.trim().isEmpty()) {
            return "Error: NPC data is required";
        }

        try {
            String sanitizedAdventure = sanitizeFilename(adventureName);
            String sanitizedNpc = sanitizeFilename(npcName);
            Path npcPath = npcsDir.resolve(sanitizedAdventure + "_" + sanitizedNpc + ".txt");

            if (!Files.exists(npcPath)) {
                return String.format("Error: NPC '%s' not found for adventure '%s'. Use ChatDM_save_npc to create a new NPC.", npcName, adventureName);
            }

            // Read existing content to preserve creation date
            String existingContent = Files.readString(npcPath);
            String createdDate = extractValue(existingContent, "CREATED:");

            String content = String.format("""
                    ADVENTURE: %s
                    NPC: %s
                    CREATED: %s
                    LAST_UPDATED: %s

                    %s
                    """, adventureName, npcName, createdDate, 
                    LocalDateTime.now().format(DATE_FORMAT), npcData);

            Files.writeString(npcPath, content);
            return String.format("NPC '%s' updated for adventure '%s'", npcName, adventureName);
        } catch (IOException e) {
            return "Error updating NPC: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_npcs", description = """
            List all NPCs for a specific adventure or all NPCs.
            
            WHEN TO USE: 
            - At session start to review available NPCs
            - When planning encounters or interactions
            - To check which NPCs exist before creating new ones
            
            Parameters:
            - adventureName: Optional. If provided, lists NPCs for that adventure only

            Returns a list of NPCs with their creation dates and adventure associations.
            """)
    public String listNpcs(String adventureName) {
        try {
            Path npcsPath = npcsDir;

            if (!Files.exists(npcsPath)) {
                return "No NPCs directory found.";
            }

            try (Stream<Path> paths = Files.list(npcsPath)) {
                List<String> npcs = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".txt"))
                        .map(path -> {
                            try {
                                String content = Files.readString(path);
                                String adventure = extractValue(content, "ADVENTURE:");
                                String npcName = extractValue(content, "NPC:");
                                String created = extractValue(content, "CREATED:");
                                
                                // Filter by adventure if specified
                                if (adventureName != null && !adventureName.trim().isEmpty()) {
                                    String sanitizedAdventure = sanitizeFilename(adventureName);
                                    String sanitizedFileAdventure = sanitizeFilename(adventure);
                                    if (!sanitizedFileAdventure.equals(sanitizedAdventure)) {
                                        return null;
                                    }
                                }
                                
                                return String.format("  - %s [%s] - Created: %s", npcName, adventure, created);
                            } catch (IOException e) {
                                return "  - " + path.getFileName().toString() + " [error reading]";
                            }
                        })
                        .filter(npc -> npc != null)
                        .sorted()
                        .collect(Collectors.toList());

                if (npcs.isEmpty()) {
                    if (adventureName != null && !adventureName.trim().isEmpty()) {
                        return String.format("No NPCs found for adventure '%s'.", adventureName);
                    } else {
                        return "No NPCs saved yet.";
                    }
                }

                String header = adventureName != null && !adventureName.trim().isEmpty() 
                    ? String.format("NPCs for Adventure '%s':\n", adventureName)
                    : "All Saved NPCs:\n";
                
                return header + String.join("\n", npcs);
            }
        } catch (IOException e) {
            return "Error listing NPCs: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_save_plot", description = """
            Save or update the plot journal for an adventure.
            
            WHEN TO USE: 
            - At adventure start to establish overarching story
            - After major plot developments or revelations
            - When story threads need to be connected
            - Between sessions to maintain narrative continuity
            - When introducing new story elements or complications
            
            Parameters:
            - adventureName: Required. The name of the adventure
            - plotData: Required. Comprehensive plot information including:
              * Main Story Arc: Primary conflict, goals, stakes
              * Subplots: Secondary storylines and character arcs
              * Themes: Central themes and motifs
              * Timeline: Key events, past and future
              * Factions: Groups, their goals and relationships
              * Secrets: Hidden information, mysteries, revelations
              * Complications: Obstacles, twists, unexpected elements
              * Future Hooks: Potential story directions, unresolved threads
            
            EXAMPLE plotData:
            "The Mystic Forest Curse: Ancient evil spreading through the grove.
            Main Arc: Stop the corruption before it reaches the village.
            Subplots: Druid circle's internal conflict, lost temple discovery.
            Themes: Nature vs corruption, sacrifice for protection.
            Timeline: Curse started 100 years ago, accelerating recently.
            Factions: Druids (protectors), Shadow Cult (corruptors), Village (victims).
            Secrets: The curse is actually a trapped nature spirit.
            Complications: PCs' actions may accelerate the curse.
            Future Hooks: Other cursed locations, spirit's true nature."
            
            Creates or updates the plot file for the adventure.
            """)
    public String savePlot(String adventureName, String plotData) {
        if (adventureName == null || adventureName.trim().isEmpty()) {
            return "Error: Adventure name is required";
        }
        if (plotData == null || plotData.trim().isEmpty()) {
            return "Error: Plot data is required";
        }

        try {
            String sanitizedAdventure = sanitizeFilename(adventureName);
            Path plotPath = plotsDir.resolve(sanitizedAdventure + "_plot.txt");

            boolean isUpdate = Files.exists(plotPath);
            String createdDate = isUpdate ? extractValue(Files.readString(plotPath), "CREATED:") : LocalDateTime.now().format(DATE_FORMAT);

            String content = String.format("""
                    ADVENTURE: %s
                    CREATED: %s
                    LAST_UPDATED: %s

                    %s
                    """, adventureName, createdDate, LocalDateTime.now().format(DATE_FORMAT), plotData);

            Files.writeString(plotPath, content);
            return String.format("Plot %s for adventure '%s'", isUpdate ? "updated" : "saved", adventureName);
        } catch (IOException e) {
            return "Error saving plot: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_load_plot", description = """
            Load the plot journal for an adventure.
            
            WHEN TO USE:
            - At session start to review story context
            - When making story decisions or introducing plot elements
            - To maintain consistency with established lore
            - When connecting new events to existing storylines
            
            Parameters:
            - adventureName: Required. The name of the adventure

            Returns the complete plot data from the file for story reference.
            """)
    public String loadPlot(String adventureName) {
        if (adventureName == null || adventureName.trim().isEmpty()) {
            return "Error: Adventure name is required";
        }

        try {
            String sanitizedAdventure = sanitizeFilename(adventureName);
            Path plotPath = plotsDir.resolve(sanitizedAdventure + "_plot.txt");

            if (!Files.exists(plotPath)) {
                return String.format("Error: No plot found for adventure '%s'. Use ChatDM_save_plot to create one.", adventureName);
            }

            return Files.readString(plotPath);
        } catch (IOException e) {
            return "Error loading plot: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_plots", description = """
            List all plot journals.
            
            WHEN TO USE:
            - To review all ongoing storylines
            - When planning cross-adventure connections
            - To check which adventures have established plots
            - During campaign planning sessions
            
            Returns a list of all adventures with plot journals and their last update dates.
            """)
    public String listPlots() {
        try {
            Path plotsPath = plotsDir;

            if (!Files.exists(plotsPath)) {
                return "No plots directory found.";
            }

            try (Stream<Path> paths = Files.list(plotsPath)) {
                List<String> plots = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith("_plot.txt"))
                        .map(path -> {
                            try {
                                String content = Files.readString(path);
                                String adventure = extractValue(content, "ADVENTURE:");
                                String lastUpdated = extractValue(content, "LAST_UPDATED:");
                                return String.format("  - %s - Last Updated: %s", adventure, lastUpdated);
                            } catch (IOException e) {
                                return "  - " + path.getFileName().toString() + " [error reading]";
                            }
                        })
                        .sorted()
                        .collect(Collectors.toList());

                if (plots.isEmpty()) {
                    return "No plot journals saved yet.";
                }

                return "Plot Journals:\n" + String.join("\n", plots);
            }
        } catch (IOException e) {
            return "Error listing plots: " + e.getMessage();
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
