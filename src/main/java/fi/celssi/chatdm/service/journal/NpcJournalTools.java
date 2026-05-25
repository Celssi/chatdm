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
public class NpcJournalTools {

    private final JournalStorage storage;

    public NpcJournalTools(JournalStorage storage) {
        this.storage = storage;
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
            String fileName = sanitizedCampaign + "_" + sanitizedNpc + ".txt";

            String content = String.format("""
                            CAMPAIGN: %s
                            NPC: %s
                            CREATED: %s
                            LAST_UPDATED: %s
                            
                            %s
                            """, campaignName, npcName,                     LocalDateTime.now().format(DATE_FORMAT),
                    LocalDateTime.now().format(DATE_FORMAT), npcData);

            storage.write(NPCS, fileName, content);
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
            String fileName = sanitizedCampaign + "_" + sanitizedNpc + ".txt";

            String content = storage.read(NPCS, fileName);
            if (content == null) {
                return String.format("Error: NPC '%s' not found for campaign '%s'. Use ChatDM_list_npcs to see available NPCs.", npcName, campaignName);
            }

            return content;
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
            String fileName = sanitizedCampaign + "_" + sanitizedNpc + ".txt";

            String existingContent = storage.read(NPCS, fileName);
            if (existingContent == null) {
                return String.format("Error: NPC '%s' not found for campaign '%s'. Use ChatDM_save_npc to create a new NPC.", npcName, campaignName);
            }

            String createdDate = extractValue(existingContent, "CREATED:");
            String campaign = extractValue(existingContent, "CAMPAIGN:");

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

            storage.write(NPCS, fileName, content);
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
            List<String> fileNames = storage.list(NPCS);

            List<String> npcs = fileNames.stream()
                    .filter(f -> f.endsWith(".txt"))
                    .map(fileName -> {
                        try {
                            String content = storage.read(NPCS, fileName);
                                String campaign = extractValue(content, "CAMPAIGN:");
                                String npcName = extractValue(content, "NPC:");
                                String created = extractValue(content, "CREATED:");

                                if (campaignName != null && !campaignName.trim().isEmpty()) {
                                    String sanitizedCampaign = sanitizeFilename(campaignName);
                                    String sanitizedFileCampaign = campaign.equals("Unknown") ? null : sanitizeFilename(campaign);
                                    if (sanitizedFileCampaign == null || !sanitizedFileCampaign.equals(sanitizedCampaign)) {
                                        return null;
                                    }
                                }

                                return String.format("  - %s [%s] - Created: %s", npcName, campaign, created);
                            } catch (IOException e) {
                                return "  - " + fileName + " [error reading]";
                            }
                        })
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();

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
        } catch (IOException e) {
            return "Error listing NPCs: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_delete_npc", description = """
            Delete an NPC from the campaign catalog.
            Parameters:
            - campaignName: Required. The campaign the NPC belongs to
            - npcName: Required. The name of the NPC to delete
            """)
    public String deleteNpc(String campaignName, String npcName) {
        if (campaignName == null || campaignName.trim().isEmpty()) {
            return "Error: Campaign name is required";
        }
        if (npcName == null || npcName.trim().isEmpty()) {
            return "Error: NPC name is required";
        }
        try {
            String fileName = sanitizeFilename(campaignName) + "_" + sanitizeFilename(npcName) + ".txt";
            if (!storage.exists(NPCS, fileName)) {
                return String.format("Error: NPC '%s' not found for campaign '%s'.", npcName, campaignName);
            }
            storage.delete(NPCS, fileName);
            return String.format("NPC '%s' deleted from campaign '%s'.", npcName, campaignName);
        } catch (IOException e) {
            return "Error deleting NPC: " + e.getMessage();
        }
    }
}
