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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class JournalOracle {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Path charactersDir;
    private final Path adventuresDir;
    private final Path npcsDir;
    private final Path campaignsDir;
    private final Path locationsDir;

    public JournalOracle() {
        // Use the user home directory to avoid permission issues
        String userHome = System.getProperty("user.home");
        Path baseDir = Paths.get(userHome, ".chatdm", "journal");
        this.charactersDir = baseDir.resolve("characters");
        this.adventuresDir = baseDir.resolve("adventures");
        this.npcsDir = baseDir.resolve("npcs");
        this.campaignsDir = baseDir.resolve("campaigns");
        this.locationsDir = baseDir.resolve("locations");
    }

    @PostConstruct
    public void init() throws IOException {
        // Create directories if they don't exist
        Files.createDirectories(charactersDir);
        Files.createDirectories(adventuresDir);
        Files.createDirectories(npcsDir);
        Files.createDirectories(campaignsDir);
        Files.createDirectories(locationsDir);
    }

    @Tool(name = "ChatDM_save_character", description = """
            Save a character to a text file for reuse across adventures.
            Parameters:
            - characterName: Required. The name of the character (will be used as filename)
            - gameSystem: Required. The game system (e.g., 'brambletrek')
            - characterData: Required. Character information (stats, description, etc.)
            - campaignName: Optional. The name of the campaign this character belongs to
            
            The character will be saved as a .txt file in the characters directory.
            Characters are bound to campaigns, allowing them to be used across multiple adventures within the same campaign.
            """)
    public String saveCharacter(String characterName, String gameSystem, String characterData, String campaignName) {
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

            // Check if character exists to preserve creation date
            boolean isUpdate = Files.exists(characterPath);
            String createdDate = isUpdate ? extractValue(Files.readString(characterPath), "CREATED:") : LocalDateTime.now().format(DATE_FORMAT);

            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append(String.format("""
                    CHARACTER: %s
                    GAME SYSTEM: %s
                    CREATED: %s
                    """, characterName, gameSystem, createdDate));

            if (campaignName != null && !campaignName.trim().isEmpty()) {
                contentBuilder.append(String.format("CAMPAIGN: %s\n", campaignName));
            }

            contentBuilder.append(String.format("""
                    
                    %s
                    """, characterData));

            Files.writeString(characterPath, contentBuilder.toString());
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
            List all saved characters, optionally filtered by campaign.
            Parameters:
            - campaignName: Optional. If provided, lists characters for that campaign only
            
            Returns a list of all character names, their game systems, and campaign associations.
            """)
    public String listCharacters(String campaignName) {
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
                                String campaign = extractValue(content, "CAMPAIGN:");

                                // Filter by campaign if specified
                                if (campaignName != null && !campaignName.trim().isEmpty()) {
                                    String sanitizedCampaign = sanitizeFilename(campaignName);
                                    String sanitizedFileCampaign = campaign.equals("Unknown") ? null : sanitizeFilename(campaign);
                                    if (sanitizedFileCampaign == null || !sanitizedFileCampaign.equals(sanitizedCampaign)) {
                                        return null;
                                    }
                                }

                                String campaignInfo = campaign.equals("Unknown") ? "" : " [Campaign: " + campaign + "]";
                                return String.format("  - %s [%s]%s", name, gameSystem, campaignInfo);
                            } catch (IOException e) {
                                return "  - " + path.getFileName().toString() + " [error reading]";
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (characters.isEmpty()) {
                    if (campaignName != null && !campaignName.trim().isEmpty()) {
                        return String.format("No characters found for campaign '%s'.", campaignName);
                    } else {
                        return "No characters saved yet.";
                    }
                }

                String header = campaignName != null && !campaignName.trim().isEmpty()
                        ? String.format("Characters for Campaign '%s':\n", campaignName)
                        : "Saved Characters:\n";

                return header + String.join("\n", characters);
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

    @Tool(name = "ChatDM_list_adventures_by_campaign", description = """
            List all adventure journals that belong to a specific campaign.
            Parameters:
            - campaignName: Required. The name of the campaign to filter by
            
            Returns a list of all adventures that have the specified campaign name in their # Campaign: comment.
            """)
    public String listAdventuresByCampaign(String campaignName) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }

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
                                String adventureCampaign = extractCampaignName(content);

                                // Filter by campaign name (case-insensitive comparison)
                                if (adventureCampaign == null || !adventureCampaign.equalsIgnoreCase(campaignName.trim())) {
                                    return null;
                                }

                                String name = extractMarkdownTitle(content);
                                String gameSystem = extractValue(content, "**Game System:**");
                                String started = extractValue(content, "**Started:**");
                                return String.format("  - %s [%s] - %s", name, gameSystem, started);
                            } catch (IOException e) {
                                return null; // Skip files with errors
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (adventures.isEmpty()) {
                    return String.format("No adventures found for campaign '%s'.", campaignName);
                }

                return String.format("Adventures for Campaign '%s':\n%s", campaignName, String.join("\n", adventures));
            }
        } catch (IOException e) {
            return "Error listing adventures by campaign: " + e.getMessage();
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
            Save an NPC to the catalog for a campaign.
            
            WHEN TO USE: When introducing a new NPC that players might interact with again.
            
            Parameters:
            - campaignName: Required. The name of the campaign this NPC belongs to
            - npcName: Required. The name of the NPC
            - npcData: Required. Comprehensive NPC information including:
              * Physical Description: Age, appearance, clothing, distinctive features
              * Personality: Traits, quirks, speech patterns, mannerisms
              * Motivations: Goals, fears, desires, what drives them
              * Background: Occupation, history, relationships, secrets
              * Role in Story: How they fit into the campaign
              * Relationships: Connections to other NPCs, factions, locations
              * Current Status: Where they are, what they're doing, mood
            
            EXAMPLE npcData:
            "Elder Willow - Ancient tree spirit, appears as weathered oak with wise eyes.
            Speaks slowly with rustling leaves. Motivated by protecting the grove from corruption.
            Knows ancient secrets about the forest curse. Currently worried about dark magic spreading.
            Allies with the druid circle, enemies of the shadow cult."
            
            The NPC will be saved as a .txt file linked to the campaign.
            NPCs are bound to campaigns, allowing them to appear across multiple adventures within the same campaign.
            """)
    public String saveNpc(String campaignName, String npcName, String npcData) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }
        if (npcName == null || npcName.trim().isEmpty()) {
            return "Error: NPC name is required";
        }
        if (npcData == null || npcData.trim().isEmpty()) {
            return "Error: NPC data is required";
        }

        try {
            String sanitizedCampaign = sanitizeFilename(campaignName);
            String sanitizedNpc = sanitizeFilename(npcName);
            Path npcPath = npcsDir.resolve(sanitizedCampaign + "_" + sanitizedNpc + ".txt");

            String content = String.format("""
                            CAMPAIGN: %s
                            NPC: %s
                            CREATED: %s
                            LAST_UPDATED: %s
                            
                            %s
                            """, campaignName, npcName, LocalDateTime.now().format(DATE_FORMAT),
                    LocalDateTime.now().format(DATE_FORMAT), npcData);

            Files.writeString(npcPath, content);
            return String.format("NPC '%s' saved for campaign '%s'", npcName, campaignName);
        } catch (IOException e) {
            return "Error saving NPC: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_load_npc", description = """
            Load an NPC from the catalog to maintain consistency.
            
            WHEN TO USE: Before NPC interactions to ensure consistent portrayal.
            
            Parameters:
            - campaignName: Required. The name of the campaign
            - npcName: Required. The name of the NPC to load
            
            Returns the complete NPC data from the file for reference during roleplay.
            """)
    public String loadNpc(String campaignName, String npcName) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }
        if (npcName == null || npcName.trim().isEmpty()) {
            return "Error: NPC name is required";
        }

        try {
            String sanitizedCampaign = sanitizeFilename(campaignName);
            String sanitizedNpc = sanitizeFilename(npcName);
            Path npcPath = npcsDir.resolve(sanitizedCampaign + "_" + sanitizedNpc + ".txt");

            if (!Files.exists(npcPath)) {
                return String.format("Error: NPC '%s' not found for campaign '%s'. Use ChatDM_list_npcs to see available NPCs.", npcName, campaignName);
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
            - campaignName: Required. The name of the campaign
            - npcName: Required. The name of the NPC to update
            - npcData: Required. Complete updated NPC information (not just changes)
            
            IMPORTANT: Provide the full updated NPC data, not just what changed.
            This ensures the NPC file remains complete and self-contained.
            
            Updates the NPC data and timestamp while preserving creation date.
            """)
    public String updateNpc(String campaignName, String npcName, String npcData) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }
        if (npcName == null || npcName.trim().isEmpty()) {
            return "Error: NPC name is required";
        }
        if (npcData == null || npcData.trim().isEmpty()) {
            return "Error: NPC data is required";
        }

        try {
            String sanitizedCampaign = sanitizeFilename(campaignName);
            String sanitizedNpc = sanitizeFilename(npcName);
            Path npcPath = npcsDir.resolve(sanitizedCampaign + "_" + sanitizedNpc + ".txt");

            if (!Files.exists(npcPath)) {
                return String.format("Error: NPC '%s' not found for campaign '%s'. Use ChatDM_save_npc to create a new NPC.", npcName, campaignName);
            }

            // Read existing content to preserve creation date
            String existingContent = Files.readString(npcPath);
            String createdDate = extractValue(existingContent, "CREATED:");
            String campaign = extractValue(existingContent, "CAMPAIGN:");

            // Use provided campaignName if we couldn't extract it
            if (campaign.equals("Unknown")) {
                campaign = campaignName;
            }

            String content = String.format("""
                            CAMPAIGN: %s
                            NPC: %s
                            CREATED: %s
                            LAST_UPDATED: %s
                            
                            %s
                            """, campaign, npcName, createdDate,
                    LocalDateTime.now().format(DATE_FORMAT), npcData);

            Files.writeString(npcPath, content);
            return String.format("NPC '%s' updated for campaign '%s'", npcName, campaign);
        } catch (IOException e) {
            return "Error updating NPC: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_npcs", description = """
            List all NPCs for a specific campaign or all NPCs.
            
            WHEN TO USE: 
            - At session start to review available NPCs
            - When planning encounters or interactions
            - To check which NPCs exist before creating new ones
            
            Parameters:
            - campaignName: Optional. If provided, lists NPCs for that campaign only
            
            Returns a list of NPCs with their creation dates and campaign associations.
            """)
    public String listNpcs(String campaignName) {
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
                                String campaign = extractValue(content, "CAMPAIGN:");
                                String npcName = extractValue(content, "NPC:");
                                String created = extractValue(content, "CREATED:");

                                // Filter by campaign if specified
                                if (campaignName != null && !campaignName.trim().isEmpty()) {
                                    String sanitizedCampaign = sanitizeFilename(campaignName);
                                    String sanitizedFileCampaign = campaign.equals("Unknown") ? null : sanitizeFilename(campaign);
                                    if (sanitizedFileCampaign == null || !sanitizedFileCampaign.equals(sanitizedCampaign)) {
                                        return null;
                                    }
                                }

                                return String.format("  - %s [%s] - Created: %s", npcName, campaign, created);
                            } catch (IOException e) {
                                return "  - " + path.getFileName().toString() + " [error reading]";
                            }
                        })
                        .filter(Objects::nonNull)
                        .sorted()
                        .collect(Collectors.toList());

                if (npcs.isEmpty()) {
                    if (campaignName != null && !campaignName.trim().isEmpty()) {
                        return String.format("No NPCs found for campaign '%s'.", campaignName);
                    } else {
                        return "No NPCs saved yet.";
                    }
                }

                String header = campaignName != null && !campaignName.trim().isEmpty()
                        ? String.format("NPCs for Campaign '%s':\n", campaignName)
                        : "All Saved NPCs:\n";

                return header + String.join("\n", npcs);
            }
        } catch (IOException e) {
            return "Error listing NPCs: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_npcs_by_campaign", description = """
            List all NPCs that belong to a specific campaign.
            
            WHEN TO USE:
            - To see which NPCs are part of a campaign
            - When planning NPC interactions across adventures
            - To review the NPC roster for a campaign
            
            Parameters:
            - campaignName: Required. The name of the campaign to filter by
            
            Returns a list of all NPCs that have the specified campaign name.
            """)
    public String listNpcsByCampaign(String campaignName) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }

        return listNpcs(campaignName);
    }

    @Tool(name = "ChatDM_save_location", description = """
            Save a location to the catalog for a campaign.
            
            WHEN TO USE: When introducing a new location that players might visit again.
            
            Parameters:
            - campaignName: Required. The name of the campaign this location belongs to
            - locationName: Required. The name of the location
            - locationData: Required. Comprehensive location information including:
              * Physical Description: Architecture, size, layout, notable features
              * Atmosphere: Mood, lighting, sounds, smells, temperature
              * History: Past events, significance, how it came to be
              * Current State: Condition, maintenance, recent changes
              * Notable Features: Unique elements, secrets, points of interest
              * Inhabitants: Who lives/works here, NPCs associated with location
              * Connections: Nearby locations, travel routes, relationships to other places
              * Secrets: Hidden areas, concealed information, mysteries
            
            EXAMPLE locationData:
            "Thornwatch Tower - Abandoned stone tower on hill overlooking Pinehaven.
            Three stories tall, weathered stone with ivy covering lower walls. Dark windows suggest abandonment.
            Built 200 years ago as watchtower, fell into disuse 50 years ago. Now occupied by dark wizard.
            Lower floor: entrance hall with collapsed ceiling. Upper floors: wizard's study and ritual chamber.
            Basement contains ancient ritual chamber with domination magic inscriptions. Secret passage behind false wall.
            Atmosphere: Foreboding, cold, echoes of dark magic. Smells of sulfur and old stone."
            
            The location will be saved as a .txt file linked to the campaign.
            Locations are bound to campaigns, allowing them to appear across multiple adventures within the same campaign.
            """)
    public String saveLocation(String campaignName, String locationName, String locationData) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }
        if (locationName == null || locationName.trim().isEmpty()) {
            return "Error: Location name is required";
        }
        if (locationData == null || locationData.trim().isEmpty()) {
            return "Error: Location data is required";
        }

        try {
            String sanitizedCampaign = sanitizeFilename(campaignName);
            String sanitizedLocation = sanitizeFilename(locationName);
            Path locationPath = locationsDir.resolve(sanitizedCampaign + "_" + sanitizedLocation + ".txt");

            String content = String.format("""
                            CAMPAIGN: %s
                            LOCATION: %s
                            CREATED: %s
                            LAST_UPDATED: %s
                            
                            %s
                            """, campaignName, locationName, LocalDateTime.now().format(DATE_FORMAT),
                    LocalDateTime.now().format(DATE_FORMAT), locationData);

            Files.writeString(locationPath, content);
            return String.format("Location '%s' saved for campaign '%s'", locationName, campaignName);
        } catch (IOException e) {
            return "Error saving location: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_load_location", description = """
            Load a location from the catalog to maintain consistency.
            
            WHEN TO USE: Before describing or using a location to ensure consistent portrayal.
            
            Parameters:
            - campaignName: Required. The name of the campaign
            - locationName: Required. The name of the location to load
            
            Returns the complete location data from the file for reference during gameplay.
            """)
    public String loadLocation(String campaignName, String locationName) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }
        if (locationName == null || locationName.trim().isEmpty()) {
            return "Error: Location name is required";
        }

        try {
            String sanitizedCampaign = sanitizeFilename(campaignName);
            String sanitizedLocation = sanitizeFilename(locationName);
            Path locationPath = locationsDir.resolve(sanitizedCampaign + "_" + sanitizedLocation + ".txt");

            if (!Files.exists(locationPath)) {
                return String.format("Error: Location '%s' not found for campaign '%s'. Use ChatDM_list_locations to see available locations.", locationName, campaignName);
            }

            return Files.readString(locationPath);
        } catch (IOException e) {
            return "Error loading location: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_update_location", description = """
            Update an existing location in the catalog.
            
            WHEN TO USE: When location status changes significantly:
            - After major story events affecting the location
            - When location is damaged, repaired, or altered
            - If new features or secrets are discovered
            - When inhabitants change or relationships evolve
            - After exploration reveals new areas
            
            Parameters:
            - campaignName: Required. The name of the campaign
            - locationName: Required. The name of the location to update
            - locationData: Required. Complete updated location information (not just changes)
            
            IMPORTANT: Provide the full updated location data, not just what changed.
            This ensures the location file remains complete and self-contained.
            
            Updates the location data and timestamp while preserving creation date.
            """)
    public String updateLocation(String campaignName, String locationName, String locationData) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }
        if (locationName == null || locationName.trim().isEmpty()) {
            return "Error: Location name is required";
        }
        if (locationData == null || locationData.trim().isEmpty()) {
            return "Error: Location data is required";
        }

        try {
            String sanitizedCampaign = sanitizeFilename(campaignName);
            String sanitizedLocation = sanitizeFilename(locationName);
            Path locationPath = locationsDir.resolve(sanitizedCampaign + "_" + sanitizedLocation + ".txt");

            if (!Files.exists(locationPath)) {
                return String.format("Error: Location '%s' not found for campaign '%s'. Use ChatDM_save_location to create a new location.", locationName, campaignName);
            }

            // Read existing content to preserve creation date
            String existingContent = Files.readString(locationPath);
            String createdDate = extractValue(existingContent, "CREATED:");
            String campaign = extractValue(existingContent, "CAMPAIGN:");

            // Use provided campaignName if we couldn't extract it
            if (campaign.equals("Unknown")) {
                campaign = campaignName;
            }

            String content = String.format("""
                            CAMPAIGN: %s
                            LOCATION: %s
                            CREATED: %s
                            LAST_UPDATED: %s
                            
                            %s
                            """, campaign, locationName, createdDate,
                    LocalDateTime.now().format(DATE_FORMAT), locationData);

            Files.writeString(locationPath, content);
            return String.format("Location '%s' updated for campaign '%s'", locationName, campaign);
        } catch (IOException e) {
            return "Error updating location: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_locations", description = """
            List all locations for a specific campaign or all locations.
            
            WHEN TO USE: 
            - At session start to review available locations
            - When planning travel or exploration
            - To check which locations exist before creating new ones
            - When mapping out the campaign world
            
            Parameters:
            - campaignName: Optional. If provided, lists locations for that campaign only
            
            Returns a list of locations with their creation dates and campaign associations.
            """)
    public String listLocations(String campaignName) {
        try {
            Path locationsPath = locationsDir;

            if (!Files.exists(locationsPath)) {
                return "No locations directory found.";
            }

            try (Stream<Path> paths = Files.list(locationsPath)) {
                List<String> locations = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".txt"))
                        .map(path -> {
                            try {
                                String content = Files.readString(path);
                                String campaign = extractValue(content, "CAMPAIGN:");
                                String locationName = extractValue(content, "LOCATION:");
                                String created = extractValue(content, "CREATED:");

                                // Filter by campaign if specified
                                if (campaignName != null && !campaignName.trim().isEmpty()) {
                                    String sanitizedCampaign = sanitizeFilename(campaignName);
                                    String sanitizedFileCampaign = campaign.equals("Unknown") ? null : sanitizeFilename(campaign);
                                    if (sanitizedFileCampaign == null || !sanitizedFileCampaign.equals(sanitizedCampaign)) {
                                        return null;
                                    }
                                }

                                return String.format("  - %s [%s] - Created: %s", locationName, campaign, created);
                            } catch (IOException e) {
                                return "  - " + path.getFileName().toString() + " [error reading]";
                            }
                        })
                        .filter(Objects::nonNull)
                        .sorted()
                        .collect(Collectors.toList());

                if (locations.isEmpty()) {
                    if (campaignName != null && !campaignName.trim().isEmpty()) {
                        return String.format("No locations found for campaign '%s'.", campaignName);
                    } else {
                        return "No locations saved yet.";
                    }
                }

                String header = campaignName != null && !campaignName.trim().isEmpty()
                        ? String.format("Locations for Campaign '%s':\n", campaignName)
                        : "All Saved Locations:\n";

                return header + String.join("\n", locations);
            }
        } catch (IOException e) {
            return "Error listing locations: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_locations_by_campaign", description = """
            List all locations that belong to a specific campaign.
            
            WHEN TO USE:
            - To see which locations are part of a campaign
            - When planning travel routes or world-building
            - To review the location roster for a campaign
            - When creating a campaign map or travel guide
            
            Parameters:
            - campaignName: Required. The name of the campaign to filter by
            
            Returns a list of all locations that have the specified campaign name.
            """)
    public String listLocationsByCampaign(String campaignName) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }

        return listLocations(campaignName);
    }

    @Tool(name = "ChatDM_save_campaign", description = """
            Save or update the campaign journal for an adventure.
            
            WHEN TO USE:
            - At campaign start to establish overarching story
            - After major plot developments or revelations
            - When story threads need to be connected
            - Between sessions to maintain narrative continuity
            - When introducing new story elements or complications
            
            Parameters:
            - campaignName: Required. The name of the campaign
            - campaignData: Required. Comprehensive campaign information including:
              * Main Story Arc: Primary conflict, goals, stakes
              * Subplots: Secondary storylines and character arcs
              * Themes: Central themes and motifs
              * Timeline: Key events, past and future
              * Factions: Groups, their goals and relationships
              * Secrets: Hidden information, mysteries, revelations
              * Complications: Obstacles, twists, unexpected elements
              * Future Hooks: Potential story directions, unresolved threads
            
            EXAMPLE campaignData:
            "The Mystic Forest Curse: Ancient evil spreading through the grove.
            Main Arc: Stop the corruption before it reaches the village.
            Subplots: Druid circle's internal conflict, lost temple discovery.
            Themes: Nature vs corruption, sacrifice for protection.
            Timeline: Curse started 100 years ago, accelerating recently.
            Factions: Druids (protectors), Shadow Cult (corruptors), Village (victims).
            Secrets: The curse is actually a trapped nature spirit.
            Complications: PCs' actions may accelerate the curse.
            Future Hooks: Other cursed locations, spirit's true nature."
            
            Creates or updates the campaign file.
            """)
    public String saveCampaign(String campaignName, String campaignData) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }
        if (campaignData == null || campaignData.trim().isEmpty()) {
            return "Error: Campaign data is required";
        }

        try {
            String sanitizedCampaign = sanitizeFilename(campaignName);
            Path campaignPath = campaignsDir.resolve(sanitizedCampaign + "_campaign.txt");

            boolean isUpdate = Files.exists(campaignPath);
            String createdDate = isUpdate ? extractValue(Files.readString(campaignPath), "CREATED:") : LocalDateTime.now().format(DATE_FORMAT);

            String content = String.format("""
                    NAME: %s
                    CREATED: %s
                    LAST_UPDATED: %s
                    
                    %s
                    """, campaignName, createdDate, LocalDateTime.now().format(DATE_FORMAT), campaignData);

            Files.writeString(campaignPath, content);
            return String.format("Campaign %s %s", isUpdate ? "updated" : "saved", campaignName);
        } catch (IOException e) {
            return "Error saving campaign: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_load_campaign", description = """
            Load the campaign journal.
            
            WHEN TO USE:
            - At session start to review story context
            - When making story decisions or introducing plot elements
            - To maintain consistency with established lore
            - When connecting new events to existing storylines
            
            Parameters:
            - campaignName: Required. The name of the campaign
            
            Returns the complete campaign data from the file for story reference.
            """)
    public String loadCampaign(String campaignName) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Adventure name is required";
        }

        try {
            String sanitizedCampaign = sanitizeFilename(campaignName);
            Path campaignPath = campaignsDir.resolve(sanitizedCampaign + "_campaign.txt");

            if (!Files.exists(campaignPath)) {
                return String.format("Error: No campaign %s found. Use ChatDM_save_campaign to create one.", campaignName);
            }

            return Files.readString(campaignPath);
        } catch (IOException e) {
            return "Error loading campaign: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_characters_by_campaign", description = """
            List all characters that belong to a specific campaign.
            
            WHEN TO USE:
            - To see which characters are part of a campaign
            - When planning character interactions across adventures
            - To review the party composition for a campaign
            
            Parameters:
            - campaignName: Required. The name of the campaign to filter by
            
            Returns a list of all characters that have the specified campaign name.
            """)
    public String listCharactersByCampaign(String campaignName) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }

        return listCharacters(campaignName);
    }

    @Tool(name = "ChatDM_list_campaigns", description = """
            List all campaign journals.
            
            WHEN TO USE:
            - To review all ongoing storylines
            - When planning cross-adventure connections
            - To check which adventures have established campaigns
            - During campaign planning sessions
            
            Returns a list of all adventures with campaign journals and their last update dates.
            """)
    public String listCampaigns() {
        try {
            Path campaignsPath = campaignsDir;

            if (!Files.exists(campaignsPath)) {
                return "No campaigns directory found.";
            }

            try (Stream<Path> paths = Files.list(campaignsPath)) {
                List<String> campaigns = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith("_campaign.txt"))
                        .map(path -> {
                            try {
                                String content = Files.readString(path);
                                String name = extractValue(content, "NAME:");
                                String lastUpdated = extractValue(content, "LAST_UPDATED:");
                                return String.format("  - %s - Last Updated: %s", name, lastUpdated);
                            } catch (IOException e) {
                                return "  - " + path.getFileName().toString() + " [error reading]";
                            }
                        })
                        .sorted()
                        .collect(Collectors.toList());

                if (campaigns.isEmpty()) {
                    return "No campaign journals saved yet.";
                }

                return "Campaign Journals:\n" + String.join("\n", campaigns);
            }
        } catch (IOException e) {
            return "Error listing campaigns: " + e.getMessage();
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
                    .filter(p -> p.getFileName().toString().endsWith("_" + sanitizedName + ".md")).min((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()))
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

        String value = content.substring(start, end).trim();
        return value.isEmpty() ? "Unknown" : value;
    }

    private String extractMarkdownTitle(String content) {
        int start = content.indexOf("# ");
        if (start == -1) return "Untitled";

        start += 2;
        int end = content.indexOf("\n", start);
        if (end == -1) end = content.length();

        return content.substring(start, end).trim();
    }

    private String extractCampaignName(String content) {
        // Look for "# Campaign: " pattern (case-insensitive)
        String pattern = "# Campaign:";
        String lowerContent = content.toLowerCase();
        String lowerPattern = pattern.toLowerCase();
        int start = lowerContent.indexOf(lowerPattern);

        if (start == -1) {
            return null;
        }

        start += pattern.length();
        int end = content.indexOf("\n", start);
        if (end == -1) end = content.length();

        String campaignName = content.substring(start, end).trim();
        return campaignName.isEmpty() ? null : campaignName;
    }
}
