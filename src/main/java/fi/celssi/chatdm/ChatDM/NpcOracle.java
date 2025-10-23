package fi.celssi.chatdm.ChatDM;

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

        // Load NPC behaviors
        ClassPathResource resource = new ClassPathResource("npc-behavior-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        npcBehaviors = data.get("behaviors");

        // Load NPC appearances
        resource = new ClassPathResource("npc-appearance-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        npcAppearances = data.get("appearances");

        // Load NPC personalities
        resource = new ClassPathResource("npc-personality-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        npcPersonalities = data.get("personalities");

        // Load NPC occupations
        resource = new ClassPathResource("npc-occupation-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        npcOccupations = data.get("occupations");
    }

    @Tool(name = "ChatDM_npc_behavior", description = "Determine how an NPC behaves or reacts in a situation.")
    public String npcBehavior() {
        return npcBehaviors[(int) (Math.random() * npcBehaviors.length)];
    }

    @Tool(name = "ChatDM_npc_appearance", description = "Generate a random physical appearance trait for an NPC.")
    public String npcAppearance() {
        return npcAppearances[(int) (Math.random() * npcAppearances.length)];
    }

    @Tool(name = "ChatDM_npc_personality", description = "Generate a random personality trait for an NPC.")
    public String npcPersonality() {
        return npcPersonalities[(int) (Math.random() * npcPersonalities.length)];
    }

    @Tool(name = "ChatDM_npc_occupation", description = "Determine what an NPC does for a living or their role in society.")
    public String npcOccupation() {
        return npcOccupations[(int) (Math.random() * npcOccupations.length)];
    }

    @Tool(name = "ChatDM_generate_npc", description = "Generate a complete random NPC with appearance, personality, occupation, and behavior.")
    public String generateNpc() {
        return String.format("Appearance: %s, Personality: %s, Occupation: %s, Behavior: %s",
                npcAppearances[(int) (Math.random() * npcAppearances.length)],
                npcPersonalities[(int) (Math.random() * npcPersonalities.length)],
                npcOccupations[(int) (Math.random() * npcOccupations.length)],
                npcBehaviors[(int) (Math.random() * npcBehaviors.length)]);
    }
}
