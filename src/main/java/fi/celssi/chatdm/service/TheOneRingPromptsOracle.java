package fi.celssi.chatdm.service;

import fi.celssi.chatdm.service.shared.PromptLoader;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class TheOneRingPromptsOracle {

    private final PromptLoader promptLoader;

    public TheOneRingPromptsOracle(PromptLoader promptLoader) {
        this.promptLoader = promptLoader;
    }

    @Tool(name = "tor_start_adventure_prompt", description = """
            Get the structured prompt for starting a new The One Ring 2e adventure.
            """)
    public String getStartAdventurePrompt() {
        return promptLoader.load("tor/start-adventure.md");
    }

    @Tool(name = "tor_create_hero_prompt", description = """
            Get the structured prompt for The One Ring 2e hero creation.
            """)
    public String getCreateHeroPrompt() {
        return promptLoader.load("tor/create-hero.md");
    }

    @Tool(name = "tor_play_scene_prompt", description = """
            Get the structured prompt for resolving non-combat scenes in The One Ring 2e.
            """)
    public String getPlayScenePrompt() {
        return promptLoader.load("tor/play-scene.md");
    }

    @Tool(name = "tor_combat_prompt", description = """
            Get structured guidance for running The One Ring 2e combat, including a Strider Mode skirmish note.
            """)
    public String getCombatPrompt() {
        return promptLoader.load("tor/combat.md");
    }

    @Tool(name = "tor_journey_prompt", description = """
            Get structured guidance for running journeys in The One Ring 2e.
            """)
    public String getJourneyPrompt() {
        return promptLoader.load("tor/journey.md");
    }

    @Tool(name = "tor_council_prompt", description = """
            Get guidance for Councils and social encounters in The One Ring 2e.
            """)
    public String getCouncilPrompt() {
        return promptLoader.load("tor/council.md");
    }

    @Tool(name = "tor_fellowship_phase_prompt", description = """
            Get guidance for Fellowship Phases in The One Ring 2e.
            """)
    public String getFellowshipPhasePrompt() {
        return promptLoader.load("tor/fellowship-phase.md");
    }

    @Tool(name = "tor_strider_mode_prompt", description = """
            Get the Strider Mode quick reference for solo play in The One Ring 2e.
            """)
    public String getStriderModePrompt() {
        return promptLoader.load("tor/strider-mode.md");
    }

    @Tool(name = "tor_patron_prompt", description = """
            Get Patron selection guidance and a d6 list of mission seeds for each Patron.
            """)
    public String getPatronPrompt() {
        return promptLoader.load("tor/patron.md");
    }

    @Tool(name = "tor_tables_prompt", description = """
            Get compact solo tables: Telling yes or no, Lore sparks, and Fortune or Ill-fortune prompts.
            """)
    public String getTablesPrompt() {
        return promptLoader.load("tor/tables.md");
    }

    @Tool(name = "tor_oracle_howto_prompt", description = """
            Get a short how-to for asking oracle questions in solo play, with odds guidance and examples.
            """)
    public String getOracleHowToPrompt() {
        return promptLoader.load("tor/oracle-howto.md");
    }

    @Tool(name = "tor_xp_milestones_prompt", description = """
            Get a milestone checklist for awarding Skill and Adventure points in Strider Mode.
            """)
    public String getXPMilestonesPrompt() {
        return promptLoader.load("tor/xp-milestones.md");
    }

}