package fi.celssi.chatdm.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class WritingTools {

    @Tool(name = "ChatDM_get_text_length", description = """
            Count words in text. Returns word count and character count.
            Parameters:
            - text: Required. Text to analyze
            """)
    public String textLength(String text) {
        if (text == null || text.isBlank()) {
            return "Error: Text is required";
        }
        String trimmed = text.trim();
        int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
        return String.format("Words: %d, Characters: %d", words, trimmed.length());
    }

    @Tool(name = "ChatDM_compare_word_count", description = """
            Compare text word count to a target (e.g. chapter targetWordCount from Wryterio).
            Parameters:
            - text: Required. Text to count
            - targetWordCount: Required. Target number of words
            """)
    public String compareWordCount(String text, int targetWordCount) {
        if (text == null || text.isBlank()) {
            return "Error: Text is required";
        }
        if (targetWordCount < 1) {
            return "Error: targetWordCount must be >= 1";
        }
        int words = text.trim().split("\\s+").length;
        int delta = words - targetWordCount;
        String status = delta >= 0 ? "at or above target" : "below target";
        return String.format("Words: %d, Target: %d, Delta: %+d (%s)", words, targetWordCount, delta, status);
    }
}
