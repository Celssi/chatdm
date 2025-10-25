package fi.celssi.chatdm.ChatDM;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class BrambletrekPromptsOracle {

    @Tool(name = "brambletrek_start_adventure_prompt", description = """
            Get the structured prompt for starting a new Brambletrek adventure.
            This provides the complete flow for adventure selection, character setup, and journal initialization.
            Use this when a player wants to begin playing Brambletrek.
            """)
    public String getStartAdventurePrompt() {
        return """
                # START NEW BRAMBLETREK ADVENTURE PROMPT
                
                Follow this guided flow to start a Brambletrek adventure:
                
                ## 1. Welcome & Adventure Selection
                
                Display:
                ```
                🌲 Welcome to Brambletrek! Let's start your adventure.
                
                What type of adventure would you like to play?
                
                1. Standard Adventure (Core Rules - Journey through Hyhill)
                2. The Pumpkin Party (Module) [Resource ID: brambletrek-pumpkin]
                3. A Birthday of Wonders (Module) [Resource ID: brambletrek-birthday]
                4. The Warmth of the First Frost (Module) [Resource ID: brambletrek-frost]
                
                Please choose 1-4:
                ```
                
                ## 2. Character Selection
                
                After player chooses adventure, display:
                ```
                Great choice! Now, do you want to:
                
                1. Use an existing character
                2. Create a new character
                
                Please choose 1-2:
                ```
                
                ## 3. If Existing Character (Option 1)
                - Use ChatDM_list_characters to show saved characters
                - Ask player which character to use
                - Use ChatDM_load_character with the chosen character name
                
                ## 4. If New Character (Option 2)
                - Use brambletrek_create_character_prompt tool to get character creation flow
                - Follow that complete flow
                
                ## 5. Start the Adventure
                
                Once character is ready:
                
                a) Create adventure journal:
                   - Use ChatDM_start_adventure(adventureName, "brambletrek", characterNames, description)
                
                b) Get adventure introduction:
                   - Use ChatDM_search_resource or ChatDM_get_page with appropriate resource ID
                   - Display the adventure setup and starting location
                
                c) Log starting scenario:
                   - Use ChatDM_log_event to log: "Adventure begins! [Character] sets out from [location]..."
                
                d) Display initial status:
                   ```
                   📋 ADVENTURE: [Adventure Name]
                   🐭 CHARACTER: [Name]
                
                   Resources:
                   ❤️ Health: X/20
                   🎯 Morale: X/20
                   🎒 Supplies: X/20
                
                   Abilities Available: 4/4 uses today
                
                   🌅 DAY 1 - MORNING
                
                   [Adventure introduction and starting description]
                   ```
                
                e) Begin first encounter:
                   - Use brambletrek_play_encounter_prompt to get encounter resolution rules
                   - Draw first encounter card
                
                ## Key Tools to Use
                - ChatDM_list_characters
                - ChatDM_load_character
                - ChatDM_start_adventure
                - ChatDM_search_resource
                - ChatDM_get_page
                - ChatDM_log_event
                - ChatDM_draw_cards
                - brambletrek_create_character_prompt (for new characters)
                - brambletrek_play_encounter_prompt (for gameplay)
                
                Keep the narrative engaging and guide the player through each step!
                """;
    }

    @Tool(name = "brambletrek_create_character_prompt", description = """
            Get the structured prompt for Brambletrek character creation.
            This provides the complete flow with Legacy selection, card draws for background/trinket/resources, and saving.
            Use this when creating a new Brambletrek character.
            """)
    public String getCreateCharacterPrompt() {
        return """
                # CREATE BRAMBLETREK CHARACTER PROMPT
                
                Follow this guided flow for character creation:
                
                ## 1. Character Name
                
                Display:
                ```
                🐭 Let's create your Gnawborn character!
                
                What is your character's name?
                ```
                
                Wait for player response.
                
                ## 2. Legacy Selection
                
                Display:
                ```
                Choose your Legacy (or type 'random' to draw a card):
                
                1. The Seer - Mystical and wise (+5 Morale, -3 Health)
                2. The Scrapper - Tough and fearless (+5 Health, -3 Morale)
                3. The Storyteller - Inspiring and charismatic (+5 Morale, -3 Supplies)
                4. The Seeker - Curious and perceptive (+5 Supplies, -3 Morale)
                5. The Sneaker - Stealthy and quick (+5 Health, -3 Supplies)
                6. The Soother - Healing and supportive (+5 Health, -3 Morale)
                
                Choose 1-6 or type 'random':
                ```
                
                - If player chooses 'random': Use ChatDM_draw_cards(1, "standard") and map to 1-6 based on card
                - Use ChatDM_search_resource(query="Legacy abilities", resourceName="brambletrek-core") to get full details
                
                ## 3. Reason for Adventure (Card Draw)
                
                Display:
                ```
                Drawing a card to determine your Reason for Adventure...
                ```
                
                - Use ChatDM_draw_cards(1, "standard")
                - Use ChatDM_search_resource to look up the Reason table for that card
                - Display: `🎴 Drew: [Card Value] of [Suit] - [Full reason text from rules]`
                
                ## 4. Background (Card Draw)
                
                Display:
                ```
                Drawing a card to determine your Background...
                ```
                
                - Use ChatDM_draw_cards(1, "standard")
                - Use ChatDM_search_resource to look up the Background table
                - Display: `🎴 Drew: [Card Value] of [Suit] - [Full background text from rules]`
                
                ## 5. Trinket (Card Draw)
                
                Display:
                ```
                Drawing a card to determine your Trinket...
                ```
                
                - Use ChatDM_draw_cards(1, "standard")
                - Use ChatDM_search_resource to look up the Trinket table
                - Display: `🎴 Drew: [Card Value] of [Suit] - [Full trinket description from rules]`
                
                ## 6. Resources Calculation
                
                For EACH of Health, Morale, and Supplies:
                
                Display:
                ```
                ❤️ Drawing for Health...
                ```
                
                - Use ChatDM_draw_cards(2, "standard") to draw 2 cards
                - Calculate total: Ace=11, Jack/Queen/King=10, 2-10=face value
                - Display: `🎴 Drew: [Card1] ([value]) + [Card2] ([value]) = [total]`
                
                **SPECIAL RULE FOR HEALTH:**
                - If Health total ≤6, automatically draw a bonus card
                - Display: `⚠️ Low Health! Drawing bonus card... 🎴 Drew: [Card3] ([value])`
                - Add to Health total
                
                Repeat for Morale and Supplies (no bonus cards for these).
                
                ## 7. Apply Legacy Modifiers
                
                Add/subtract the Legacy modifiers:
                - Example: The Seer gives +5 Morale, -3 Health
                
                Display:
                ```
                ⚡ Applying [Legacy Name] modifiers...
                
                Final Resources:
                ❤️ Health: [base ± modifier]/20
                🎯 Morale: [base ± modifier]/20
                🎒 Supplies: [base ± modifier]/20
                ```
                
                ## 8. Display Complete Character
                
                Display:
                ```
                📋 CHARACTER CREATED: [Name]
                
                Legacy: [Legacy Name]
                Reason: [Brief summary of reason]
                Background: [Brief summary of background]
                Trinket: [Trinket name]
                
                Resources:
                ❤️ Health: X/20
                🎯 Morale: X/20
                🎒 Supplies: X/20
                
                [Legacy Name] Abilities (4 uses per day):
                • [Ability 1 name and description]
                • [Ability 2 name and description]
                • [Ability 3 name and description]
                • [Ability 4 name and description]
                ```
                
                Use ChatDM_search_resource to get accurate ability descriptions from brambletrek-core.
                
                ## 9. Save Character
                
                - Prepare complete character data with all details (name, legacy, stats, reason, background, trinket, abilities)
                - Use ChatDM_save_character(characterName, "brambletrek", characterData)
                - Display: `✅ Character saved successfully!`
                
                ## Important Notes
                - Always use ChatDM_draw_cards for card draws
                - Always use ChatDM_search_resource to look up tables and abilities from rules
                - Show each card draw dramatically with emoji
                - Calculate stats correctly: Ace=11, Face cards=10, others=face value
                - Health bonus card triggers when total ≤6
                - Save complete character data, not just basics
                
                Keep the process engaging with narrative flavor!
                """;
    }

    @Tool(name = "brambletrek_play_encounter_prompt", description = """
            Get the structured prompt for resolving Brambletrek encounters.
            This covers card draws, Overcome the Odds, combat, ability usage, and resource management.
            Use this during active Brambletrek gameplay.
            """)
    public String getPlayEncounterPrompt() {
        return """
                # BRAMBLETREK ENCOUNTER RESOLUTION PROMPT
                
                Follow these rules for encounter gameplay:
                
                ## Draw and Identify Encounter
                
                1. Use ChatDM_draw_cards(1, "standard") to draw encounter card
                2. Use ChatDM_search_resource to look up encounter for card and current region
                3. Display dramatically:
                   ```
                   🎴 Drew: [Value] of [Suit]
                
                   [Encounter description from rules]
                   ```
                
                ## Event Types
                
                - **Black cards (Clubs ♣ / Spades ♠)** = Unfortunate Events (negative)
                - **Red cards (Hearts ♥ / Diamonds ♦)** = Fortunate Events (positive)
                
                ## FOR POSITIVE EVENTS (Red Cards) - Auto-Resolve
                
                Display:
                ```
                🎴 Drew: [Value] of [Hearts/Diamonds]
                
                [Positive encounter description]
                
                Gained: +[X] [Health/Morale/Supplies]
                [Stat]: [old] → [new]/20
                ```
                
                - Apply benefits immediately
                - Log with ChatDM_log_event if significant
                - Continue to next action
                
                ## FOR NEGATIVE EVENTS (Black Cards) - Offer Overcome the Odds
                
                **BEFORE applying damage, display:**
                
                ```
                🎴 Drew: [Value] of [Clubs/Spades]
                
                [Negative encounter description]
                Potential loss: -[X] [Health/Morale/Supplies]
                
                This is an Unfortunate Event. Would you like to attempt to Overcome the Odds?
                
                ⚔️ OVERCOME THE ODDS:
                • Uses ONE of your 4 daily Legacy Ability slots
                • Draw Ability Card and Outcome Card
                • If Ability > Outcome: SUCCESS (avoid the event!)
                • If Ability = Ace (11): CRITICAL SUCCESS (GAIN the stat instead!)
                • If Ability = 2: CRITICAL FAILURE (lose DOUBLE!)
                • If Ability < Outcome: FAILURE (event happens normally)
                
                Abilities remaining today: [X]/4
                
                Attempt to Overcome the Odds? (yes/no)
                ```
                
                ### If Player Says YES:
                
                1. Check if abilities remain (max 4 per day including Overcome attempts)
                2. Use ChatDM_draw_cards(2, "standard") for Ability and Outcome cards
                3. Compare values: Ace=11, Face cards=10, 2-10=face value
                4. Resolve based on comparison:
                
                **SUCCESS (Ability > Outcome):**
                ```
                Drawing to Overcome the Odds...
                
                Ability Card: [Card] ([value])
                Outcome Card: [Card] ([value])
                
                [value] > [value] - SUCCESS!
                
                [Narrative: How character overcomes the challenge]
                
                Event Overcome: No damage taken!
                [Stat]: [current]/20 (unchanged)
                
                ⚡ Ability uses remaining today: [X-1]/4
                ```
                
                **CRITICAL SUCCESS (Ability = Ace):**
                ```
                Ability Card: Ace of [Suit] (11) ⭐
                Outcome Card: [Card] ([value])
                
                ACE - CRITICAL SUCCESS!
                
                [Narrative: Character turns negative into positive]
                
                Event Overcome: GAINED +[X] [stat] instead!
                [Stat]: [old] → [new]/20
                
                ⚡ Ability uses remaining today: [X-1]/4
                ```
                
                **CRITICAL FAILURE (Ability = 2):**
                ```
                Ability Card: 2 of [Suit] (2) ⚠️
                Outcome Card: [Card] ([value])
                
                2 - CRITICAL FAILURE!
                
                [Narrative: Attempt makes things worse]
                
                DOUBLE damage: -[X] → -[2X] [stat]!
                [Stat]: [old] → [new]/20
                
                ⚡ Ability uses remaining today: [X-1]/4
                ```
                
                **FAILURE (Ability < Outcome):**
                ```
                Ability Card: [Card] ([value])
                Outcome Card: [Card] ([value])
                
                [value] < [value] - FAILED!
                
                Despite your efforts, the challenge prevails.
                
                Event occurs as normal: -[X] [stat]
                [Stat]: [old] → [new]/20
                
                ⚡ Ability uses remaining today: [X-1]/4
                ```
                
                ### If Player Says NO or Overcame Failed:
                
                Apply damage/loss normally:
                ```
                Taking damage...
                
                Lost: -[X] [stat]
                [Stat]: [old] → [new]/20
                ```
                
                ## FOR COMBAT (Face Cards on Black Suits)
                
                ### Before Combat - Offer Overcome the Odds
                
                ```
                ⚔️ COMBAT ENCOUNTER: [Enemy Name]
                Type: [Enemy Type]
                Enemy Health: [X]/[X]
                
                This is an Unfortunate Event (black suit face card).
                
                Would you like to attempt to Overcome the Odds?
                • Success: Avoid combat entirely
                • Critical Success (Ace): Enemy flees and drops an item!
                • Critical Failure (2): Enemy gets a Surprise Turn before initiative
                
                Abilities remaining today: [X]/4
                
                Attempt to Overcome the Odds? (yes/no)
                ```
                
                ### If Combat Proceeds:
                
                **1. Initiative:**
                - Use ChatDM_draw_cards(2, "standard") for player and enemy
                - Higher card goes first
                - Display: `Drawing initiative... You: [Card] | Enemy: [Card]`
                
                **2. Deal Player Hand:**
                - Use ChatDM_draw_cards(4, "standard") to give player 4 tactic cards
                
                **3. Each Turn:**
                ```
                --- YOUR TURN [#] ---
                Your Health: [X]/20  |  Enemy Health: [X]/[X]
                
                Your hand:
                1. [Card] - [Tactic name and effect]
                2. [Card] - [Tactic name and effect]
                3. [Card] - [Tactic name and effect]
                4. [Card] - [Tactic name and effect]
                
                Which card do you play? (1-4)
                ```
                
                - Use ChatDM_search_resource to look up tactic effects
                - Apply player's chosen tactic
                - Enemy turn: Use ChatDM_draw_cards(1, "standard") and resolve
                - Continue until one reaches 0 Health
                
                ## Critical Resource Checks
                
                **When any stat drops below 30% (6 or less out of 20), display:**
                
                ```
                ⚠️ Your [Health/Morale/Supplies] is critically low! ([X]/20)
                
                Available abilities:
                • [Ability 1 name] - [effect] [✓ available / ✗ used today]
                • [Ability 2 name] - [effect] [✓ available / ✗ used today]
                ...
                
                Would you like to use an ability? (yes/no, or ability name)
                ```
                
                - Track which specific abilities have been used
                - Remember: Max 4 total ability uses per day (including Overcome the Odds)
                
                ## After Each Encounter
                
                Display:
                ```
                --- Encounter Complete ---
                
                Current Status:
                ❤️ Health: [X]/20
                🎯 Morale: [X]/20
                🎒 Supplies: [X]/20
                
                Abilities remaining today: [X]/4
                
                What would you like to do?
                • 'continue' - Draw next encounter
                • 'rest' - Spend 4 Supplies to restore 2 Health
                • 'check' - View full character sheet and abilities
                
                Your choice: _____
                ```
                
                - Use ChatDM_log_event for significant events
                - If player rests: Check Supplies ≥4, then restore 2 Health
                
                ## End of Day (After 4-6 Encounters or Major Rest)
                
                Display:
                ```
                🌙 NIGHT FALLS - End of Day [#]
                
                Day Summary:
                • [X] encounters completed
                • [X] combat victories
                • [Notable events]
                
                Current Resources:
                ❤️ Health: [X]/20
                🎯 Morale: [X]/20
                🎒 Supplies: [X]/20
                
                MANDATORY REST:
                Spending 4 Supplies...
                Restoring 2 Health...
                
                After Rest:
                ❤️ Health: [X]/20
                🎯 Morale: [X]/20
                🎒 Supplies: [X]/20
                
                ⚡ ALL ABILITIES RESET FOR TOMORROW:
                • [Ability 1] - Ready (4/4 available)
                • [Ability 2] - Ready
                • [Ability 3] - Ready
                • [Ability 4] - Ready
                
                Sleep well. Tomorrow brings new adventures...
                
                Ready for Day [#+1]? (yes)
                ```
                
                - Use ChatDM_log_event for day summary
                - Reset ability counter to 4/4
                
                ## Key Rules Summary
                
                1. **Overcome the Odds:**
                   - ONLY on black cards (Clubs/Spades)
                   - Uses 1 of 4 daily ability slots
                   - Offer BEFORE applying damage/combat
                
                2. **Ability Tracking:**
                   - 4 total uses per day
                   - Overcome the Odds counts as 1 use
                   - Each Legacy ability counts as 1 use
                   - Reset at end of day rest
                
                3. **Card Values:**
                   - Ace = 11
                   - Jack/Queen/King = 10
                   - 2-10 = Face value
                
                4. **Event Types:**
                   - Red (♥♦) = Auto-resolve positive
                   - Black (♣♠) = Offer Overcome, then resolve
                   - Black face cards = Combat (offer Overcome first)
                
                5. **Combat:**
                   - Can be avoided with successful Overcome the Odds
                   - Turn-by-turn with player choosing tactics
                   - Enemy draws automatically
                
                6. **Resource Management:**
                   - Warn at 30% (≤6)
                   - Quick rest: 4 Supplies → 2 Health
                   - End of day: Mandatory 4 Supplies → 2 Health + ability reset
                
                Always use ChatDM tools and keep narrative engaging!
                """;
    }
}
