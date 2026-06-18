# ChatDM MCP Tools

Auto-generated catalog of **91** tools.

| Tool | Description | Source |
|------|-------------|--------|
| `ChatDM_action` | Generate a random action or verb for narrative prompts. | `fi/celssi/chatdm/service/NarrativeOracle.java` |
| `ChatDM_action_subject` | Generate combined action and subject for instant narrative prompts. | `fi/celssi/chatdm/service/NarrativeOracle.java` |
| `ChatDM_atmosphere` | Generate the atmosphere or mood of a location or scene. | `fi/celssi/chatdm/service/SceneOracle.java` |
| `ChatDM_complication` | Add a complication or twist to escalate the current situation. | `fi/celssi/chatdm/service/NarrativeOracle.java` |
| `ChatDM_conversation_intent` | Determine what an NPC is trying to achieve or communicate in conversation. | `fi/celssi/chatdm/service/ConversationOracle.java` |
| `ChatDM_conversation_mood` | Determine the emotional tone or attitude of an NPC during conversation. | `fi/celssi/chatdm/service/ConversationOracle.java` |
| `ChatDM_conversation_setup` | Generate complete conversation context with topic, mood, and intent at once. | `fi/celssi/chatdm/service/ConversationOracle.java` |
| `ChatDM_conversation_topic` | Generate what an NPC wants to discuss or what subject arises in conversation. | `fi/celssi/chatdm/service/ConversationOracle.java` |
| `ChatDM_delete_adventure` | Delete an adventure journal by name (removes the latest file for that adventure name). | `fi/celssi/chatdm/service/journal/AdventureJournalTools.java` |
| `ChatDM_delete_campaign` | Delete a campaign journal. | `fi/celssi/chatdm/service/journal/CampaignJournalTools.java` |
| `ChatDM_delete_character` | Delete a saved character. | `fi/celssi/chatdm/service/journal/CharacterJournalTools.java` |
| `ChatDM_delete_location` | Delete a location from the campaign catalog. | `fi/celssi/chatdm/service/journal/LocationJournalTools.java` |
| `ChatDM_delete_npc` | Delete an NPC from the campaign catalog. | `fi/celssi/chatdm/service/journal/NpcJournalTools.java` |
| `ChatDM_descriptor` | Generate a random descriptor or adjective for enhanced detail. | `fi/celssi/chatdm/service/NarrativeOracle.java` |
| `ChatDM_direction` | Generate a random direction for navigation or events. | `fi/celssi/chatdm/service/DungeonOracle.java` |
| `ChatDM_draw_playing_cards` | Draw playing cards from a standard 52-card deck for fortune-telling or random inspiration. | `fi/celssi/chatdm/service/CardOracle.java` |
| `ChatDM_draw_tarot_cards` | Draw tarot cards from a 78-card deck for divination and narrative inspiration. | `fi/celssi/chatdm/service/CardOracle.java` |
| `ChatDM_dungeon_room` | Generate dungeon room contents for exploration and discovery. | `fi/celssi/chatdm/service/DungeonOracle.java` |
| `ChatDM_generate_npc` | Generate a complete NPC with appearance, personality, occupation, and current behavior. | `fi/celssi/chatdm/service/NpcOracle.java` |
| `ChatDM_get_page` | Read the full text from a specific page of an RPG rulebook or supplement. | `fi/celssi/chatdm/service/GameResourceOracle.java` |
| `ChatDM_likelihood` | Ask a yes/no question weighted by probability for more realistic outcomes. | `fi/celssi/chatdm/service/BasicOracle.java` |
| `ChatDM_list_adventures` | List all adventure journals. | `fi/celssi/chatdm/service/journal/AdventureJournalTools.java` |
| `ChatDM_list_adventures_by_campaign` | List all adventure journals that belong to a specific campaign. | `fi/celssi/chatdm/service/journal/AdventureJournalTools.java` |
| `ChatDM_list_campaigns` | List all campaign journals. | `fi/celssi/chatdm/service/journal/CampaignJournalTools.java` |
| `ChatDM_list_characters` | List all saved characters, optionally filtered by campaign. | `fi/celssi/chatdm/service/journal/CharacterJournalTools.java` |
| `ChatDM_list_locations` | List all locations for a specific campaign or all locations. | `fi/celssi/chatdm/service/journal/LocationJournalTools.java` |
| `ChatDM_list_npcs` | List all NPCs for a specific campaign or all NPCs. | `fi/celssi/chatdm/service/journal/NpcJournalTools.java` |
| `ChatDM_list_resources` | List all available RPG rulebooks, adventures, and supplements across all game systems. | `fi/celssi/chatdm/service/GameResourceOracle.java` |
| `ChatDM_load_campaign` | Load the campaign journal. | `fi/celssi/chatdm/service/journal/CampaignJournalTools.java` |
| `ChatDM_load_character` | Load a previously saved character from file. | `fi/celssi/chatdm/service/journal/CharacterJournalTools.java` |
| `ChatDM_load_location` | Load a location from the catalog to maintain consistency. | `fi/celssi/chatdm/service/journal/LocationJournalTools.java` |
| `ChatDM_load_npc` | Load an NPC from the catalog to maintain consistency. | `fi/celssi/chatdm/service/journal/NpcJournalTools.java` |
| `ChatDM_location_type` | Generate a random location type for scene setting. | `fi/celssi/chatdm/service/SceneOracle.java` |
| `ChatDM_log_event` | Log an event to the current adventure journal. | `fi/celssi/chatdm/service/journal/AdventureJournalTools.java` |
| `ChatDM_npc_appearance` | Generate a distinctive physical appearance trait for an NPC. | `fi/celssi/chatdm/service/NpcOracle.java` |
| `ChatDM_npc_behavior` | Generate how an NPC behaves or reacts in the current situation. | `fi/celssi/chatdm/service/NpcOracle.java` |
| `ChatDM_npc_occupation` | Determine an NPC's profession, role, or place in society. | `fi/celssi/chatdm/service/NpcOracle.java` |
| `ChatDM_npc_personality` | Generate a core personality trait for an NPC. | `fi/celssi/chatdm/service/NpcOracle.java` |
| `ChatDM_random_event` | Generate a random event to inject into the current scene. | `fi/celssi/chatdm/service/NarrativeOracle.java` |
| `ChatDM_read_adventure` | Read a single adventure journal by name. | `fi/celssi/chatdm/service/journal/AdventureJournalTools.java` |
| `ChatDM_roll_dice` | Roll a dice with any number of sides for game mechanics. | `fi/celssi/chatdm/service/BasicOracle.java` |
| `ChatDM_save_campaign` | Save or update the campaign journal for an adventure. | `fi/celssi/chatdm/service/journal/CampaignJournalTools.java` |
| `ChatDM_save_character` | Save a character to a text file for reuse across adventures. | `fi/celssi/chatdm/service/journal/CharacterJournalTools.java` |
| `ChatDM_save_location` | Save a location to the catalog for a campaign. | `fi/celssi/chatdm/service/journal/LocationJournalTools.java` |
| `ChatDM_save_npc` | Save an NPC to the catalog for a campaign. | `fi/celssi/chatdm/service/journal/NpcJournalTools.java` |
| `ChatDM_scene_setup` | Generate a complete scene setup with all environmental elements at once. | `fi/celssi/chatdm/service/SceneOracle.java` |
| `ChatDM_search_resource` | Search for rules, lore, or mechanics within RPG rulebooks and supplements. | `fi/celssi/chatdm/service/GameResourceOracle.java` |
| `ChatDM_start_adventure` | Start a new adventure journal. | `fi/celssi/chatdm/service/journal/AdventureJournalTools.java` |
| `ChatDM_subject` | Generate a random subject or noun for narrative focus. | `fi/celssi/chatdm/service/NarrativeOracle.java` |
| `ChatDM_time_of_day` | Determine the current time of day for time-sensitive scenes. | `fi/celssi/chatdm/service/SceneOracle.java` |
| `ChatDM_trap` | Generate trap types for hazardous locations. | `fi/celssi/chatdm/service/DungeonOracle.java` |
| `ChatDM_treasure` | Generate treasure or valuable items as rewards. | `fi/celssi/chatdm/service/DungeonOracle.java` |
| `ChatDM_update_character` | Update an existing saved character. | `fi/celssi/chatdm/service/journal/CharacterJournalTools.java` |
| `ChatDM_update_location` | Update an existing location in the catalog. | `fi/celssi/chatdm/service/journal/LocationJournalTools.java` |
| `ChatDM_update_npc` | Update an existing NPC in the catalog. | `fi/celssi/chatdm/service/journal/NpcJournalTools.java` |
| `ChatDM_weather` | Generate current weather conditions for outdoor scenes. | `fi/celssi/chatdm/service/SceneOracle.java` |
| `ChatDM_yes_or_no` | Ask a binary yes/no question to determine uncertain outcomes in the game world. | `fi/celssi/chatdm/service/BasicOracle.java` |
| `brambletrek_create_character_prompt` | Get the structured prompt for Brambletrek character creation. | `fi/celssi/chatdm/service/BrambletrekPromptsOracle.java` |
| `brambletrek_play_encounter_prompt` | Get the structured prompt for resolving Brambletrek encounters. | `fi/celssi/chatdm/service/BrambletrekPromptsOracle.java` |
| `brambletrek_start_adventure_prompt` | Get the structured prompt for starting a new Brambletrek adventure. | `fi/celssi/chatdm/service/BrambletrekPromptsOracle.java` |
| `dnd_combat_prompt` | Get structured guidance for running D&D 5e 2024 combat encounters. | `fi/celssi/chatdm/service/DnDPromptsOracle.java` |
| `dnd_create_character_prompt` | Get the structured prompt for D&D 5e 2024 character creation. | `fi/celssi/chatdm/service/DnDPromptsOracle.java` |
| `dnd_dm_guide_prompt` | Get general DM guidance for running D&D 5e 2024 games. | `fi/celssi/chatdm/service/DnDPromptsOracle.java` |
| `dnd_exploration_prompt` | Get guidance for handling exploration and social interaction in D&D 5e 2024. | `fi/celssi/chatdm/service/DnDPromptsOracle.java` |
| `dnd_leveling_prompt` | Get guidance for leveling up characters in D&D 5e 2024. | `fi/celssi/chatdm/service/DnDPromptsOracle.java` |
| `dnd_monster_running_prompt` | Get guidance for running monsters and NPCs in combat. | `fi/celssi/chatdm/service/DnDPromptsOracle.java` |
| `dnd_play_encounter_prompt` | Get the structured prompt for resolving D&D 5e 2024 encounters. | `fi/celssi/chatdm/service/DnDPromptsOracle.java` |
| `dnd_spellcasting_prompt` | Get guidance for handling spellcasting in D&D 5e 2024. | `fi/celssi/chatdm/service/DnDPromptsOracle.java` |
| `dnd_start_adventure_prompt` | Get the structured prompt for starting a new D&D 5e 2024 adventure. | `fi/celssi/chatdm/service/DnDPromptsOracle.java` |
| `mlp_conflict_prompt` | Get structured guidance for resolving Challenges and Conflicts in My Little Pony RPG. | `fi/celssi/chatdm/service/MyLittlePonyPromptsOracle.java` |
| `mlp_create_character_prompt` | Get the structured prompt for My Little Pony character creation. | `fi/celssi/chatdm/service/MyLittlePonyPromptsOracle.java` |
| `mlp_friendship_circle_prompt` | Guide for forming and resolving Friendship Circles in My Little Pony RPG. | `fi/celssi/chatdm/service/MyLittlePonyPromptsOracle.java` |
| `mlp_friendship_points_prompt` | Get guidance on managing Friendship Points during My Little Pony gameplay. | `fi/celssi/chatdm/service/MyLittlePonyPromptsOracle.java` |
| `mlp_level_progression_prompt` | Guidance for tracking levels and perks in My Little Pony Essence20 gameplay. | `fi/celssi/chatdm/service/MyLittlePonyPromptsOracle.java` |
| `mlp_magic_prompt` | Get the structured prompt for handling Unicorn spellcasting and magical actions. | `fi/celssi/chatdm/service/MyLittlePonyPromptsOracle.java` |
| `mlp_play_encounter_prompt` | Get the structured prompt for resolving My Little Pony encounters. | `fi/celssi/chatdm/service/MyLittlePonyPromptsOracle.java` |
| `mlp_session_wrap_prompt` | Prompt for ending a My Little Pony session with reflection and friendship lessons. | `fi/celssi/chatdm/service/MyLittlePonyPromptsOracle.java` |
| `mlp_start_adventure_prompt` | Get the structured prompt for starting a new My Little Pony adventure. | `fi/celssi/chatdm/service/MyLittlePonyPromptsOracle.java` |
| `mlp_tone_prompt` | Get guidance on maintaining the proper tone and narrative style for My Little Pony. | `fi/celssi/chatdm/service/MyLittlePonyPromptsOracle.java` |
| `tor_combat_prompt` | Get structured guidance for running The One Ring 2e combat, including a Strider Mode skirmish note. | `fi/celssi/chatdm/service/TheOneRingPromptsOracle.java` |
| `tor_council_prompt` | Get guidance for Councils and social encounters in The One Ring 2e. | `fi/celssi/chatdm/service/TheOneRingPromptsOracle.java` |
| `tor_create_hero_prompt` | Get the structured prompt for The One Ring 2e hero creation. | `fi/celssi/chatdm/service/TheOneRingPromptsOracle.java` |
| `tor_fellowship_phase_prompt` | Get guidance for Fellowship Phases in The One Ring 2e. | `fi/celssi/chatdm/service/TheOneRingPromptsOracle.java` |
| `tor_journey_prompt` | Get structured guidance for running journeys in The One Ring 2e. | `fi/celssi/chatdm/service/TheOneRingPromptsOracle.java` |
| `tor_oracle_howto_prompt` | Get a short how-to for asking oracle questions in solo play, with odds guidance and examples. | `fi/celssi/chatdm/service/TheOneRingPromptsOracle.java` |
| `tor_patron_prompt` | Get Patron selection guidance and a d6 list of mission seeds for each Patron. | `fi/celssi/chatdm/service/TheOneRingPromptsOracle.java` |
| `tor_play_scene_prompt` | Get the structured prompt for resolving non-combat scenes in The One Ring 2e. | `fi/celssi/chatdm/service/TheOneRingPromptsOracle.java` |
| `tor_start_adventure_prompt` | Get the structured prompt for starting a new The One Ring 2e adventure. | `fi/celssi/chatdm/service/TheOneRingPromptsOracle.java` |
| `tor_strider_mode_prompt` | Get the Strider Mode quick reference for solo play in The One Ring 2e. | `fi/celssi/chatdm/service/TheOneRingPromptsOracle.java` |
| `tor_tables_prompt` | Get compact solo tables: Telling yes or no, Lore sparks, and Fortune or Ill-fortune prompts. | `fi/celssi/chatdm/service/TheOneRingPromptsOracle.java` |
| `tor_xp_milestones_prompt` | Get a milestone checklist for awarding Skill and Adventure points in Strider Mode. | `fi/celssi/chatdm/service/TheOneRingPromptsOracle.java` |
