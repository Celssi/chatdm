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

    @Tool(name = "ChatDM_action", description = """
            Generate a random action or verb for narrative prompts.

            WHEN TO USE:
            - Need inspiration for what happens next
            - NPC or environment does something unexpected
            - Solo play to determine scene development
            - Breaking writer's block during improvisation

            EXAMPLES:
            Returns actions like "Attacks", "Reveals", "Hides", "Discovers", "Betrays",
            "Protects", "Escapes", "Investigates", "Negotiates", "Confronts", etc.

            USAGE PATTERN:
            Combine with subjects for complete prompts: ChatDM_subject + ChatDM_action
            Or use ChatDM_action_subject for instant combinations.

            TIP: Interpret abstractly. "Attacks" might be verbal, physical, or political.
            """)
    public String action() {
        return actions[(int) (Math.random() * actions.length)];
    }

    @Tool(name = "ChatDM_subject", description = """
            Generate a random subject or noun for narrative focus.

            WHEN TO USE:
            - Determining who/what is central to an event
            - Identifying unexpected actors in a scene
            - Solo play to introduce new elements
            - Creating plot hooks and complications

            EXAMPLES:
            Returns subjects like "Guard", "Merchant", "Secret", "Artifact", "Leader",
            "Stranger", "Message", "Creature", "Faction", "Memory", etc.

            USAGE PATTERN:
            - Combine with actions: subject + action = "The merchant betrays"
            - Combine with descriptors: descriptor + subject = "Ancient artifact"
            - Use alone for "Who/what is involved?"

            TIP: Subjects can be people, objects, concepts, or factions.
            """)
    public String subject() {
        return subjects[(int) (Math.random() * subjects.length)];
    }

    @Tool(name = "ChatDM_descriptor", description = """
            Generate a random descriptor or adjective for enhanced detail.

            WHEN TO USE:
            - Adding flavor to people, places, or objects
            - Enhancing narrative descriptions
            - Characterizing situations or atmospheres
            - Distinguishing similar elements

            EXAMPLES:
            Returns descriptors like "Ancient", "Mysterious", "Dangerous", "Hidden",
            "Powerful", "Corrupt", "Sacred", "Forgotten", "Ornate", "Crude", etc.

            USAGE PATTERN:
            - Modify subjects: descriptor + subject = "Mysterious stranger"
            - Modify locations: descriptor + location = "Ancient ruins"
            - Modify situations: descriptor + event = "Dangerous revelation"

            TIP: Layer multiple descriptors for richer detail.
            """)
    public String descriptor() {
        return descriptors[(int) (Math.random() * descriptors.length)];
    }

    @Tool(name = "ChatDM_random_event", description = """
            Generate a random event to inject into the current scene.

            WHEN TO USE:
            - Scenes losing momentum or becoming predictable
            - Solo play to simulate GM surprises
            - Adding complications during travel/downtime
            - Creating dramatic turning points

            EXAMPLES:
            Returns events like "Ambush!", "Discovery", "Betrayal", "Arrival",
            "Revelation", "Chase begins", "Trap triggered", "Alliance formed", etc.

            GAMEPLAY IMPACT:
            - Changes scene direction and energy
            - Creates urgency or new opportunities
            - Tests player adaptability
            - Generates memorable moments

            TIP: Random events work best when they connect to existing story threads.
            Interpret results through the lens of current situation.
            """)
    public String randomEvent() {
        return randomEvents[(int) (Math.random() * randomEvents.length)];
    }

    @Tool(name = "ChatDM_complication", description = """
            Add a complication or twist to escalate the current situation.

            WHEN TO USE:
            - Success feels too easy
            - Need to raise stakes
            - Solo play to simulate challenging GM
            - Creating memorable "Yes, but..." moments

            EXAMPLES:
            Returns complications like "Time runs out", "Reinforcements arrive",
            "Equipment fails", "Witness appears", "Mistaken identity", "Secret revealed",
            "Unintended consequences", "Divided loyalties", etc.

            GAMEPLAY IMPACT:
            - Prevents boring success
            - Creates tough choices
            - Raises tension and drama
            - Forces creative problem-solving

            TIP: Use after successful rolls to add "but..." clauses.
            "You pick the lock, BUT someone's coming down the hall."
            """)
    public String complication() {
        return complications[(int) (Math.random() * complications.length)];
    }

    @Tool(name = "ChatDM_action_subject", description = """
            Generate combined action and subject for instant narrative prompts.

            WHEN TO USE:
            - Need complete story beat immediately
            - Solo play oracle for scene development
            - Breaking narrative stalls
            - Random encounter initiators

            RETURNS:
            Subject + Action combination like "The guard attacks", "The secret reveals itself",
            "The merchant betrays", "The artifact corrupts", etc.

            USAGE:
            1. Generate the combination
            2. Interpret through current context
            3. Elaborate with specific details
            4. Let it drive next scene development

            EXAMPLES IN PLAY:
            - "The stranger investigates" → This NPC starts asking questions
            - "The artifact protects" → Magic item activates defensively
            - "The leader betrays" → Political alliance collapses

            TIP: These are story prompts, not rigid mandates. Adapt to your narrative.
            """)
    public String actionSubject() {
        return String.format("%s %s",
                subjects[(int) (Math.random() * subjects.length)],
                actions[(int) (Math.random() * actions.length)].toLowerCase());
    }
}
