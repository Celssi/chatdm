package fi.celssi.chatdm.service.shared;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PromptLoader {

    public String load(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/" + resourcePath);
            if (!resource.exists()) {
                throw new IllegalStateException("Prompt resource not found: prompts/" + resourcePath);
            }
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load prompt: prompts/" + resourcePath, e);
        }
    }
}
