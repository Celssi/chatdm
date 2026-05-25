package fi.celssi.chatdm.service;

import fi.celssi.chatdm.service.shared.PromptLoader;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class NovelPromptsOracle {

    private final PromptLoader promptLoader;

    public NovelPromptsOracle(PromptLoader promptLoader) {
        this.promptLoader = promptLoader;
    }

    @Tool(name = "novel_what_happens_next_prompt", description = """
            Get a prompt for suggesting what could happen in the next three paragraphs.
            WORKFLOW: Call ChatDM_load_book_chapter first; pass the loaded content (or last paragraphs) as recentText.
            Returns a prompt string—pass it to the LLM to generate suggestions.
            Parameters:
            - recentText: Optional. The last few paragraphs or scene context to build on (from ChatDM_load_book_chapter)
            - bookName: Optional. Book title for context
            - chapterIndex: Optional. Current chapter number
            """)
    public String getWhatHappensNextPrompt(String recentText, String bookName, Integer chapterIndex) {
        StringBuilder prompt = new StringBuilder(promptLoader.load("novel/what-happens-next.md"));
        if (recentText != null && !recentText.trim().isEmpty()) {
            prompt.insert(0, "Recent context:\n---\n" + recentText.trim() + "\n---\n\n");
        }
        if (bookName != null && !bookName.trim().isEmpty()) {
            String header = "Book: " + bookName.trim();
            if (chapterIndex != null && chapterIndex >= 1) {
                header += ", Chapter " + chapterIndex;
            }
            prompt.insert(0, header + "\n\n");
        }
        return prompt.toString();
    }

    @Tool(name = "novel_character_dialogue_prompt", description = """
            Get a prompt for suggesting what a character might say next.
            WORKFLOW: Load character bio with ChatDM_load_book_bio first if available; pass as characterBio.
            Returns a prompt string—pass it to the LLM to generate dialogue suggestions.
            Parameters:
            - characterName: Required. The character who might speak
            - sceneContext: Optional. Current scene, recent dialogue, or situation (e.g. from ChatDM_load_book_chapter)
            - characterBio: Optional. Character bio (load with ChatDM_load_book_bio if available)
            """)
    public String getCharacterDialoguePrompt(String characterName, String sceneContext, String characterBio) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return "Error: Character name is required.";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Suggest what ").append(characterName.trim()).append(" might say next in this scene.\n\n");
        if (characterBio != null && !characterBio.trim().isEmpty()) {
            prompt.append("Character bio:\n---\n").append(characterBio.trim()).append("\n---\n\n");
        }
        if (sceneContext != null && !sceneContext.trim().isEmpty()) {
            prompt.append("Scene context:\n---\n").append(sceneContext.trim()).append("\n---\n\n");
        }
        prompt.append(promptLoader.load("novel/character-dialogue.md"));
        return prompt.toString();
    }
}
