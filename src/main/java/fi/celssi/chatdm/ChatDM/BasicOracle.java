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
public class BasicOracle {

    private String[] yesOrNoAnswers;
    private Map<String, String[]> likelihoodAnswers;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, String[]>> typeRef = new TypeReference<>() {
        };

        // Load yes-or-no answers
        ClassPathResource resource = new ClassPathResource("yes-or-no-answers.json");
        Map<String, String[]> data = mapper.readValue(resource.getInputStream(), typeRef);
        yesOrNoAnswers = data.get("yesOrNoAnswers");

        // Load likelihood answers
        resource = new ClassPathResource("likelihood-answers.json");
        likelihoodAnswers = mapper.readValue(resource.getInputStream(), typeRef);
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
        String[] answers = (String[]) likelihoodAnswers.get(likelihood);
        if (answers == null) {
            return "Invalid likelihood. Use: almostCertain, likely, fiftyFifty, unlikely, or almostImpossible";
        }
        return answers[(int) (Math.random() * answers.length)];
    }
}
