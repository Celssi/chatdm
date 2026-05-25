package fi.celssi.chatdm.service.journal;

import fi.celssi.chatdm.storage.JournalStorage;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fi.celssi.chatdm.service.journal.JournalFileParser.*;

@Service
public class CharacterJournalTools {

    private final JournalStorage storage;

    public CharacterJournalTools(JournalStorage storage) {
        this.storage = storage;
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
            String fileName = sanitizedName + ".txt";

            boolean isUpdate = storage.exists(CHARACTERS, fileName);
            String createdDate = isUpdate ? extractValue(storage.read(CHARACTERS, fileName), "CREATED:") : LocalDateTime.now().format(DATE_FORMAT);

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

            storage.write(CHARACTERS, fileName, contentBuilder.toString());
            return String.format("Character '%s' saved successfully", characterName);
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
            String fileName = sanitizedName + ".txt";

            String content = storage.read(CHARACTERS, fileName);
            if (content == null) {
                return String.format("Error: Character '%s' not found. Use ChatDM_list_characters to see available characters.", characterName);
            }

            return content;
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
            List<String> fileNames = storage.list(CHARACTERS);
            List<String> characters = fileNames.stream()
                    .filter(f -> f.endsWith(".txt"))
                    .map(fileName -> {
                        try {
                            String content = storage.read(CHARACTERS, fileName);
                            if (content == null) return null;
                            String name = extractValue(content, "CHARACTER:");
                            String gameSystem = extractValue(content, "GAME SYSTEM:");
                            String campaign = extractValue(content, "CAMPAIGN:");

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
                            return "  - " + fileName + " [error reading]";
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
        } catch (IOException e) {
            return "Error listing characters: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_update_character", description = """
            Update an existing saved character.
            Parameters:
            - characterName: Required. The name of the character to update
            - gameSystem: Optional. Updated game system (keeps existing if omitted)
            - characterData: Required. Complete updated character information
            - campaignName: Optional. Updated campaign association (keeps existing if omitted)
            
            Preserves the original creation date.
            """)
    public String updateCharacter(String characterName, String gameSystem, String characterData, String campaignName) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return "Error: Character name is required";
        }
        if (characterData == null || characterData.trim().isEmpty()) {
            return "Error: Character data is required";
        }

        try {
            String sanitizedName = sanitizeFilename(characterName);
            String fileName = sanitizedName + ".txt";
            String existingContent = storage.read(CHARACTERS, fileName);
            if (existingContent == null) {
                return String.format("Error: Character '%s' not found. Use ChatDM_save_character to create one.", characterName);
            }

            String createdDate = extractValue(existingContent, "CREATED:");
            String existingGameSystem = extractValue(existingContent, "GAME SYSTEM:");
            String existingCampaign = extractValue(existingContent, "CAMPAIGN:");
            String resolvedGameSystem = (gameSystem != null && !gameSystem.trim().isEmpty())
                    ? gameSystem.trim() : (existingGameSystem.equals("Unknown") ? "" : existingGameSystem);
            String resolvedCampaign = (campaignName != null && !campaignName.trim().isEmpty())
                    ? campaignName.trim()
                    : (existingCampaign.equals("Unknown") ? null : existingCampaign);

            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append(String.format("""
                    CHARACTER: %s
                    GAME SYSTEM: %s
                    CREATED: %s
                    """, characterName, resolvedGameSystem, createdDate));
            if (resolvedCampaign != null && !resolvedCampaign.isEmpty()) {
                contentBuilder.append(String.format("CAMPAIGN: %s\n", resolvedCampaign));
            }
            contentBuilder.append(String.format("""
                    
                    %s
                    """, characterData));

            storage.write(CHARACTERS, fileName, contentBuilder.toString());
            return String.format("Character '%s' updated successfully", characterName);
        } catch (IOException e) {
            return "Error updating character: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_delete_character", description = """
            Delete a saved character.
            Parameters:
            - characterName: Required. The name of the character to delete
            """)
    public String deleteCharacter(String characterName) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return "Error: Character name is required";
        }
        try {
            String fileName = sanitizeFilename(characterName) + ".txt";
            if (!storage.exists(CHARACTERS, fileName)) {
                return String.format("Error: Character '%s' not found.", characterName);
            }
            storage.delete(CHARACTERS, fileName);
            return String.format("Character '%s' deleted.", characterName);
        } catch (IOException e) {
            return "Error deleting character: " + e.getMessage();
        }
    }
}
