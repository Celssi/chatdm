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

    @Tool(name = "ChatDM_conversation_topic", description = """
            Generate what an NPC wants to discuss or what subject arises in conversation.

            WHEN TO USE:
            - Starting NPC dialogue
            - NPC initiates unexpected conversation
            - Social encounters need direction
            - Information-gathering scenes

            EXAMPLES:
            Returns topics like "Local rumors", "Personal troubles", "Political intrigue",
            "Business proposal", "Warning", "Request for help", "Past events", etc.

            USAGE:
            - Guides conversation content and direction
            - Suggests what information NPC might share
            - Creates natural dialogue hooks
            - Can reveal plot information organically

            TIP: Topic doesn't mean NPC volunteers everything—they may need persuasion.
            """)
    public String conversationTopic() {
        return topics[(int) (Math.random() * topics.length)];
    }

    @Tool(name = "ChatDM_conversation_mood", description = """
            Determine the emotional tone or attitude of an NPC during conversation.

            WHEN TO USE:
            - Establishing NPC's current emotional state
            - Roleplaying social encounters
            - Determining difficulty of persuasion/intimidation
            - Adding depth to dialogue

            EXAMPLES:
            Returns moods like "Nervous", "Friendly", "Hostile", "Cautious", "Enthusiastic",
            "Melancholy", "Suspicious", "Desperate", "Cheerful", "Angry", etc.

            GAMEPLAY IMPACT:
            - Affects social skill DCs
            - Influences NPC's willingness to help
            - Guides your vocal/descriptive choices
            - Suggests appropriate player approaches

            TIP: Mood can change during conversation based on player actions.
            """)
    public String conversationMood() {
        return moods[(int) (Math.random() * moods.length)];
    }

    @Tool(name = "ChatDM_conversation_intent", description = """
            Determine what an NPC is trying to achieve or communicate in conversation.

            WHEN TO USE:
            - Understanding NPC's underlying goal
            - Detecting hidden agendas
            - Roleplaying social manipulation
            - Insight checks reveal true intentions

            EXAMPLES:
            Returns intents like "Seeks information", "Wants to deceive", "Needs help",
            "Testing loyalty", "Making threats", "Building trust", "Stalling for time", etc.

            GAMEPLAY USE:
            - Guides NPC's conversational tactics
            - Determines what they'll say or conceal
            - Affects Insight vs Deception contests
            - Creates layered social encounters

            TIP: Intent may differ from stated topic. Friendly mood + deceptive intent = manipulation.
            """)
    public String conversationIntent() {
        return intents[(int) (Math.random() * intents.length)];
    }

    @Tool(name = "ChatDM_conversation_setup", description = """
            Generate complete conversation context with topic, mood, and intent at once.

            WHEN TO USE:
            - Need instant full conversational framework
            - NPC initiates unexpected dialogue
            - Social encounters requiring depth
            - Solo play social adjudication

            RETURNS:
            Three elements combined with line breaks:
            - Topic: What they want to discuss
            - Mood: How they're feeling emotionally
            - Intent: What they're really trying to accomplish

            EXAMPLE OUTPUT:
            "Topic: Local rumors
            Mood: Nervous
            Intent: Seeks information"

            USAGE PATTERN:
            1. Generate the setup
            2. Interpret through NPC's personality and situation
            3. Roleplay conversation with these guides
            4. Let player actions shift mood and reveal intent

            TIP: The combination creates complex, realistic social encounters.
            Nervous mood + "warns" topic + "builds trust" intent = ally reaching out carefully.
            """)
    public String conversationSetup() {
        String topic = topics[(int) (Math.random() * topics.length)];
        String mood = moods[(int) (Math.random() * moods.length)];
        String intent = intents[(int) (Math.random() * intents.length)];

        return "Topic: " + topic + "\n" +
                "Mood: " + mood + "\n" +
                "Intent: " + intent;
    }
}
