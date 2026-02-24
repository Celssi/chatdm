package fi.celssi.chatdm;

import fi.celssi.chatdm.service.*;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ChatDmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatDmApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Configuration
    public static class McpConfig {

        @Bean
        public ToolCallbackProvider toolCallbackProvider(
                DungeonOracle dungeonOracle,
                NarrativeOracle narrativeOracle,
                NpcOracle npcOracle,
                SceneOracle sceneOracle,
                BasicOracle basicOracle,
                ConversationOracle conversationOracle,
                CardOracle cardOracle,
                GameResourceOracle gameResourceOracle,
                JournalOracle journalOracle,
                NovelOracle novelOracle,
                NovelPromptsOracle novelPromptsOracle,
                WryterioOracle wryterioOracle,
                BrambletrekPromptsOracle brambletrekPromptsOracle,
                MyLittlePonyPromptsOracle myLittlePonyPromptsOracle,
                DnDPromptsOracle dndPromptsOracle,
                TheOneRingPromptsOracle theOneRingPromptsOracle,
                WritingTools writingTools) {
            return MethodToolCallbackProvider
                    .builder()
                    .toolObjects(dungeonOracle, narrativeOracle, npcOracle, sceneOracle,
                            basicOracle, conversationOracle, cardOracle, gameResourceOracle,
                            journalOracle, novelOracle, novelPromptsOracle, wryterioOracle, brambletrekPromptsOracle, myLittlePonyPromptsOracle,
                            dndPromptsOracle, theOneRingPromptsOracle, writingTools)
                    .build();
        }
    }
}
