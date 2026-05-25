package fi.celssi.chatdm;

import fi.celssi.chatdm.service.*;
import fi.celssi.chatdm.service.wryterio.*;
import fi.celssi.chatdm.service.journal.*;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
public class ChatDmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatDmApplication.class, args);
    }

    @Value("${chatdm.wryterio.connect-timeout:10}")
    private int wryterioConnectTimeoutSeconds;

    @Value("${chatdm.wryterio.read-timeout:120}")
    private int wryterioReadTimeoutSeconds;

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(wryterioConnectTimeoutSeconds));
        factory.setConnectionRequestTimeout(Duration.ofSeconds(wryterioConnectTimeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(wryterioReadTimeoutSeconds));
        return new RestTemplate(factory);
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
                CharacterJournalTools characterJournalTools,
                AdventureJournalTools adventureJournalTools,
                NpcJournalTools npcJournalTools,
                LocationJournalTools locationJournalTools,
                CampaignJournalTools campaignJournalTools,
                NovelOracle novelOracle,
                NovelPromptsOracle novelPromptsOracle,
                WryterioBookTools wryterioBookTools,
                WryterioChapterTools wryterioChapterTools,
                WryterioStoryElementTools wryterioStoryElementTools,
                WryterioSyncTools wryterioSyncTools,
                BrambletrekPromptsOracle brambletrekPromptsOracle,
                MyLittlePonyPromptsOracle myLittlePonyPromptsOracle,
                DnDPromptsOracle dndPromptsOracle,
                TheOneRingPromptsOracle theOneRingPromptsOracle,
                WritingTools writingTools) {
            return MethodToolCallbackProvider
                    .builder()
                    .toolObjects(dungeonOracle, narrativeOracle, npcOracle, sceneOracle,
                            basicOracle, conversationOracle, cardOracle, gameResourceOracle,
                            characterJournalTools, adventureJournalTools, npcJournalTools,
                            locationJournalTools, campaignJournalTools,
                            novelOracle, novelPromptsOracle,
                            wryterioBookTools, wryterioChapterTools, wryterioStoryElementTools, wryterioSyncTools,
                            brambletrekPromptsOracle, myLittlePonyPromptsOracle,
                            dndPromptsOracle, theOneRingPromptsOracle, writingTools)
                    .build();
        }
    }
}
