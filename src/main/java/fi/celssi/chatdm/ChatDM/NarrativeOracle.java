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
public class NarrativeOracle {

    private String[] actions;
    private String[] subjects;
    private String[] descriptors;
    private String[] randomEvents;
    private String[] complications;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String[]> data;
        TypeReference<Map<String, String[]>> typeRef = new TypeReference<>() {
        };

        // Load actions
        ClassPathResource resource = new ClassPathResource("actions-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        actions = data.get("actions");

        // Load subjects
        resource = new ClassPathResource("subjects-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        subjects = data.get("subjects");

        // Load descriptors
        resource = new ClassPathResource("descriptors-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        descriptors = data.get("descriptors");

        // Load random events
        resource = new ClassPathResource("random-event-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        randomEvents = data.get("events");

        // Load complications
        resource = new ClassPathResource("complication-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        complications = data.get("complications");
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

    @Tool(name = "ChatDM_random_event", description = "Generate a random event to add to the current scene or situation.")
    public String randomEvent() {
        return randomEvents[(int) (Math.random() * randomEvents.length)];
    }

    @Tool(name = "ChatDM_complication", description = "Add a complication or twist to the current situation.")
    public String complication() {
        return complications[(int) (Math.random() * complications.length)];
    }

    @Tool(name = "ChatDM_action_subject", description = "Generate a random action and subject combination to create a prompt like 'The guard attacks' or 'The wizard hides'.")
    public String actionSubject() {
        return String.format("%s %s",
                subjects[(int) (Math.random() * subjects.length)],
                actions[(int) (Math.random() * actions.length)].toLowerCase());
    }
}
