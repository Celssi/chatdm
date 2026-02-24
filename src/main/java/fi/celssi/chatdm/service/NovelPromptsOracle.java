package fi.celssi.chatdm.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class NovelPromptsOracle {

    @Tool(name = "novel_what_happens_next_prompt", description = """
            Get a prompt for suggesting what could happen in the next three paragraphs.
            Use after loading chapter/context with ChatDM_load_book_chapter.
            Parameters:
            - recentText: Optional. The last few paragraphs or scene context to build on
            - bookName: Optional. Book title for context
            - chapterIndex: Optional. Current chapter number
            """)
    public String getWhatHappensNextPrompt(String recentText, String bookName, Integer chapterIndex) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Suggest 3 possible directions for what could happen in the next three paragraphs of this novel.");
        prompt.append("\n\n");
        if (recentText != null && !recentText.trim().isEmpty()) {
            prompt.append("Recent context:\n---\n").append(recentText.trim()).append("\n---\n\n");
        }
        if (bookName != null && !bookName.trim().isEmpty()) {
            prompt.append("Book: ").append(bookName.trim());
            if (chapterIndex != null && chapterIndex >= 1) {
                prompt.append(", Chapter ").append(chapterIndex);
            }
            prompt.append("\n\n");
        }
        prompt.append("For each option, write 2-3 paragraphs that could naturally follow. ");
        prompt.append("Keep tone and style consistent. Vary the directions (e.g. action, revelation, dialogue, internal conflict).");
        return prompt.toString();
    }

    @Tool(name = "novel_character_dialogue_prompt", description = """
            Get a prompt for suggesting what a character might say next.
            Use after loading character bio with ChatDM_load_book_bio and chapter context.
            Parameters:
            - characterName: Required. The character who might speak
            - sceneContext: Optional. Current scene, recent dialogue, or situation
            - characterBio: Optional. Character bio (load with ChatDM_load_book_bio if available)
            """)
    public String getCharacterDialoguePrompt(String characterName, String sceneContext, String characterBio) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return "Error: Character name is required.";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Suggest what ").append(characterName.trim()).append(" might say next in this scene.");
        prompt.append("\n\n");
        if (characterBio != null && !characterBio.trim().isEmpty()) {
            prompt.append("Character bio:\n---\n").append(characterBio.trim()).append("\n---\n\n");
        }
        if (sceneContext != null && !sceneContext.trim().isEmpty()) {
            prompt.append("Scene context:\n---\n").append(sceneContext.trim()).append("\n---\n\n");
        }
        prompt.append("Provide 2-3 possible lines of dialogue that fit the character's voice and the situation. ");
        prompt.append("Include brief stage direction or tone notes if helpful.");
        return prompt.toString();
    }
}
