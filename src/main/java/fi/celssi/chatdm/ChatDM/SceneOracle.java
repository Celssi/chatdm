package fi.celssi.chatdm.ChatDM;

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

    @Tool(name = "ChatDM_scene_setup", description = "Generate a complete scene setup with location, time, weather, and atmosphere.")
    public String sceneSetup() {
        return String.format("Location: %s, Time: %s, Weather: %s, Atmosphere: %s",
                locationTypes[(int) (Math.random() * locationTypes.length)],
                timeOfDay[(int) (Math.random() * timeOfDay.length)],
                weather[(int) (Math.random() * weather.length)],
                atmospheres[(int) (Math.random() * atmospheres.length)]);
    }
}
