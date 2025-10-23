package fi.celssi.chatdm.ChatDM;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class Oracle {

    @Tool(name = "ChatDM_yes_or_no", description = "Use this oracle when playing a rpg to determine an answer for yes or no questions.")
    public String yesOrNo() {
        String[] answers = {"Strong no", "No", "No", "No", "Weak no", "Weak yes", "Yes", "Yes", "Yes", "Strong yes"};
        return answers[(int) (Math.random() * answers.length)];
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
