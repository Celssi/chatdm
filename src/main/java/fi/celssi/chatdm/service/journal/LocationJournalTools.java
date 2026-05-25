package fi.celssi.chatdm.service.journal;

import fi.celssi.chatdm.storage.JournalStorage;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static fi.celssi.chatdm.service.journal.JournalFileParser.*;

@Service
public class LocationJournalTools {

    private final JournalStorage storage;

    public LocationJournalTools(JournalStorage storage) {
        this.storage = storage;
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
            String fileName = sanitizedCampaign + "_" + sanitizedLocation + ".txt";

            String content = String.format("""
                            CAMPAIGN: %s
                            LOCATION: %s
                            CREATED: %s
                            LAST_UPDATED: %s
                            
                            %s
                            """, campaignName, locationName, LocalDateTime.now().format(DATE_FORMAT),
                    LocalDateTime.now().format(DATE_FORMAT), locationData);

            storage.write(LOCATIONS, fileName, content);
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
            String fileName = sanitizedCampaign + "_" + sanitizedLocation + ".txt";

            String content = storage.read(LOCATIONS, fileName);
            if (content == null) {
                return String.format("Error: Location '%s' not found for campaign '%s'. Use ChatDM_list_locations to see available locations.", locationName, campaignName);
            }

            return content;
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
            String fileName = sanitizedCampaign + "_" + sanitizedLocation + ".txt";

            String existingContent = storage.read(LOCATIONS, fileName);
            if (existingContent == null) {
                return String.format("Error: Location '%s' not found for campaign '%s'. Use ChatDM_save_location to create a new location.", locationName, campaignName);
            }

            String createdDate = extractValue(existingContent, "CREATED:");
            String campaign = extractValue(existingContent, "CAMPAIGN:");

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

            storage.write(LOCATIONS, fileName, content);
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
            List<String> fileNames = storage.list(LOCATIONS);

            List<String> locations = fileNames.stream()
                    .filter(f -> f.endsWith(".txt"))
                    .map(fileName -> {
                        try {
                            String content = storage.read(LOCATIONS, fileName);
                                String campaign = extractValue(content, "CAMPAIGN:");
                                String locationName = extractValue(content, "LOCATION:");
                                String created = extractValue(content, "CREATED:");

                                if (campaignName != null && !campaignName.trim().isEmpty()) {
                                    String sanitizedCampaign = sanitizeFilename(campaignName);
                                    String sanitizedFileCampaign = campaign.equals("Unknown") ? null : sanitizeFilename(campaign);
                                    if (sanitizedFileCampaign == null || !sanitizedFileCampaign.equals(sanitizedCampaign)) {
                                        return null;
                                    }
                                }

                                return String.format("  - %s [%s] - Created: %s", locationName, campaign, created);
                            } catch (IOException e) {
                                return "  - " + fileName + " [error reading]";
                            }
                        })
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();

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
        } catch (IOException e) {
            return "Error listing locations: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_delete_location", description = """
            Delete a location from the campaign catalog.
            Parameters:
            - campaignName: Required. The campaign the location belongs to
            - locationName: Required. The name of the location to delete
            """)
    public String deleteLocation(String campaignName, String locationName) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }
        if (locationName == null || locationName.trim().isEmpty()) {
            return "Error: Location name is required";
        }
        try {
            String fileName = sanitizeFilename(campaignName) + "_" + sanitizeFilename(locationName) + ".txt";
            if (!storage.exists(LOCATIONS, fileName)) {
                return String.format("Error: Location '%s' not found for campaign '%s'.", locationName, campaignName);
            }
            storage.delete(LOCATIONS, fileName);
            return String.format("Location '%s' deleted from campaign '%s'.", locationName, campaignName);
        } catch (IOException e) {
            return "Error deleting location: " + e.getMessage();
        }
    }
}
