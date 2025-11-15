package fi.celssi.chatdm.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class WritingTools {

    @Tool(name = "ChatDM_get_text_length", description = "Use this tool to get the length of the text in words.")
    public int textLength(String text) {
        return text.split(" ").length;
    }
    
}
