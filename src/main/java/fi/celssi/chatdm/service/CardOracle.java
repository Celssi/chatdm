package fi.celssi.chatdm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CardOracle {

    private String[] playingCards;
    private String[] tarotCards;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String[]> data;
        TypeReference<Map<String, String[]>> typeRef = new TypeReference<>() {
        };

        // Load playing cards
        ClassPathResource resource = new ClassPathResource("playing-cards-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        playingCards = data.get("playingCards");

        // Load tarot cards
        resource = new ClassPathResource("tarot-cards-oracle.json");
        data = mapper.readValue(resource.getInputStream(), typeRef);
        tarotCards = data.get("tarotCards");
    }

    @Tool(name = "ChatDM_draw_playing_cards", description = "Draw random playing cards from a standard 52-card deck. Specify how many cards to draw (1-52). Cards are drawn without replacement (no duplicates).")
    public String drawPlayingCards(int count) {
        if (count < 1 || count > playingCards.length) {
            return "Invalid count. Please specify a number between 1 and " + playingCards.length;
        }

        // Create a shuffled copy and draw the requested number of cards
        List<String> deck = new ArrayList<>(Arrays.asList(playingCards));
        Collections.shuffle(deck);
        List<String> drawnCards = deck.subList(0, count);

        if (count == 1) {
            return drawnCards.get(0);
        } else {
            return String.join(", ", drawnCards);
        }
    }

    @Tool(name = "ChatDM_draw_tarot_cards", description = "Draw random tarot cards from a 78-card tarot deck (22 Major Arcana + 56 Minor Arcana). Specify how many cards to draw (1-78). Cards are drawn without replacement (no duplicates).")
    public String drawTarotCards(int count) {
        if (count < 1 || count > tarotCards.length) {
            return "Invalid count. Please specify a number between 1 and " + tarotCards.length;
        }

        // Create a shuffled copy and draw the requested number of cards
        List<String> deck = new ArrayList<>(Arrays.asList(tarotCards));
        Collections.shuffle(deck);
        List<String> drawnCards = deck.subList(0, count);

        if (count == 1) {
            return drawnCards.get(0);
        } else {
            return String.join(", ", drawnCards);
        }
    }
}
