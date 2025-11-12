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
public class SceneOracle {

    private String[] locationTypes;
    private String[] atmospheres;
    private String[] weather;
    private String[] timeOfDay;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String[]> data;
        TypeReference<Map<String, String[]>> typeRef = new TypeReference<>() {
        };
        
        // Load location types
        ClassPathResource resource = new ClassPathResource("location-type-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        locationTypes = data.get("locations");

        // Load atmospheres
        resource = new ClassPathResource("atmosphere-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        atmospheres = data.get("atmospheres");

        // Load weather
        resource = new ClassPathResource("weather-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        weather = data.get("weather");

        // Load time of day
        resource = new ClassPathResource("time-of-day-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        timeOfDay = data.get("times");
    }

    @Tool(name = "ChatDM_location_type", description = """
            Generate a random location type for scene setting.

            WHEN TO USE:
            - Starting a new scene or encounter
            - Players travel to an unknown destination
            - Need quick location inspiration
            - Random encounters or exploration

            EXAMPLES:
            Returns location types like "Ancient ruins", "Dense forest", "Busy marketplace", "Mountain pass",
            "Tavern", "Cave system", "Noble's estate", "Abandoned temple", etc.

            Use with other scene tools (atmosphere, weather, time) for complete scene setting.
            """)
    public String locationType() {
        return locationTypes[(int) (Math.random() * locationTypes.length)];
    }

    @Tool(name = "ChatDM_atmosphere", description = """
            Generate the atmosphere or mood of a location or scene.

            WHEN TO USE:
            - Setting the tone for a new scene
            - Adding emotional depth to locations
            - Creating tension or comfort
            - Enhancing descriptive narration

            EXAMPLES:
            Returns atmospheric qualities like "Tense and foreboding", "Peaceful and serene",
            "Chaotic and busy", "Mysterious and quiet", "Oppressive and dark", etc.

            TIP: Use atmosphere to guide your narrative tone and NPC reactions.
            Combine with weather and location for richer scene-setting.
            """)
    public String atmosphere() {
        return atmospheres[(int) (Math.random() * atmospheres.length)];
    }

    @Tool(name = "ChatDM_weather", description = """
            Generate current weather conditions for outdoor scenes.

            WHEN TO USE:
            - Outdoor scenes and travel
            - Setting environmental challenges
            - Adding realism to wilderness adventures
            - Journey complications or benefits

            EXAMPLES:
            Returns weather like "Clear skies", "Heavy rain", "Dense fog", "Snowstorm",
            "Hot and humid", "Windy", "Overcast", etc.

            GAMEPLAY IMPACT:
            - Affects visibility, travel speed, and comfort
            - Can create tactical advantages/disadvantages in combat
            - Influences NPC behavior and scene mood
            """)
    public String weather() {
        return weather[(int) (Math.random() * weather.length)];
    }

    @Tool(name = "ChatDM_time_of_day", description = """
            Determine the current time of day for time-sensitive scenes.

            WHEN TO USE:
            - Tracking time during adventures
            - Determining NPC availability
            - Light/visibility conditions
            - Setting scene timing

            EXAMPLES:
            Returns times like "Dawn", "Morning", "Midday", "Afternoon", "Dusk",
            "Evening", "Midnight", "Late night", etc.

            GAMEPLAY IMPACT:
            - Affects visibility (darkvision, light sources)
            - Determines who/what is active (nocturnal creatures, businesses)
            - Influences social encounters (who's available, where)
            """)
    public String timeOfDay() {
        return timeOfDay[(int) (Math.random() * timeOfDay.length)];
    }

    @Tool(name = "ChatDM_scene_setup", description = """
            Generate a complete scene setup with all environmental elements at once.

            WHEN TO USE:
            - Need a full scene quickly
            - Starting a new chapter or session
            - Random encounters requiring complete context
            - Improvising unexpected locations

            RETURNS:
            Four elements combined: Location type, Time of day, Weather, Atmosphere
            Example: "Location: Mountain pass, Time: Dusk, Weather: Light snow, Atmosphere: Tense and foreboding"

            BEST PRACTICE:
            1. Use this tool for instant scene foundation
            2. Flesh out details based on the random elements
            3. Let the combination inspire specific features
            4. Adjust any element that doesn't fit your story

            TIP: The random combinations often create interesting contrasts
            (e.g., "peaceful atmosphere" in "dangerous ruins" = calm before the storm).
            """)
    public String sceneSetup() {
        return String.format("Location: %s, Time: %s, Weather: %s, Atmosphere: %s",
                locationTypes[(int) (Math.random() * locationTypes.length)],
                timeOfDay[(int) (Math.random() * timeOfDay.length)],
                weather[(int) (Math.random() * weather.length)],
                atmospheres[(int) (Math.random() * atmospheres.length)]);
    }
}
