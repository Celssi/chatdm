package fi.celssi.chatdm.ChatDM;

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
        Map<String, String[]> data;

        // Load dungeon rooms
        ClassPathResource resource = new ClassPathResource("dungeon-room-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        dungeonRooms = data.get("rooms");

        // Load treasures
        resource = new ClassPathResource("treasure-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        treasures = data.get("treasures");

        // Load traps
        resource = new ClassPathResource("trap-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        traps = data.get("traps");

        // Load directions
        resource = new ClassPathResource("direction-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        directions = data.get("directions");
    }

    @Tool(name = "ChatDM_dungeon_room", description = "Determine what is in a dungeon room or chamber.")
    public String dungeonRoom() {
        return dungeonRooms[(int) (Math.random() * dungeonRooms.length)];
    }

    @Tool(name = "ChatDM_treasure", description = "Generate a random treasure or valuable item.")
    public String treasure() {
        return treasures[(int) (Math.random() * treasures.length)];
    }

    @Tool(name = "ChatDM_trap", description = "Generate a random trap type.")
    public String trap() {
        return traps[(int) (Math.random() * traps.length)];
    }

    @Tool(name = "ChatDM_direction", description = "Determine a random direction.")
    public String direction() {
        return directions[(int) (Math.random() * directions.length)];
    }
}
