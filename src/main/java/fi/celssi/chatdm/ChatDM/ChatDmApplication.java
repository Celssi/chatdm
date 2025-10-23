package fi.celssi.chatdm.ChatDM;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class ChatDmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatDmApplication.class, args);
    }

    @Bean
    public List<ToolCallback> oracles(Oracle oracle,
                                       NpcOracle npcOracle,
                                       SceneOracle sceneOracle,
                                       NarrativeOracle narrativeOracle,
                                       DungeonOracle dungeonOracle,
                                       ConversationOracle conversationOracle) {
        return List.of(
                ToolCallbacks.from(oracle),
                ToolCallbacks.from(npcOracle),
                ToolCallbacks.from(sceneOracle),
                ToolCallbacks.from(narrativeOracle),
                ToolCallbacks.from(dungeonOracle),
                ToolCallbacks.from(conversationOracle)
        );
    }
}
