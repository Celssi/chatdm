package fi.celssi.chatdm.service;

import fi.celssi.chatdm.model.GameSystem;
import fi.celssi.chatdm.model.ResourceInfo;
import fi.celssi.chatdm.model.SearchResult;
import fi.celssi.chatdm.util.PdfTextCache;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameResourceOracle {

    private static final int DEFAULT_MAX_RESULTS = 5;

    private final Map<String, GameSystem> gameSystems = new HashMap<>();
    private final Map<String, ResourceInfo> resources = new HashMap<>();
    private final PdfTextCache pdfTextCache;
    private final PdfSearchEngine searchEngine;

    public GameResourceOracle(PdfTextCache pdfTextCache, PdfSearchEngine searchEngine) {
        this.pdfTextCache = pdfTextCache;
        this.searchEngine = searchEngine;
    }

    @PostConstruct
    public void init() {
        registerBrambletrek();
        registerMyLittlePony();
        registerDnD5e2024();
        registerTheOneRing();
    }

    private void registerBrambletrek() {
        GameSystem system = createGameSystem("brambletrek", "Brambletrek",
                "A woodland creatures RPG about tiny animals on big adventures");

        addResource(system, "core", "Brambletrek Core Rules",
                "pdfs/brambletrek/Brambletrek_-_Complete_Digital_Edition.pdf",
                "core",
                "The complete rulebook for Brambletrek, a cozy solo journaling RPG set in the woodland world of Hyhill. Create a Gnawborn wanderer and explore the quiet magic of the forest through card prompts, seasonal festivals, and heartfelt storytelling. Includes full rules, lore, and starter adventures.");

        addResource(system, "birthday", "A Birthday of Wonders",
                "pdfs/brambletrek/Brambletrek_-_A_Birthday_of_Wonders.pdf",
                "adventure",
                "Celebrate Brambletrek's founding in this heartwarming adventure where the Gnawborn, Bristleborn, and Bushborn gather for the village's grand anniversary. Join Bramble on a journey to find the perfect birthday gift—a quest that reveals the meaning of kinship, memory, and home.");

        addResource(system, "pumpkin", "The Pumpkin Party",
                "pdfs/brambletrek/Brambletrek_-_The_Pumpkin_Party.pdf",
                "adventure",
                "An autumn festival adventure steeped in Gnawborn tradition. Prepare for the annual Pumpkin Party—a glowing celebration of harvest, memory, and community. Carve, bake, and celebrate under lantern light as old spirits stir among the vines and the forest comes alive with story and song.");

        addResource(system, "frost", "The Warmth of the First Frost",
                "pdfs/brambletrek/Brambletrek_-_The_Warmth_of_the_First_Frost.pdf",
                "adventure",
                "A winter tale of unity and mystery. As the First Frost arrives in Hyhill, the forest gathers around the Grand Frostfire to celebrate and remember. Beneath the snow lie ancient secrets waiting to be uncovered, as you join the Gnawborn, Bristleborn, and Bushborn in a season of wonder and renewal.");
    }

    private void registerMyLittlePony() {
        GameSystem system = createGameSystem("my-little-pony", "My Little Pony Roleplaying Game",
                "A friendship-powered RPG using the Essence20 system");

        addResource(system, "core", "My Little Pony Core Rulebook",
                "pdfs/my-little-pony/My_Little_Pony_-_Core_Rulebook.pdf",
                "core",
                "The complete rulebook for the My Little Pony Roleplaying Game. Create your own pony hero, explore Equestria, and tell stories of friendship and adventure using the Essence20 system. Includes full character creation, magic, equipment, creatures, and an introductory adventure.");

        addResource(system, "knights", "Knights of Canterlot",
                "pdfs/my-little-pony/My_Little_Pony_-_Knights_of_Canterlot.pdf",
                "supplement",
                "A fantasy expansion transforming the My Little Pony RPG into an epic quest across Equestria. Features new Influences, spells, magical items, and Threats, plus the full adventure 'The Mares of Malevolence.' Join the secret order of the Knights of Canterlot to defend harmony and uncover ancient mysteries.");

        addResource(system, "seasons", "Story of the Seasons",
                "pdfs/my-little-pony/My_Little_Pony_-_Story_of_the_Seasons.pdf",
                "adventure",
                "A year-long sourcebook of Ponyville festivals and seasonal stories. Includes four adventures—one for each season—along with rules for downtime, new Griffon origins, and a detailed gazetteer of Ponyville. Celebrate friendship and fun through the turning of the seasons in Equestria.");
    }

    private void registerDnD5e2024() {
        GameSystem system = createGameSystem("dnd-5e-2024", "Dungeons & Dragons 5th Edition (2024)",
                "The world's greatest roleplaying game - 2024 edition with updated rules");

        addResource(system, "phb", "Player's Handbook (2024)",
                "pdfs/dnd/players_handbook.pdf", "core",
                "The essential rulebook for all players. Includes complete rules for character creation, combat, adventuring, and spellcasting, alongside detailed lore for every class, race, and background in the 2024 edition of Dungeons & Dragons.");

        addResource(system, "dmg", "Dungeon Master's Guide (2024)",
                "pdfs/dnd/dungeon_masters_guide.pdf", "core",
                "The ultimate guide for Dungeon Masters. Offers world-building advice, encounter creation tools, treasure tables, and optional rules to help you craft unforgettable adventures and campaigns.");

        addResource(system, "mm", "Monster Manual (2024)",
                "pdfs/dnd/monster_manual.pdf", "core",
                "A vast bestiary of Dungeons & Dragons creatures, from goblins and dragons to celestial beings and fiends. Includes lore, stat blocks, and inspiration for encounters and storytelling.");

        addResource(system, "tasha", "Tasha's Cauldron of Everything",
                "pdfs/dnd/tashas_cauldron_of_everything.pdf", "supplement",
                "An expansion of magical creativity and versatility. Introduces new subclasses, spells, magic items, and rules for group patrons, sidekicks, and supernatural regions—curated with wit and wisdom by the archmage Tasha herself.");

        addResource(system, "xanathar", "Xanathar's Guide to Everything",
                "pdfs/dnd/xanathars_guide_to_everything.pdf", "supplement",
                "A treasure trove of expanded options for players and DMs alike, featuring new subclasses, spells, downtime activities, and guidance for running rich, character-driven campaigns, all through the eyes of Waterdeep's most infamous beholder.");

        addResource(system, "xanathar-lost", "Xanathar's Lost Notes to Everything Else",
                "pdfs/dnd/xanathars_lost_notes.pdf", "supplement",
                "A DM's Guild Adepts companion to Xanathar's Guide, offering dozens of new subclasses, backgrounds, races, and rules modules. Blends player options with story-driven lore from the darker corners of the multiverse.");

        addResource(system, "dragons", "The Book of Dragons",
                "pdfs/dnd/book_of_dragons.pdf", "supplement",
                "An expansive guide to dragonkind across the worlds of D&D. Details draconic lore, physiology, and psychology, alongside lair designs, hoards, new dragon types, and tools for creating legendary dragon encounters.");
    }

    private void registerTheOneRing() {
        GameSystem system = createGameSystem("the-one-ring", "The One Ring RPG (2nd Edition)",
                "Adventure in Middle-earth in the time between The Hobbit and The Lord of the Rings");

        addResource(system, "core", "Core Rulebook",
                "pdfs/lotr/core_rulebook.pdf", "core",
                "The definitive guide to The One Ring 2nd Edition. Contains the full rules, setting of Eriador, and guidance for both Players and Loremasters to explore Middle-earth in the Twilight of the Third Age.");

        addResource(system, "loremasters-screen", "Loremaster's Screen",
                "pdfs/lotr/loremasters_screen.pdf", "core",
                "A handy reference for Loremasters, featuring quick-access tables for combat, journeys, councils, risk levels, and sources of injury, making play faster and smoother.");

        addResource(system, "character-lifepaths", "Character Lifepaths",
                "pdfs/lotr/character_lifepaths.pdf", "supplement",
                "Generate unique backstories for your heroes with lifepaths tied to each culture. Includes optional attributes, favoured skills, and distinctive features to enrich character creation.");

        addResource(system, "moria", "Moria - Through The Doors of Durin",
                "pdfs/lotr/moria.pdf", "setting",
                "An expansive campaign sourcebook exploring the dark depths of Khazad-dûm. Includes history, lore, maps, and adventures within the Dwarrowdelf — plus options for solo and group play.");

        addResource(system, "peoples-of-wilderland", "Peoples of Wilderland",
                "pdfs/lotr/peoples_of_wilderland.pdf", "setting",
                "Details the cultures of the northern lands — Beornings, Woodmen, Elves, and Dwarves — with new blessings, virtues, and backgrounds expanding your hero creation options.");

        addResource(system, "ruins-lost-realm", "Ruins of the Lost Realm",
                "pdfs/lotr/ruins_of_the_lost_realm.pdf", "setting",
                "A setting guide to southern Eriador, exploring Tharbad, Lond Daer, and the forgotten lands of Cardolan and Minhiriath. Includes twelve detailed landmarks, characters, and threats rising in the West.");

        addResource(system, "tales-lone-lands", "Tales from the Lone-lands",
                "pdfs/lotr/tales_from_the_lone_lands.pdf", "adventure",
                "Six linked adventures set across the wilds of Eriador, from Bree to the ruins of Angmar. Uncover ancient evils, forgotten lineages, and the growing shadow of Mordor in the North.");

        addResource(system, "strider-mode", "Strider Mode",
                "pdfs/lotr/strider_mode.pdf", "core",
                "Rules for solo play inspired by Aragorn's wandering years. Includes oracles, tables, and guidance to explore Middle-earth alone or cooperatively without a Loremaster.");

        addResource(system, "starter-adventures", "Starter Set - The Adventures",
                "pdfs/lotr/starter_set_adventures.pdf", "adventure",
                "Five introductory adventures set in the Shire, featuring Bilbo and his Hobbit kin. A perfect starting point for new players to experience gentle heroism and quiet peril.");

        addResource(system, "starter-rules", "Starter Set - The Rules",
                "pdfs/lotr/starter_set_rules.pdf", "core",
                "Streamlined rules and examples of play introducing The One Ring's core mechanics. Ideal for learning the system and guiding new adventurers through their first journeys.");

        addResource(system, "starter-shire", "Starter Set - The Shire",
                "pdfs/lotr/starter_set_shire.pdf", "setting",
                "A richly detailed gazetteer of the Shire with maps, customs, and local tales. Serves as a peaceful yet lively setting for early adventures in the heart of Hobbiton.");
    }

    private GameSystem createGameSystem(String id, String name, String description) {
        GameSystem system = new GameSystem(id, name, description);
        gameSystems.put(id, system);
        return system;
    }

    private void addResource(GameSystem system, String key, String name, String path, String type, String description) {
        String fullId = system.getId() + "-" + key;
        ResourceInfo resource = new ResourceInfo(name, path, type, description, system.getId());
        system.addResource(fullId, resource);
        resources.put(fullId, resource);
    }

    @Tool(name = "ChatDM_list_resources", description = """
            List all available RPG resources (rulebooks and adventures) across all game systems.
            Returns information about each resource including game system, name, type, and description.
            """)
    public String listResources() {
        StringBuilder result = new StringBuilder("Available Game Systems and Resources:\n\n");

        for (GameSystem gameSystem : gameSystems.values()) {
            result.append(String.format("=== %s ===\n%s\n\n",
                    gameSystem.getName(), gameSystem.getDescription()));

            Map<String, List<Map.Entry<String, ResourceInfo>>> byType = gameSystem.getResources().entrySet().stream()
                    .collect(Collectors.groupingBy(e -> e.getValue().type));

            appendResourceSection(result, byType, "core", "CORE RULEBOOKS");
            appendResourceSection(result, byType, "adventure", "ADVENTURES");
            appendResourceSection(result, byType, "setting", "SETTING BOOKS");
            appendResourceSection(result, byType, "supplement", "SUPPLEMENTS");

            result.append("\n");
        }

        return result.toString();
    }

    private void appendResourceSection(StringBuilder result,
                                       Map<String, List<Map.Entry<String, ResourceInfo>>> byType,
                                       String type, String header) {
        List<Map.Entry<String, ResourceInfo>> entries = byType.get(type);
        if (entries != null && !entries.isEmpty()) {
            result.append(header).append(":\n");
            for (Map.Entry<String, ResourceInfo> entry : entries) {
                result.append(String.format("  - %s [ID: %s]\n    Description: %s\n\n",
                        entry.getValue().name, entry.getKey(), entry.getValue().description));
            }
        }
    }

    @Tool(name = "ChatDM_search_resource", description = """
            Search for text within a specific resource or across all resources in all game systems.
            Supports both plain text and regex pattern matching with fuzzy/stemmed search.

            Parameters:
            - query: The text or regex pattern to search for (required)
            - resourceName: Optional. Leave empty to search all resources, or specify a resource ID.
              Use ChatDM_list_resources to see available resource IDs.
            - maxResults: Optional. Maximum number of results to return (default: 5)
            - useRegex: Optional. Set to true to treat query as a regex pattern (default: false)

            Returns search results with page numbers, text context, match counts, and relevance scores.
            Results are ranked by relevance.
            """)
    public String searchResource(String query, String resourceName, Integer maxResults, Boolean useRegex) {
        if (query == null || query.trim().isEmpty()) {
            return "Error: Query cannot be empty";
        }

        maxResults = (maxResults == null) ? DEFAULT_MAX_RESULTS : maxResults;
        useRegex = (useRegex == null) ? false : useRegex;

        Collection<Map.Entry<String, ResourceInfo>> resourcesToSearch = getResourcesToSearch(resourceName);

        if (resourcesToSearch == null) {
            return "Error: Unknown resource. Use ChatDM_list_resources to see available resources.";
        }

        List<SearchResult> results = searchEngine.performParallelSearch(resourcesToSearch, query, maxResults, useRegex);

        if (results.isEmpty()) {
            return "No results found for: " + query;
        }

        return formatSearchResults(results, query, maxResults);
    }

    private Collection<Map.Entry<String, ResourceInfo>> getResourcesToSearch(String resourceName) {
        if (resourceName != null && !resourceName.trim().isEmpty()) {
            if (!resources.containsKey(resourceName)) {
                return null;
            }
            return Collections.singletonList(Map.entry(resourceName, resources.get(resourceName)));
        }
        return resources.entrySet();
    }

    private String formatSearchResults(List<SearchResult> results, String query, int maxResults) {
        StringBuilder output = new StringBuilder();
        output.append(String.format("Found %d result(s) for '%s' (showing top %d):\n\n",
                results.size(), query, Math.min(results.size(), maxResults)));
        for (SearchResult result : results) {
            output.append(result.toString()).append("\n\n");
        }
        return output.toString();
    }

    @Tool(name = "ChatDM_get_page", description = """
            Get the text content from a specific page of a resource.
            Parameters:
            - resourceName: Required. The resource ID to read from.
              Use ChatDM_list_resources to see available resource IDs.
            - pageNumber: Required. The page number to retrieve (1-indexed)

            Returns the text content of the specified page.
            """)
    public String getPage(String resourceName, int pageNumber) {
        if (resourceName == null || !resources.containsKey(resourceName)) {
            return "Error: Unknown resource. Use ChatDM_list_resources to see available resources.";
        }

        if (pageNumber < 1) {
            return "Error: Page number must be 1 or greater.";
        }

        ResourceInfo resource = resources.get(resourceName);

        try {
            String pageText = pdfTextCache.getPageText(resource.path, pageNumber);
            if (pageText == null) {
                return String.format("Error: Page %d does not exist.", pageNumber);
            }

            return String.format("[%s - Page %d]\n\n%s", resource.name, pageNumber, pageText);
        } catch (IOException e) {
            return "Error reading page: " + e.getMessage();
        }
    }
}
