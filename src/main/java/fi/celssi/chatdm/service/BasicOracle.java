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

    @Tool(name = "ChatDM_yes_or_no", description = """
            Ask a binary yes/no question to determine uncertain outcomes in the game world.

            WHEN TO USE:
            - Uncertain situations where you need a quick decision
            - Solo play when there's no GM to adjudicate
            - Determining NPC reactions, environmental factors, or random events

            EXAMPLES:
            - "Does the guard notice me sneaking past?"
            - "Is there a healer in this village?"
            - "Does the storm stop before nightfall?"

            Returns a nuanced answer from: "Yes", "Yes, and...", "Yes, but...", "No", "No, and...", "No, but..."
            Use ChatDM_likelihood for probability-weighted questions.
            """)
    public String yesOrNo() {
        return yesOrNoAnswers[(int) (Math.random() * yesOrNoAnswers.length)];
    }

    @Tool(name = "ChatDM_roll_dice", description = """
            Roll a dice with any number of sides for game mechanics.

            WHEN TO USE:
            - Ability checks, attack rolls, damage rolls
            - Random table lookups (d6, d10, d12, d20, d100)
            - Any game mechanic requiring dice rolls

            COMMON DICE:
            - d4 (4 sides): Small damage, minor tables
            - d6 (6 sides): Common damage, many tables
            - d8 (8 sides): Medium weapons, some tables
            - d10 (10 sides): Percentile rolls, larger damage
            - d12 (12 sides): Great weapons, barbaric damage
            - d20 (20 sides): D&D checks, attacks, saves
            - d100 (100 sides): Percentile tables, critical charts

            USAGE: Specify sides as a positive integer (e.g., sides=20 for d20).
            Returns a single random number between 1 and the number of sides (inclusive).
            """)
    public int rollDice(int sides) {
        if (sides < 1) {
            throw new IllegalArgumentException("Dice must have at least 1 side");
        }
        return (int) (Math.random() * sides) + 1;
    }

    @Tool(name = "ChatDM_likelihood", description = """
            Ask a yes/no question weighted by probability for more realistic outcomes.

            WHEN TO USE:
            - When outcomes should match fictional probability
            - Solo play for GM-less adjudication
            - Situations where simple yes/no is too random

            LIKELIHOOD OPTIONS:
            - 'almostCertain' (90% yes): Very likely to occur
            - 'likely' (70% yes): Probable outcome
            - 'fiftyFifty' (50% yes): Equally likely either way
            - 'unlikely' (20% yes): Improbable but possible
            - 'almostImpossible' (10% yes): Extremely rare occurrence

            EXAMPLES:
            - "Is there a blacksmith in this large city?" → almostCertain
            - "Does the merchant trust me after my good deed?" → likely
            - "Do I find anything valuable in this random house?" → fiftyFifty
            - "Is the ancient artifact still intact after centuries?" → unlikely
            - "Does the dragon spare me without reason?" → almostImpossible

            Returns nuanced answers like "Yes", "Yes, and...", "Yes, but...", "No", "No, and...", "No, but..."
            """)
    public String likelihood(String likelihood) {
        String[] answers = (String[]) likelihoodAnswers.get(likelihood);
        if (answers == null) {
            return "Invalid likelihood. Use: almostCertain, likely, fiftyFifty, unlikely, or almostImpossible";
        }
        return answers[(int) (Math.random() * answers.length)];
    }

    @Tool(name = "ChatDM_roll_multiple_dice", description = """
            Roll multiple dice at once using standard RPG dice notation.

            WHEN TO USE:
            - Damage rolls with multiple dice (e.g., "3d6" for fireball)
            - Advantage/disadvantage rolls in D&D ("2d20" take higher/lower)
            - Multiple attack rolls
            - Stat generation (e.g., "4d6" drop lowest)
            - Any situation requiring multiple dice of the same type

            DICE NOTATION:
            Format: XdY+Z or XdY-Z
            - X = number of dice to roll (1-100)
            - Y = number of sides per die (2-1000)
            - Z = modifier to add or subtract (optional, -1000 to +1000)

            EXAMPLES:
            - "3d6" → Roll 3 six-sided dice
            - "2d20" → Roll 2 twenty-sided dice (advantage/disadvantage)
            - "4d6+5" → Roll 4 six-sided dice and add 5
            - "1d8-2" → Roll 1 eight-sided die and subtract 2
            - "8d6" → Fireball damage at higher levels

            RETURNS:
            Detailed breakdown showing:
            - Individual die results
            - Sum of all dice
            - Modifier applied (if any)
            - Final total

            Example output for "3d6+2":
            "Rolls: [4, 6, 3] = 13 + 2 = 15"

            TIP: For advantage/disadvantage in D&D, roll "2d20" then take higher (advantage) or lower (disadvantage).
            """)
    public String rollMultipleDice(String diceNotation) {
        try {
            // Parse dice notation (e.g., "3d6+2", "2d20", "1d8-1")
            diceNotation = diceNotation.trim().toLowerCase();

            // Extract modifier if present
            int modifier = 0;
            String dicePartOnly = diceNotation;
            if (diceNotation.contains("+")) {
                String[] parts = diceNotation.split("\\+");
                dicePartOnly = parts[0];
                modifier = Integer.parseInt(parts[1].trim());
            } else if (diceNotation.contains("-") && diceNotation.lastIndexOf("-") > 0) {
                String[] parts = diceNotation.split("-");
                dicePartOnly = parts[0];
                modifier = -Integer.parseInt(parts[1].trim());
            }

            // Parse XdY format
            String[] diceParts = dicePartOnly.split("d");
            if (diceParts.length != 2) {
                return "Invalid dice notation. Use format: XdY or XdY+Z (e.g., '3d6', '2d20', '4d6+5')";
            }

            int numDice = Integer.parseInt(diceParts[0].trim());
            int numSides = Integer.parseInt(diceParts[1].trim());

            // Validation
            if (numDice < 1 || numDice > 100) {
                return "Error: Number of dice must be between 1 and 100";
            }
            if (numSides < 2 || numSides > 1000) {
                return "Error: Number of sides must be between 2 and 1000";
            }
            if (modifier < -1000 || modifier > 1000) {
                return "Error: Modifier must be between -1000 and +1000";
            }

            // Roll the dice
            StringBuilder rolls = new StringBuilder();
            rolls.append("[");
            int sum = 0;
            for (int i = 0; i < numDice; i++) {
                int roll = (int) (Math.random() * numSides) + 1;
                sum += roll;
                if (i > 0) rolls.append(", ");
                rolls.append(roll);
            }
            rolls.append("]");

            // Format output
            String result = "Rolls: " + rolls + " = " + sum;
            if (modifier != 0) {
                int total = sum + modifier;
                String modifierStr = (modifier > 0) ? "+" + modifier : String.valueOf(modifier);
                result += " " + modifierStr + " = " + total;
            }

            return result;

        } catch (NumberFormatException e) {
            return "Invalid dice notation. Use format: XdY or XdY+Z (e.g., '3d6', '2d20', '4d6+5')";
        } catch (Exception e) {
            return "Error parsing dice notation: " + e.getMessage();
        }
    }
}
