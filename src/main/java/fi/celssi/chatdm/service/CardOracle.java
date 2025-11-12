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

    @Tool(name = "ChatDM_draw_playing_cards", description = """
            Draw playing cards from a standard 52-card deck for fortune-telling or random inspiration.

            WHEN TO USE:
            - Card-based fortune telling in taverns
            - Random inspiration for narrative direction
            - Games of chance in-character
            - Symbolic interpretation (suit = element, number = magnitude)
            - Quick random tables (draw for 1d13 per suit)

            PARAMETERS:
            - count: Number of cards to draw (1-52), drawn without replacement

            CARD SYMBOLISM:
            - Hearts: Emotions, relationships, love, compassion
            - Diamonds: Wealth, material matters, trade, ambition
            - Clubs: Conflict, struggle, challenge, warfare
            - Spades: Mystery, death, secrets, endings
            - Face cards: Important people or powerful forces
            - Aces: Beginnings, pure essence, critical moments

            EXAMPLES:
            - Draw 1 card for simple yes/no interpretation
            - Draw 3 cards for past/present/future reading
            - Draw 5 cards for complex situation analysis

            Returns card names like "Ace of Hearts", "Queen of Spades", "7 of Diamonds", etc.
            """)
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

    @Tool(name = "ChatDM_draw_tarot_cards", description = """
            Draw tarot cards from a 78-card deck for divination and narrative inspiration.

            WHEN TO USE:
            - Fortune teller NPC performs reading
            - Mystical prophecy or vision scenes
            - Rich symbolic inspiration for plot
            - Solo play for narrative direction
            - Thematic guidance for character arcs

            PARAMETERS:
            - count: Number of cards to draw (1-78), drawn without replacement

            DECK COMPOSITION:
            - 22 Major Arcana: Major life themes, spiritual lessons, fate
            - 56 Minor Arcana: Daily life, four suits (Wands, Cups, Swords, Pentacles)

            COMMON SPREADS:
            - 1 card: Simple answer or daily guidance
            - 3 cards: Past/Present/Future or Situation/Action/Outcome
            - 5 cards: Complex situation analysis
            - 10 cards: Celtic Cross (comprehensive)

            MAJOR ARCANA THEMES:
            Major cards represent significant life events, spiritual themes, and archetypal forces.
            Examples: The Fool (beginnings), Death (transformation), The Tower (upheaval)

            MINOR ARCANA SUITS:
            - Wands: Action, creativity, passion, adventure
            - Cups: Emotions, relationships, intuition, dreams
            - Swords: Intellect, conflict, truth, challenges
            - Pentacles: Material world, resources, body, practical matters

            Returns card names like "The Fool", "Queen of Cups", "Five of Swords", etc.

            TIP: Interpret cards through the lens of your current story for rich, contextual meaning.
            """)
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
