package fi.celssi.chatdm.ChatDM;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class ChatDmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatDmApplication.class, args);
    }

    @Configuration
    public static class McpConfig {

        @Bean
        public ToolCallbackProvider toolCallbackProvider(DungeonOracle dungeonOracle, NarrativeOracle narrativeOracle, NpcOracle npcOracle, SceneOracle sceneOracle, BasicOracle basicOracle, ConversationOracle conversationOracle, CardOracle cardOracle, ResourceOracle resourceOracle) {
            return MethodToolCallbackProvider
                    .builder()
                    .toolObjects(dungeonOracle, narrativeOracle, npcOracle, sceneOracle, basicOracle, conversationOracle, cardOracle, resourceOracle)
                    .build();
        }
    }
}
