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
                - ChatDM_draw_playing_cards
                - brambletrek_create_character_prompt (for new characters)
                - brambletrek_play_encounter_prompt (for gameplay)
                
                Keep the narrative engaging and guide the player through each step!
