package fi.celssi.chatdm.service;

import fi.celssi.chatdm.service.shared.PromptLoader;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class DnDPromptsOracle {

    private final PromptLoader promptLoader;

    public DnDPromptsOracle(PromptLoader promptLoader) {
        this.promptLoader = promptLoader;
    }

    @Tool(name = "dnd_start_adventure_prompt", description = """
            Get the structured prompt for starting a new D&D 5e 2024 adventure.
            """)
    public String getStartAdventurePrompt() {
        return promptLoader.load("dnd/start-adventure.md");
    }

    @Tool(name = "dnd_create_character_prompt", description = """
            Get the structured prompt for D&D 5e 2024 character creation.
            """)
    public String getCreateCharacterPrompt() {
        return promptLoader.load("dnd/create-character.md");
    }

    @Tool(name = "dnd_play_encounter_prompt", description = """
            Get the structured prompt for resolving D&D 5e 2024 encounters.
            """)
    public String getPlayEncounterPrompt() {
        return promptLoader.load("dnd/play-encounter.md");
    }

    @Tool(name = "dnd_combat_prompt", description = """
            Get structured guidance for running D&D 5e 2024 combat encounters.
            """)
    public String getCombatPrompt() {
        return promptLoader.load("dnd/combat.md");
    }

    @Tool(name = "dnd_spellcasting_prompt", description = """
            Get guidance for handling spellcasting in D&D 5e 2024.
            """)
    public String getSpellcastingPrompt() {
        return promptLoader.load("dnd/spellcasting.md");
    }

    @Tool(name = "dnd_exploration_prompt", description = """
            Get guidance for handling exploration and social interaction in D&D 5e 2024.
            """)
    public String getExplorationPrompt() {
        return promptLoader.load("dnd/exploration.md");
    }

    @Tool(name = "dnd_leveling_prompt", description = """
            Get guidance for leveling up characters in D&D 5e 2024.
            """)
    public String getLevelingPrompt() {
        return promptLoader.load("dnd/leveling.md");
    }

    @Tool(name = "dnd_dm_guide_prompt", description = """
            Get general DM guidance for running D&D 5e 2024 games.
            """)
    public String getDMGuidePrompt() {
        return promptLoader.load("dnd/dm-guide.md");
    }

    @Tool(name = "dnd_monster_running_prompt", description = """
            Get guidance for running monsters and NPCs in combat.
            """)
    public String getMonsterRunningPrompt() {
        return promptLoader.load("dnd/monster-running.md");
    }

}