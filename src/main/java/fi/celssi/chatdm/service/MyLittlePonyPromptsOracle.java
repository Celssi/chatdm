package fi.celssi.chatdm.service;

import fi.celssi.chatdm.service.shared.PromptLoader;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class MyLittlePonyPromptsOracle {

    private final PromptLoader promptLoader;

    public MyLittlePonyPromptsOracle(PromptLoader promptLoader) {
        this.promptLoader = promptLoader;
    }

    @Tool(name = "mlp_start_adventure_prompt", description = """
            Get the structured prompt for starting a new My Little Pony adventure.
            """)
    public String getStartAdventurePrompt() {
        return promptLoader.load("mlp/start-adventure.md");
    }

    @Tool(name = "mlp_create_character_prompt", description = """
            Get the structured prompt for My Little Pony character creation.
            """)
    public String getCreateCharacterPrompt() {
        return promptLoader.load("mlp/create-character.md");
    }

    @Tool(name = "mlp_play_encounter_prompt", description = """
            Get the structured prompt for resolving My Little Pony encounters.
            """)
    public String getPlayEncounterPrompt() {
        return promptLoader.load("mlp/play-encounter.md");
    }

    @Tool(name = "mlp_tone_prompt", description = """
            Get guidance on maintaining the proper tone and narrative style for My Little Pony.
            """)
    public String getTonePrompt() {
        return promptLoader.load("mlp/tone.md");
    }

    @Tool(name = "mlp_friendship_points_prompt", description = """
            Get guidance on managing Friendship Points during My Little Pony gameplay.
            """)
    public String getFriendshipPointsPrompt() {
        return promptLoader.load("mlp/friendship-points.md");
    }

    @Tool(name = "mlp_conflict_prompt", description = """
            Get structured guidance for resolving Challenges and Conflicts in My Little Pony RPG.
            """)
    public String getConflictPrompt() {
        return promptLoader.load("mlp/conflict.md");
    }

    @Tool(name = "mlp_magic_prompt", description = """
            Get the structured prompt for handling Unicorn spellcasting and magical actions.
            """)
    public String getMagicPrompt() {
        return promptLoader.load("mlp/magic.md");
    }

    @Tool(name = "mlp_friendship_circle_prompt", description = """
            Guide for forming and resolving Friendship Circles in My Little Pony RPG.
            """)
    public String getFriendshipCirclePrompt() {
        return promptLoader.load("mlp/friendship-circle.md");
    }

    @Tool(name = "mlp_level_progression_prompt", description = """
            Guidance for tracking levels and perks in My Little Pony Essence20 gameplay.
            """)
    public String getLevelProgressionPrompt() {
        return promptLoader.load("mlp/level-progression.md");
    }

    @Tool(name = "mlp_session_wrap_prompt", description = """
            Prompt for ending a My Little Pony session with reflection and friendship lessons.
            """)
    public String getSessionWrapPrompt() {
        return promptLoader.load("mlp/session-wrap.md");
    }

}