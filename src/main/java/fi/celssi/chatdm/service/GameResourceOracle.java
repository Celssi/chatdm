package fi.celssi.chatdm.service;

import fi.celssi.chatdm.model.GameSystem;
import fi.celssi.chatdm.model.ResourceInfo;
import fi.celssi.chatdm.model.SearchResult;
import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameResourceOracle {

    private static final int DEFAULT_MAX_RESULTS = 5;
    private static final int DEFAULT_CONTEXT_LENGTH = 150;

    private final Map<String, GameSystem> gameSystems = new HashMap<>();
    private final Map<String, ResourceInfo> resources = new HashMap<>();

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
                "core", "Complete core rulebook with basic adventures");
        addResource(system, "birthday", "A Birthday of Wonders",
                "pdfs/brambletrek/Brambletrek_-_A_Birthday_of_Wonders.pdf",
                "adventure", "Brambletrek adventure module");
        addResource(system, "pumpkin", "The Pumpkin Party",
                "pdfs/brambletrek/Brambletrek_-_The_Pumpkin_Party.pdf",
                "adventure", "Brambletrek adventure module");
        addResource(system, "frost", "The Warmth of the First Frost",
                "pdfs/brambletrek/Brambletrek_-_The_Warmth_of_the_First_Frost.pdf",
                "adventure", "Brambletrek adventure module");
    }

    private void registerMyLittlePony() {
        GameSystem system = createGameSystem("my-little-pony", "My Little Pony Roleplaying Game",
                "A friendship-powered RPG using the Essence20 system");

        addResource(system, "core", "My Little Pony Core Rulebook",
                "pdfs/my-little-pony/My_Little_Pony_-_Core_Rulebook.pdf",
                "core", "Complete core rulebook for the My Little Pony RPG");
        addResource(system, "knights", "Knights of Canterlot",
                "pdfs/my-little-pony/My_Little_Pony_-_Knights_of_Canterlot.pdf",
                "adventure", "My Little Pony adventure module");
        addResource(system, "seasons", "Story of the Seasons",
                "pdfs/my-little-pony/My_Little_Pony_-_Story_of_the_Seasons.pdf",
                "adventure", "My Little Pony adventure module");
    }

    private void registerDnD5e2024() {
        GameSystem system = createGameSystem("dnd-5e-2024", "Dungeons & Dragons 5th Edition (2024)",
                "The world's greatest roleplaying game - 2024 edition with updated rules");

        addResource(system, "phb", "Player's Handbook (2024)",
                "pdfs/dnd/players_handbook.pdf", "core",
                "Complete player's handbook with character creation, classes, spells, and rules");
        addResource(system, "dmg", "Dungeon Master's Guide (2024)",
                "pdfs/dnd/dungeon_masters_guide.pdf", "core",
                "Comprehensive guide for Dungeon Masters with world-building, treasure, and DMing advice");
        addResource(system, "mm", "Monster Manual (2024)",
                "pdfs/dnd/monster_manual.pdf", "core",
                "Extensive bestiary with hundreds of monsters, stat blocks, and lore");
        addResource(system, "tasha", "Tasha's Cauldron of Everything",
                "pdfs/dnd/tashas_cauldron_of_everything.pdf", "core",
                "Expanded rules for character options, spells, and magical items");
        addResource(system, "xanathar", "Xanathar's Guide to Everything",
                "pdfs/dnd/xanathars_guide_to_everything.pdf", "core",
                "Additional character options, spells, and DM tools");
        addResource(system, "xanathar-lost", "Xanathar's Lost Notes to Everything Else",
                "pdfs/dnd/xanathars_lost_notes.pdf", "core",
                "Unofficial supplement with additional character options and rules");
        addResource(system, "dragons", "The Book of Dragons",
                "pdfs/dnd/book_of_dragons.pdf", "core",
                "Comprehensive guide to dragons in D&D lore and gameplay");
    }

    private void registerTheOneRing() {
        GameSystem system = createGameSystem("the-one-ring", "The One Ring RPG (2nd Edition)",
                "Adventure in Middle-earth in the time between The Hobbit and The Lord of the Rings");

        addResource(system, "core", "Core Rulebook",
                "pdfs/lotr/core_rulebook.pdf", "core",
                "Complete core rulebook for The One Ring 2nd Edition");
        addResource(system, "loremasters-screen", "Loremaster's Screen",
                "pdfs/lotr/loremasters_screen.pdf", "core",
                "Reference screen with quick rules and tables for Loremasters");
        addResource(system, "character-lifepaths", "Character Lifepaths",
                "pdfs/lotr/character_lifepaths.pdf", "supplement",
                "Expanded character creation options and background lifepaths");
        addResource(system, "moria", "Moria - Through The Doors of Durin",
                "pdfs/lotr/moria.pdf", "setting",
                "Adventure in the depths of Moria, the ancient Dwarven kingdom");
        addResource(system, "peoples-of-wilderland", "Peoples of Wilderland",
                "pdfs/lotr/peoples_of_wilderland.pdf", "setting",
                "Expanded lore and rules for the peoples of Wilderland");
        addResource(system, "ruins-lost-realm", "Ruins of the Lost Realm",
                "pdfs/lotr/ruins_of_the_lost_realm.pdf", "setting",
                "Explore the ruins and secrets of Arnor, the lost realm of the North");
        addResource(system, "tales-lone-lands", "Tales from the Lone-lands",
                "pdfs/lotr/tales_from_the_lone_lands.pdf", "adventure",
                "Collection of adventures set in the Lone-lands of Eriador");
        addResource(system, "strider-mode", "Strider Mode",
                "pdfs/lotr/strider_mode.pdf", "core",
                "Solo play rules and adventure for playing The One Ring without a Loremaster");
        addResource(system, "starter-adventures", "Starter Set - The Adventures",
                "pdfs/lotr/starter_set_adventures.pdf", "adventure",
                "Introductory adventures from the starter set");
        addResource(system, "starter-rules", "Starter Set - The Rules",
                "pdfs/lotr/starter_set_rules.pdf", "core",
                "Simplified rules from the starter set for new players");
        addResource(system, "starter-shire", "Starter Set - The Shire",
                "pdfs/lotr/starter_set_shire.pdf", "setting",
                "Introduction to adventuring in the Shire");
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
            Parameters:
            - query: The text to search for (required)
            - resourceName: Optional. Leave empty to search all resources, or specify a resource ID.
              Use ChatDM_list_resources to see available resource IDs.
            - maxResults: Optional. Maximum number of results to return (default: 5)

            Returns search results with page numbers and text context.
            """)
    public String searchResource(String query, String resourceName, Integer maxResults) {
        if (query == null || query.trim().isEmpty()) {
            return "Error: Query cannot be empty";
        }

        maxResults = (maxResults == null) ? DEFAULT_MAX_RESULTS : maxResults;
        Collection<Map.Entry<String, ResourceInfo>> resourcesToSearch = getResourcesToSearch(resourceName);

        if (resourcesToSearch == null) {
            return "Error: Unknown resource. Use ChatDM_list_resources to see available resources.";
        }

        List<SearchResult> results = performSearch(resourcesToSearch, query, maxResults);

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

    private List<SearchResult> performSearch(Collection<Map.Entry<String, ResourceInfo>> resourcesToSearch,
                                             String query, int maxResults) {
        List<SearchResult> results = new ArrayList<>();
        for (Map.Entry<String, ResourceInfo> entry : resourcesToSearch) {
            try {
                results.addAll(searchInPdf(entry.getValue(), query, maxResults));
            } catch (IOException e) {
                return new ArrayList<>(); // Return empty list on error
            }
        }
        return results;
    }

    private String formatSearchResults(List<SearchResult> results, String query, int maxResults) {
        results = results.stream().limit(maxResults).collect(Collectors.toList());
        StringBuilder output = new StringBuilder();
        output.append(String.format("Found %d result(s) for '%s':\n\n", results.size(), query));
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
            ClassPathResource pdfResource = new ClassPathResource(resource.path);
            try (PDDocument document = Loader.loadPDF(pdfResource.getInputStream().readAllBytes())) {
                if (pageNumber > document.getNumberOfPages()) {
                    return String.format("Error: Page %d does not exist. Document has %d pages.",
                            pageNumber, document.getNumberOfPages());
                }

                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);
                String text = stripper.getText(document);

                return String.format("[%s - Page %d]\n\n%s",
                        resource.name, pageNumber, text);
            }
        } catch (IOException e) {
            return "Error reading page: " + e.getMessage();
        }
    }

    private List<SearchResult> searchInPdf(ResourceInfo resource, String query, int maxPerResource) throws IOException {
        List<SearchResult> results = new ArrayList<>();
        String queryLower = query.toLowerCase();

        ClassPathResource pdfResource = new ClassPathResource(resource.path);
        try (PDDocument document = Loader.loadPDF(pdfResource.getInputStream().readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();

            for (int i = 1; i <= document.getNumberOfPages() && results.size() < maxPerResource; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(document);

                if (pageText.toLowerCase().contains(queryLower)) {
                    String context = extractContext(pageText, queryLower);
                    results.add(new SearchResult(resource.name, i, context));
                }
            }
        }

        return results;
    }

    private String extractContext(String text, String query) {
        String textLower = text.toLowerCase();
        int index = textLower.indexOf(query.toLowerCase());

        if (index == -1) {
            return text.substring(0, Math.min(DEFAULT_CONTEXT_LENGTH, text.length()));
        }

        int start = Math.max(0, index - DEFAULT_CONTEXT_LENGTH / 2);
        int end = Math.min(text.length(), index + query.length() + DEFAULT_CONTEXT_LENGTH / 2);

        return text.substring(start, end).replaceAll("\\s+", " ").trim();
    }
}
