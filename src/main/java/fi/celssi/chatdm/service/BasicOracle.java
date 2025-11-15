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

    @Tool(
            name = "ChatDM_roll_multiple_dice",
            description = """
                    Roll multiple dice at once using standard RPG dice notation,
                    with optional modes for sum, advantage, or disadvantage behavior.
                    
                    PARAMETERS:
                    diceNotation (String)
                      - Standard RPG notation XdY+Z or XdY-Z
                      - X = number of dice (1–100)
                      - Y = sides per die (2–1000)
                      - Z = modifier (optional, -1000 to +1000)
                    
                    mode (String)
                      - Controls how the final result is chosen:
                        • "sum"     → Add all dice together (default)
                        • "highest" → Take the highest die (use for advantage)
                        • "lowest"  → Take the lowest die (use for disadvantage)
                    
                    WHEN TO USE:
                    - Damage rolls with multiple dice (e.g., "3d6")
                    - Advantage/disadvantage ("2d20" with mode "highest" or "lowest")
                    - Attack rolls
                    - Stat generation ("4d6" drop lowest manually)
                    - Any situation requiring multiple rolls of the same die type
                    
                    EXAMPLES:
                    - rollMultipleDice("3d6", "sum")
                    - rollMultipleDice("2d20+5", "highest")    (advantage)
                    - rollMultipleDice("2d20+5", "lowest")     (disadvantage)
                    - rollMultipleDice("4d6-2", "sum")
                    
                    RETURNS:
                    Detailed breakdown including:
                    - Individual die results
                    - Chosen result (sum, highest, or lowest)
                    - Modifier applied
                    - Final total
                    
                    TIP:
                    For D&D 5e advantage, use mode "highest".
                    For disadvantage, use mode "lowest".
                    """
    )
    public String rollMultipleDice(String diceNotation, String mode) {
        try {
            // Normalize input
            diceNotation = diceNotation.trim().toLowerCase();
            String modeNorm = (mode == null) ? "sum" : mode.trim().toLowerCase();

            // Validate / normalize mode
            if (!modeNorm.equals("sum") && !modeNorm.equals("highest") && !modeNorm.equals("lowest")) {
                return "Invalid mode. Use: 'sum', 'highest', or 'lowest'.";
            }

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
                return "Invalid dice notation. Use format: XdY or XdY+Z (e.g., '3d6', '2d20', '4d6+5').";
            }

            int numDice = Integer.parseInt(diceParts[0].trim());
            int numSides = Integer.parseInt(diceParts[1].trim());

            // Validation
            if (numDice < 1 || numDice > 100) {
                return "Error: Number of dice must be between 1 and 100.";
            }
            if (numSides < 2 || numSides > 1000) {
                return "Error: Number of sides must be between 2 and 1000.";
            }
            if (modifier < -1000 || modifier > 1000) {
                return "Error: Modifier must be between -1000 and +1000.";
            }

            // Roll the dice
            StringBuilder rolls = new StringBuilder();
            rolls.append("[");
            int sum = 0;
            int highest = Integer.MIN_VALUE;
            int lowest = Integer.MAX_VALUE;

            for (int i = 0; i < numDice; i++) {
                int roll = (int) (Math.random() * numSides) + 1;
                sum += roll;
                if (roll > highest) highest = roll;
                if (roll < lowest) lowest = roll;

                if (i > 0) rolls.append(", ");
                rolls.append(roll);
            }
            rolls.append("]");

            // Choose base result according to mode
            int baseResult;
            String modeLabel;
            switch (modeNorm) {
                case "highest":
                    baseResult = highest;
                    modeLabel = "highest";
                    break;
                case "lowest":
                    baseResult = lowest;
                    modeLabel = "lowest";
                    break;
                default:
                    baseResult = sum;
                    modeLabel = "sum";
                    break;
            }

            int total = baseResult + modifier;

            // Build result string
            StringBuilder result = new StringBuilder();
            result.append("Rolls: ").append(rolls);
            result.append(" → ").append(modeLabel).append(" = ").append(baseResult);

            if (modifier != 0) {
                String modifierStr = (modifier > 0) ? "+" + modifier : String.valueOf(modifier);
                result.append(" ").append(modifierStr).append(" = ").append(total);
            }

            return result.toString();

        } catch (NumberFormatException e) {
            return "Invalid dice notation. Use format: XdY or XdY+Z (e.g., '3d6', '2d20', '4d6+5').";
        } catch (Exception e) {
            return "Error parsing dice notation: " + e.getMessage();
        }
    }
}
