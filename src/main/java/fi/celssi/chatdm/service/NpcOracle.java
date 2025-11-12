package fi.celssi.chatdm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class NpcOracle {

    private String[] npcBehaviors;
    private String[] npcAppearances;
    private String[] npcPersonalities;
    private String[] npcOccupations;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String[]> data;
        TypeReference<Map<String, String[]>> typeRef = new TypeReference<>() {
        };

        // Load NPC behaviors
        ClassPathResource resource = new ClassPathResource("npc-behavior-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        npcBehaviors = data.get("behaviors");

        // Load NPC appearances
        resource = new ClassPathResource("npc-appearance-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        npcAppearances = data.get("appearances");

        // Load NPC personalities
        resource = new ClassPathResource("npc-personality-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        npcPersonalities = data.get("personalities");

        // Load NPC occupations
        resource = new ClassPathResource("npc-occupation-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        npcOccupations = data.get("occupations");
    }

    @Tool(name = "ChatDM_npc_behavior", description = """
            Generate how an NPC behaves or reacts in the current situation.

            WHEN TO USE:
            - Uncertain how an NPC should respond to players
            - Need immediate NPC reaction to unexpected actions
            - Want to add unpredictability to NPC interactions
            - Determining NPC's current mood or demeanor

            Returns behavioral traits like "Suspicious and guarded", "Friendly and helpful", "Nervous and evasive", etc.
            Combine with personality for fuller characterization. Use ChatDM_save_npc to remember important NPCs.
            """)
    public String npcBehavior() {
        return npcBehaviors[(int) (Math.random() * npcBehaviors.length)];
    }

    @Tool(name = "ChatDM_npc_appearance", description = """
            Generate a distinctive physical appearance trait for an NPC.

            WHEN TO USE:
            - Creating a new NPC on the fly
            - Need a memorable visual detail
            - Want to distinguish NPCs from each other
            - Describing someone the players just met

            Returns physical traits like "Scarred face", "Elaborate tattoos", "Unusual height", "Distinctive clothing", etc.
            Use multiple calls for richer descriptions. Combine with other NPC tools for complete characters.
            """)
    public String npcAppearance() {
        return npcAppearances[(int) (Math.random() * npcAppearances.length)];
    }

    @Tool(name = "ChatDM_npc_personality", description = """
            Generate a core personality trait for an NPC.

            WHEN TO USE:
            - Creating NPC character depth
            - Determining how NPC approaches situations
            - Need consistent characterization
            - Building memorable NPCs quickly

            Returns personality traits like "Cautious and paranoid", "Bold and reckless", "Wise and contemplative", etc.
            This defines their fundamental nature. Use ChatDM_npc_behavior for current mood/state.
            """)
    public String npcPersonality() {
        return npcPersonalities[(int) (Math.random() * npcPersonalities.length)];
    }

    @Tool(name = "ChatDM_npc_occupation", description = """
            Determine an NPC's profession, role, or place in society.

            WHEN TO USE:
            - Establishing NPC background and skills
            - Determining what services/information NPC can provide
            - Creating believable settlements and communities
            - Need quick NPC identity in taverns, shops, etc.

            Returns occupations like "Blacksmith", "Traveling merchant", "Town guard", "Herbalist", "Beggar", etc.
            Informs what the NPC knows, can do, and where they fit in the world.
            """)
    public String npcOccupation() {
        return npcOccupations[(int) (Math.random() * npcOccupations.length)];
    }

    @Tool(name = "ChatDM_generate_npc", description = """
            Generate a complete NPC with appearance, personality, occupation, and current behavior.

            WHEN TO USE:
            - Need a full NPC immediately
            - Players enter a tavern/shop/location unexpectedly
            - Want a quick, complete character
            - Random encounter with humanoid NPCs

            RETURNS:
            Four traits in one: Appearance, Personality, Occupation, Behavior
            Example: "Appearance: Scarred hands, Personality: Suspicious, Occupation: Tavern keeper, Behavior: Defensive"

            BEST PRACTICE:
            1. Use this tool to generate the NPC
            2. Add a distinctive name
            3. Weave the traits into natural description
            4. Use ChatDM_save_npc if the NPC might recur

            TIP: The traits are starting points—flesh them out with details, voice, and motivations.
            """)
    public String generateNpc() {
        return String.format("Appearance: %s, Personality: %s, Occupation: %s, Behavior: %s",
                npcAppearances[(int) (Math.random() * npcAppearances.length)],
                npcPersonalities[(int) (Math.random() * npcPersonalities.length)],
                npcOccupations[(int) (Math.random() * npcOccupations.length)],
                npcBehaviors[(int) (Math.random() * npcBehaviors.length)]);
    }
}
