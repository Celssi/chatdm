package fi.celssi.chatdm.service.journal;

import fi.celssi.chatdm.storage.JournalStorage;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static fi.celssi.chatdm.service.journal.JournalFileParser.*;

@Service
public class AdventureJournalTools {

    private final JournalStorage storage;

    public AdventureJournalTools(JournalStorage storage) {
        this.storage = storage;
    }

    @Tool(name = "ChatDM_start_adventure", description = """
            Start a new adventure journal.
            Parameters:
            - adventureName: Required. Name of the adventure
            - gameSystem: Required. The game system being used
            - characters: Optional. Comma-separated list of character names participating
            - description: Optional. Brief description of the adventure
            - campaignName: Optional. Campaign this adventure belongs to (links to ChatDM_list_adventures_by_campaign)
            
            Creates a new markdown file for the adventure log.
            """)
    public String startAdventure(String adventureName, String gameSystem, String characters, String description, String campaignName) {
        if (adventureName == null || adventureName.trim().isEmpty()) {
            return "Error: Adventure name is required";
        }
        if (gameSystem == null || gameSystem.trim().isEmpty()) {
            return "Error: Game system is required";
        }

        try {
            String sanitizedName = sanitizeFilename(adventureName);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = timestamp + "_" + sanitizedName + ".md";

            StringBuilder content = new StringBuilder();
            content.append("# ").append(adventureName).append("\n\n");
            content.append("**Game System:** ").append(gameSystem).append("\n\n");
            content.append("**Started:** ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n\n");

            if (campaignName != null && !campaignName.trim().isEmpty()) {
                content.append("**Campaign:** ").append(campaignName.trim()).append("\n\n");
            }

            if (characters != null && !characters.trim().isEmpty()) {
                content.append("**Characters:** ").append(characters).append("\n\n");
            }

            if (description != null && !description.trim().isEmpty()) {
                content.append("## Description\n\n");
                content.append(description).append("\n\n");
            }

            content.append("## Adventure Log\n\n");

            storage.write(ADVENTURES, fileName, content.toString());
            return String.format("Adventure '%s' started.", adventureName);
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
            String latestFile = findLatestAdventureFile(sanitizeFilename(adventureName));

            if (latestFile == null) {
                return String.format("Error: No adventure found with name '%s'. Use ChatDM_start_adventure first.", adventureName);
            }

            String logEntry = String.format("### %s\n\n%s\n\n",
                    LocalDateTime.now().format(DATE_FORMAT), event);

            storage.append(ADVENTURES, latestFile, logEntry);
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
            List<String> fileNames = storage.list(ADVENTURES).stream()
                    .filter(f -> f.endsWith(".md"))
                    .sorted((a, b) -> b.compareTo(a))
                    .toList();

            List<String> adventures = fileNames.stream()
                    .map(fileName -> {
                        try {
                            String content = storage.read(ADVENTURES, fileName);
                            if (content == null) return null;
                            String name = extractMarkdownTitle(content);
                            String gameSystem = extractValue(content, "**Game System:**");
                            String started = extractValue(content, "**Started:**");
                            return String.format("  - %s [%s] - %s", name, gameSystem, started);
                        } catch (IOException e) {
                            return "  - " + fileName + " [error reading]";
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            if (adventures.isEmpty()) {
                return "No adventures logged yet.";
            }

            return "Adventure Journals:\n" + String.join("\n", adventures);
        } catch (IOException e) {
            return "Error listing adventures: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_adventures_by_campaign", description = """
            List all adventure journals that belong to a specific campaign.
            Parameters:
            - campaignName: Required. The name of the campaign to filter by
            
            Returns a list of all adventures that have the specified campaign name in their **Campaign:** field.
            """)
    public String listAdventuresByCampaign(String campaignName) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }

        try {
            List<String> fileNames = storage.list(ADVENTURES).stream()
                    .filter(f -> f.endsWith(".md"))
                    .sorted((a, b) -> b.compareTo(a))
                    .toList();

            List<String> adventures = fileNames.stream()
                    .map(fileName -> {
                        try {
                            String content = storage.read(ADVENTURES, fileName);
                            if (content == null) return null;
                            String adventureCampaign = extractCampaignName(content);

                            if (adventureCampaign == null || !adventureCampaign.equalsIgnoreCase(campaignName.trim())) {
                                return null;
                            }

                            String name = extractMarkdownTitle(content);
                            String gameSystem = extractValue(content, "**Game System:**");
                            String started = extractValue(content, "**Started:**");
                            return String.format("  - %s [%s] - %s", name, gameSystem, started);
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            if (adventures.isEmpty()) {
                return String.format("No adventures found for campaign '%s'.", campaignName);
            }

            return String.format("Adventures for Campaign '%s':\n%s", campaignName, String.join("\n", adventures));
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
            String latestFile = findLatestAdventureFile(sanitizedName);

            if (latestFile == null) {
                return String.format("Error: No adventure found with name '%s'. Use ChatDM_list_adventures to see available adventures.", adventureName);
            }

            String content = storage.read(ADVENTURES, latestFile);
            return content != null ? content : "Error reading adventure.";
        } catch (IOException e) {
            return "Error reading adventure: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_delete_adventure", description = """
            Delete an adventure journal by name (removes the latest file for that adventure name).
            Parameters:
            - adventureName: Required. The name of the adventure to delete
            """)
    public String deleteAdventure(String adventureName) {
        if (adventureName == null || adventureName.trim().isEmpty()) {
            return "Error: Adventure name is required";
        }
        try {
            String latestFile = findLatestAdventureFile(sanitizeFilename(adventureName));
            if (latestFile == null) {
                return String.format("Error: No adventure found with name '%s'.", adventureName);
            }
            storage.delete(ADVENTURES, latestFile);
            return String.format("Adventure '%s' deleted.", adventureName);
        } catch (IOException e) {
            return "Error deleting adventure: " + e.getMessage();
        }
    }

    private String findLatestAdventureFile(String sanitizedName) throws IOException {
        return storage.list(ADVENTURES).stream()
                .filter(f -> f.endsWith("_" + sanitizedName + ".md"))
                .max(String::compareTo)
                .orElse(null);
    }
}
