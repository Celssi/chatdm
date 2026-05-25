package fi.celssi.chatdm.service;

import fi.celssi.chatdm.service.shared.PromptLoader;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class BrambletrekPromptsOracle {

    private final PromptLoader promptLoader;

    public BrambletrekPromptsOracle(PromptLoader promptLoader) {
        this.promptLoader = promptLoader;
    }

    @Tool(name = "brambletrek_start_adventure_prompt", description = """
            Get the structured prompt for starting a new Brambletrek adventure.
            """)
    public String getStartAdventurePrompt() {
        return promptLoader.load("brambletrek/start-adventure.md");
    }

    @Tool(name = "brambletrek_create_character_prompt", description = """
            Get the structured prompt for Brambletrek character creation.
            """)
    public String getCreateCharacterPrompt() {
        return promptLoader.load("brambletrek/create-character.md");
    }

    @Tool(name = "brambletrek_play_encounter_prompt", description = """
            Get the structured prompt for resolving Brambletrek encounters.
            """)
    public String getPlayEncounterPrompt() {
        return promptLoader.load("brambletrek/play-encounter.md");
    }

}