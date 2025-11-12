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
public class DungeonOracle {

    private String[] dungeonRooms;
    private String[] treasures;
    private String[] traps;
    private String[] directions;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, String[]>> typeRef = new TypeReference<>() {
        };

        // Load dungeon rooms
        ClassPathResource resource = new ClassPathResource("dungeon-room-oracle.json");
        Map<String, String[]> data = mapper.readValue(resource.getInputStream(), typeRef);
        dungeonRooms = data.get("rooms");

        // Load treasures
        resource = new ClassPathResource("treasure-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        treasures = data.get("treasures");

        // Load traps
        resource = new ClassPathResource("trap-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        traps = data.get("traps");

        // Load directions
        resource = new ClassPathResource("direction-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        directions = data.get("directions");
    }

    @Tool(name = "ChatDM_dungeon_room", description = """
            Generate dungeon room contents for exploration and discovery.

            WHEN TO USE:
            - Players enter a new room in a dungeon/ruins/cave
            - Need quick room contents for improvisation
            - Procedurally generating dungeon areas
            - Adding variety to dungeon crawls

            EXAMPLES:
            Returns room contents like "Guard post", "Prison cells", "Treasury", "Altar room",
            "Barracks", "Library", "Torture chamber", "Storage area", "Empty chamber", etc.

            GAMEPLAY USE:
            - Informs encounters (guards in guard post, prisoners in cells)
            - Suggests treasure/loot locations
            - Creates narrative opportunities
            - Combines with traps, monsters, and treasure tools

            TIP: Room contents suggest appropriate challenges and rewards.
            """)
    public String dungeonRoom() {
        return dungeonRooms[(int) (Math.random() * dungeonRooms.length)];
    }

    @Tool(name = "ChatDM_treasure", description = """
            Generate treasure or valuable items as rewards.

            WHEN TO USE:
            - Defeating enemies or monsters
            - Searching rooms, chests, or bodies
            - Quest rewards and loot distribution
            - Hidden caches and discoveries

            EXAMPLES:
            Returns treasures like "Gold coins", "Precious gems", "Ancient artifact",
            "Magic potion", "Fine weapon", "Jewelry", "Art object", "Rare book", etc.

            GAMEPLAY USE:
            - Provides tangible rewards for exploration and combat
            - Can be sold, traded, or used
            - May have plot significance
            - Motivates continued adventuring

            TIP: Combine generic results with specific details for your setting.
            "Precious gems" → "Three sapphires set in a dwarven brooch"
            """)
    public String treasure() {
        return treasures[(int) (Math.random() * treasures.length)];
    }

    @Tool(name = "ChatDM_trap", description = """
            Generate trap types for hazardous locations.

            WHEN TO USE:
            - Dungeons, ruins, and tombs
            - Protecting treasure or important areas
            - Adding danger to exploration
            - Testing player caution and perception

            EXAMPLES:
            Returns trap types like "Pit trap", "Poison dart", "Falling block", "Magic alarm",
            "Swinging blade", "Collapsing floor", "Arrow trap", "Fire jet", etc.

            GAMEPLAY USE:
            - Requires Perception checks to detect
            - Requires skill checks or clever solutions to disarm
            - Deals damage or creates complications
            - Rewards cautious play

            MECHANICS:
            - Set DC for detection (typically 13-18)
            - Set DC for disarm (typically 15-20)
            - Determine damage/effect based on level
            - Consider warning signs players might notice
            """)
    public String trap() {
        return traps[(int) (Math.random() * traps.length)];
    }

    @Tool(name = "ChatDM_direction", description = """
            Generate a random direction for navigation or events.

            WHEN TO USE:
            - Determining travel/chase direction
            - Random encounters approaching from unknown direction
            - Dungeon navigation (which way do passages lead)
            - Scatter patterns (explosions, fleeing groups)
            - Wind direction, sound sources, etc.

            EXAMPLES:
            Returns cardinal/ordinal directions: "North", "Northeast", "East", "Southeast",
            "South", "Southwest", "West", "Northwest"

            GAMEPLAY USE:
            - Orienting players and tracking position
            - Determining approach vectors for enemies
            - Navigation challenges in wilderness or dungeons
            - Environmental effects (wind, sound, smells)

            TIP: Combine with distance for complete spatial information.
            """)

    public String direction() {
        return directions[(int) (Math.random() * directions.length)];
    }
}
