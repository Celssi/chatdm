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

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("yes-or-no-answers.json");
        Map<String, String[]> data = mapper.readValue(resource.getInputStream(), Map.class);
        yesOrNoAnswers = data.get("yesOrNoAnswers");
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
}
