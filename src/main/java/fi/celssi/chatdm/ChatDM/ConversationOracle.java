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
public class ConversationOracle {

    private String[] topics;
    private String[] moods;
    private String[] intents;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String[]> data;
        TypeReference<Map<String, String[]>> typeRef = new TypeReference<>() {
        };

        // Load conversation topics
        ClassPathResource resource = new ClassPathResource("conversation-topics-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        topics = data.get("topics");

        // Load conversation moods
        resource = new ClassPathResource("conversation-moods-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        moods = data.get("moods");

        // Load conversation intents
        resource = new ClassPathResource("conversation-intents-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        intents = data.get("intents");
    }

    @Tool(name = "ChatDM_conversation_topic",
            description = "Generate a random conversation topic. Useful for determining what an NPC wants to talk about or what subject comes up in conversation.")
    public String conversationTopic() {
        return topics[(int) (Math.random() * topics.length)];
    }

    @Tool(name = "ChatDM_conversation_mood",
            description = "Generate a random conversation mood. Useful for determining the emotional tone or attitude of an NPC during conversation.")
    public String conversationMood() {
        return moods[(int) (Math.random() * moods.length)];
    }

    @Tool(name = "ChatDM_conversation_intent",
            description = "Generate a random conversation intent. Useful for determining what an NPC is trying to achieve or communicate in the conversation.")
    public String conversationIntent() {
        return intents[(int) (Math.random() * intents.length)];
    }

    @Tool(name = "ChatDM_conversation_setup",
            description = "Generate a complete conversation setup with topic, mood, and intent. Useful for quickly creating a full conversational context.")
    public String conversationSetup() {
        String topic = topics[(int) (Math.random() * topics.length)];
        String mood = moods[(int) (Math.random() * moods.length)];
        String intent = intents[(int) (Math.random() * intents.length)];

        return "Topic: " + topic + "\n" +
                "Mood: " + mood + "\n" +
                "Intent: " + intent;
    }
}
