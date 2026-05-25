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
                
                - If player chooses 'random': Use ChatDM_draw_playing_cards(1) and map to 1-6 based on card
                - Use ChatDM_search_resource(query="Legacy abilities", resourceName="brambletrek-core") to get full details
                
                ## 3. Reason for Adventure (Card Draw)
                
                Display:
                ```
                Drawing a card to determine your Reason for Adventure...
                ```
                
                - Use ChatDM_draw_playing_cards(1)
                - Use ChatDM_search_resource to look up the Reason table for that card
                - Display: `🎴 Drew: [Card Value] of [Suit] - [Full reason text from rules]`
                
                ## 4. Background (Card Draw)
                
                Display:
                ```
                Drawing a card to determine your Background...
                ```
                
                - Use ChatDM_draw_playing_cards(1)
                - Use ChatDM_search_resource to look up the Background table
                - Display: `🎴 Drew: [Card Value] of [Suit] - [Full background text from rules]`
                
                ## 5. Trinket (Card Draw)
                
                Display:
                ```
                Drawing a card to determine your Trinket...
                ```
                
                - Use ChatDM_draw_playing_cards(1)
                - Use ChatDM_search_resource to look up the Trinket table
                - Display: `🎴 Drew: [Card Value] of [Suit] - [Full trinket description from rules]`
                
                ## 6. Resources Calculation
                
                For EACH of Health, Morale, and Supplies:
                
                Display:
                ```
                ❤️ Drawing for Health...
                ```
                
                - Use ChatDM_draw_playing_cards(2) to draw 2 cards
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
                - Always use ChatDM_draw_playing_cards for card draws
                - Always use ChatDM_search_resource to look up tables and abilities from rules
                - Show each card draw dramatically with emoji
                - Calculate stats correctly: Ace=11, Face cards=10, others=face value
                - Health bonus card triggers when total ≤6
                - Save complete character data, not just basics
                
                Keep the process engaging with narrative flavor!
