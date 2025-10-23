package fi.celssi.chatdm.ChatDM;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class Oracle {

    private String[] yesOrNoAnswers;
    private Map<String, String[]> likelihoodAnswers;
    private String[] actions;
    private String[] subjects;
    private String[] descriptors;
    private String[] npcBehaviors;
    private String[] npcAppearances;
    private String[] npcPersonalities;
    private String[] npcOccupations;
    private String[] locationTypes;
    private String[] atmospheres;
    private String[] weather;
    private String[] timeOfDay;
    private String[] randomEvents;
    private String[] complications;
    private String[] dungeonRooms;
    private String[] treasures;
    private String[] traps;
    private String[] directions;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Load yes-or-no answers
        ClassPathResource resource = new ClassPathResource("yes-or-no-answers.json");
        Map<String, String[]> data = mapper.readValue(resource.getInputStream(), Map.class);
        yesOrNoAnswers = data.get("yesOrNoAnswers");

        // Load likelihood answers
        resource = new ClassPathResource("likelihood-answers.json");
        likelihoodAnswers = mapper.readValue(resource.getInputStream(), Map.class);

        // Load actions
        resource = new ClassPathResource("actions-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        actions = data.get("actions");

        // Load subjects
        resource = new ClassPathResource("subjects-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        subjects = data.get("subjects");

        // Load descriptors
        resource = new ClassPathResource("descriptors-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        descriptors = data.get("descriptors");

        // Load NPC behaviors
        resource = new ClassPathResource("npc-behavior-oracle.json");
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

        // Load location types
        resource = new ClassPathResource("location-type-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        locationTypes = data.get("locations");

        // Load atmospheres
        resource = new ClassPathResource("atmosphere-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        atmospheres = data.get("atmospheres");

        // Load weather
        resource = new ClassPathResource("weather-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        weather = data.get("weather");

        // Load time of day
        resource = new ClassPathResource("time-of-day-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        timeOfDay = data.get("times");

        // Load random events
        resource = new ClassPathResource("random-event-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        randomEvents = data.get("events");

        // Load complications
        resource = new ClassPathResource("complication-oracle.json");
        data = mapper.readValue(resource.getInputStream(), Map.class);
        complications = data.get("complications");

        // Load dungeon rooms
        resource = new ClassPathResource("dungeon-room-oracle.json");
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

    @Tool(name = "ChatDM_yes_or_no", description = "Use this oracle when playing a rpg to determine an answer for yes or no questions.")
    public String yesOrNo() {
        return yesOrNoAnswers[(int) (Math.random() * yesOrNoAnswers.length)];
    }

    @Tool(name = "ChatDM_roll_dice", description = """
            Use this tool to roll a dice with the specified number of sides.
            Common dice types: 4-sided (d4), 6-sided (d6), 8-sided (d8),
            10-sided (d10), 12-sided (d12), 20-sided (d20), 100-sided (d100).
            Parameter 'sides' must be a positive integer representing the number of sides on the dice.
            """)
    public int rollDice(int sides) {
        if (sides < 1) {
            throw new IllegalArgumentException("Dice must have at least 1 side");
        }
        return (int) (Math.random() * sides) + 1;
    }

    @Tool(name = "ChatDM_likelihood", description = """
            Ask a yes or no question with a specific likelihood. This is useful for determining outcomes based on probability.
            Likelihood options: 'almostCertain' (90% yes), 'likely' (70% yes), 'fiftyFifty' (50% yes),
            'unlikely' (20% yes), 'almostImpossible' (10% yes).
            """)
    public String likelihood(String likelihood) {
        String[] answers = likelihoodAnswers.get(likelihood);
        if (answers == null) {
            return "Invalid likelihood. Use: almostCertain, likely, fiftyFifty, unlikely, or almostImpossible";
        }
        return answers[(int) (Math.random() * answers.length)];
    }

    @Tool(name = "ChatDM_action", description = "Get a random action or verb. Useful for determining what happens next in a scene.")
    public String action() {
        return actions[(int) (Math.random() * actions.length)];
    }

    @Tool(name = "ChatDM_subject", description = "Get a random subject or noun. Useful for determining who or what is involved.")
    public String subject() {
        return subjects[(int) (Math.random() * subjects.length)];
    }

    @Tool(name = "ChatDM_descriptor", description = "Get a random descriptor or adjective. Useful for adding details to people, places, or things.")
    public String descriptor() {
        return descriptors[(int) (Math.random() * descriptors.length)];
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

    @Tool(name = "ChatDM_location_type", description = "Generate a random location type. Useful for determining where a scene takes place.")
    public String locationType() {
        return locationTypes[(int) (Math.random() * locationTypes.length)];
    }

    @Tool(name = "ChatDM_atmosphere", description = "Determine the atmosphere or mood of a location or scene.")
    public String atmosphere() {
        return atmospheres[(int) (Math.random() * atmospheres.length)];
    }

    @Tool(name = "ChatDM_weather", description = "Generate random weather conditions.")
    public String weather() {
        return weather[(int) (Math.random() * weather.length)];
    }

    @Tool(name = "ChatDM_time_of_day", description = "Determine what time of day it is.")
    public String timeOfDay() {
        return timeOfDay[(int) (Math.random() * timeOfDay.length)];
    }

    @Tool(name = "ChatDM_random_event", description = "Generate a random event to add to the current scene or situation.")
    public String randomEvent() {
        return randomEvents[(int) (Math.random() * randomEvents.length)];
    }

    @Tool(name = "ChatDM_complication", description = "Add a complication or twist to the current situation.")
    public String complication() {
        return complications[(int) (Math.random() * complications.length)];
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

    @Tool(name = "ChatDM_scene_setup", description = "Generate a complete scene setup with location, time, weather, and atmosphere.")
    public String sceneSetup() {
        return String.format("Location: %s, Time: %s, Weather: %s, Atmosphere: %s",
                locationTypes[(int) (Math.random() * locationTypes.length)],
                timeOfDay[(int) (Math.random() * timeOfDay.length)],
                weather[(int) (Math.random() * weather.length)],
                atmospheres[(int) (Math.random() * atmospheres.length)]);
    }

    @Tool(name = "ChatDM_generate_npc", description = "Generate a complete random NPC with appearance, personality, occupation, and behavior.")
    public String generateNpc() {
        return String.format("Appearance: %s, Personality: %s, Occupation: %s, Behavior: %s",
                npcAppearances[(int) (Math.random() * npcAppearances.length)],
                npcPersonalities[(int) (Math.random() * npcPersonalities.length)],
                npcOccupations[(int) (Math.random() * npcOccupations.length)],
                npcBehaviors[(int) (Math.random() * npcBehaviors.length)]);
    }

    @Tool(name = "ChatDM_action_subject", description = "Generate a random action and subject combination to create a prompt like 'The guard attacks' or 'The wizard hides'.")
    public String actionSubject() {
        return String.format("%s %s",
                subjects[(int) (Math.random() * subjects.length)],
                actions[(int) (Math.random() * actions.length)].toLowerCase());
    }
}
