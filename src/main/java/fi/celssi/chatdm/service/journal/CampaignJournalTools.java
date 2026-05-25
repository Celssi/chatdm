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
public class CampaignJournalTools {

    private final JournalStorage storage;

    public CampaignJournalTools(JournalStorage storage) {
        this.storage = storage;
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
            String fileName = sanitizedCampaign + "_campaign.txt";

            boolean isUpdate = storage.exists(CAMPAIGNS, fileName);
            String createdDate = isUpdate ? extractValue(storage.read(CAMPAIGNS, fileName), "CREATED:") : LocalDateTime.now().format(DATE_FORMAT);

            String content = String.format("""
                    NAME: %s
                    CREATED: %s
                    LAST_UPDATED: %s
                    
                    %s
                    """, campaignName, createdDate, LocalDateTime.now().format(DATE_FORMAT), campaignData);

            storage.write(CAMPAIGNS, fileName, content);
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
            String fileName = sanitizedCampaign + "_campaign.txt";

            String content = storage.read(CAMPAIGNS, fileName);
            if (content == null) {
                return String.format("Error: No campaign %s found. Use ChatDM_save_campaign to create one.", campaignName);
            }

            return content;
        } catch (IOException e) {
            return "Error loading campaign: " + e.getMessage();
        }
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
            List<String> fileNames = storage.list(CAMPAIGNS);

            List<String> campaigns = fileNames.stream()
                    .filter(f -> f.endsWith("_campaign.txt"))
                    .map(fileName -> {
                        try {
                            String content = storage.read(CAMPAIGNS, fileName);
                            if (content == null) return null;
                            String name = extractValue(content, "NAME:");
                            String lastUpdated = extractValue(content, "LAST_UPDATED:");
                            return String.format("  - %s - Last Updated: %s", name, lastUpdated);
                        } catch (IOException e) {
                            return "  - " + fileName + " [error reading]";
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();

            if (campaigns.isEmpty()) {
                return "No campaign journals saved yet.";
            }

            return "Campaign Journals:\n" + String.join("\n", campaigns);
        } catch (IOException e) {
            return "Error listing campaigns: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_delete_campaign", description = """
            Delete a campaign journal.
            Parameters:
            - campaignName: Required. The name of the campaign to delete
            """)
    public String deleteCampaign(String campaignName) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }
        try {
            String fileName = sanitizeFilename(campaignName) + "_campaign.txt";
            if (!storage.exists(CAMPAIGNS, fileName)) {
                return String.format("Error: Campaign '%s' not found.", campaignName);
            }
            storage.delete(CAMPAIGNS, fileName);
            return String.format("Campaign '%s' deleted.", campaignName);
        } catch (IOException e) {
            return "Error deleting campaign: " + e.getMessage();
        }
    }
}
