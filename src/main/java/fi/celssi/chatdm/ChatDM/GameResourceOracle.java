package fi.celssi.chatdm.ChatDM;

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

    private Map<String, GameSystem> gameSystems;
    private Map<String, ResourceInfo> resources; // Flattened view for backward compatibility

    @PostConstruct
    public void init() throws IOException {
        gameSystems = new HashMap<>();
        resources = new HashMap<>();

        // Register Brambletrek game system
        registerBrambletrek();

        // Register My Little Pony RPG game system
        registerMyLittlePony();

        // Register D&D 5e 2024 game system
        registerDnD5e2024();
    }

    private void registerBrambletrek() {
        GameSystem brambletrek = new GameSystem(
                "brambletrek",
                "Brambletrek",
                "A woodland creatures RPG about tiny animals on big adventures"
        );

        // Add Brambletrek resources
        addGameResource(brambletrek, "core", new ResourceInfo(
                "Brambletrek Core Rules",
                "pdfs/brambletrek/Brambletrek_-_Complete_Digital_Edition.pdf",
                "core",
                "Complete core rulebook with basic adventures",
                "brambletrek"
        ));

        addGameResource(brambletrek, "birthday", new ResourceInfo(
                "A Birthday of Wonders",
                "pdfs/brambletrek/Brambletrek_-_A_Birthday_of_Wonders.pdf",
                "adventure",
                "Brambletrek adventure module",
                "brambletrek"
        ));

        addGameResource(brambletrek, "pumpkin", new ResourceInfo(
                "The Pumpkin Party",
                "pdfs/brambletrek/Brambletrek_-_The_Pumpkin_Party.pdf",
                "adventure",
                "Brambletrek adventure module",
                "brambletrek"
        ));

        addGameResource(brambletrek, "frost", new ResourceInfo(
                "The Warmth of the First Frost",
                "pdfs/brambletrek/Brambletrek_-_The_Warmth_of_the_First_Frost.pdf",
                "adventure",
                "Brambletrek adventure module",
                "brambletrek"
        ));

        gameSystems.put("brambletrek", brambletrek);
    }

    private void registerMyLittlePony() {
        GameSystem mlp = new GameSystem(
                "my-little-pony",
                "My Little Pony Roleplaying Game",
                "A friendship-powered RPG using the Essence20 system"
        );

        // Add My Little Pony resources
        addGameResource(mlp, "core", new ResourceInfo(
                "My Little Pony Core Rulebook",
                "pdfs/my-little-pony/My_Little_Pony_-_Core_Rulebook.pdf",
                "core",
                "Complete core rulebook for the My Little Pony RPG",
                "my-little-pony"
        ));

        addGameResource(mlp, "knights", new ResourceInfo(
                "Knights of Canterlot",
                "pdfs/my-little-pony/My_Little_Pony_-_Knights_of_Canterlot.pdf",
                "adventure",
                "My Little Pony adventure module",
                "my-little-pony"
        ));

        addGameResource(mlp, "seasons", new ResourceInfo(
                "Story of the Seasons",
                "pdfs/my-little-pony/My_Little_Pony_-_Story_of_the_Seasons.pdf",
                "adventure",
                "My Little Pony adventure module",
                "my-little-pony"
        ));

        gameSystems.put("my-little-pony", mlp);
    }

    private void registerDnD5e2024() {
        GameSystem dnd = new GameSystem(
                "dnd-5e-2024",
                "Dungeons & Dragons 5th Edition (2024)",
                "The world's greatest roleplaying game - 2024 edition with updated rules"
        );

        // Add D&D 5e 2024 core books
        addGameResource(dnd, "phb", new ResourceInfo(
                "Player's Handbook (2024)",
                "pdfs/dnd/players_handbook.pdf",
                "core",
                "Complete player's handbook with character creation, classes, spells, and rules",
                "dnd-5e-2024"
        ));

        addGameResource(dnd, "dmg", new ResourceInfo(
                "Dungeon Master's Guide (2024)",
                "pdfs/dnd/dungeon_masters_guide.pdf",
                "core",
                "Comprehensive guide for Dungeon Masters with world-building, treasure, and DMing advice",
                "dnd-5e-2024"
        ));

        addGameResource(dnd, "mm", new ResourceInfo(
                "Monster Manual (2024)",
                "pdfs/dnd/monster_manual.pdf",
                "core",
                "Extensive bestiary with hundreds of monsters, stat blocks, and lore",
                "dnd-5e-2024"
        ));

        addGameResource(dnd, "tasha", new ResourceInfo(
                "Tasha's Cauldron of Everything",
                "pdfs/dnd/tashas_cauldron_of_everything.pdf",
                "core",
                "Expanded rules for character options, spells, and magical items",
                "dnd-5e-2024"
        ));

        addGameResource(dnd, "xanathar", new ResourceInfo(
                "Xanathar's Guide to Everything",
                "pdfs/dnd/xanathars_guide_to_everything.pdf",
                "core",
                "Additional character options, spells, and DM tools",
                "dnd-5e-2024"
        ));

        addGameResource(dnd, "xanathar-lost", new ResourceInfo(
                "Xanathar's Lost Notes to Everything Else",
                "pdfs/dnd/xanathars_lost_notes.pdf",
                "core",
                "Unofficial supplement with additional character options and rules",
                "dnd-5e-2024"
        ));

        addGameResource(dnd, "dragons", new ResourceInfo(
                "The Book of Dragons",
                "pdfs/dnd/book_of_dragons.pdf",
                "core",
                "Comprehensive guide to dragons in D&D lore and gameplay",
                "dnd-5e-2024"
        ));

        gameSystems.put("dnd-5e-2024", dnd);
    }

    private void addGameResource(GameSystem gameSystem, String resourceKey, ResourceInfo resource) {
        String fullResourceId = gameSystem.getId() + "-" + resourceKey;
        gameSystem.addResource(fullResourceId, resource);
        resources.put(fullResourceId, resource);
    }

    @Tool(name = "ChatDM_list_resources", description = """
            List all available RPG resources (rulebooks and adventures) across all game systems.
            Returns information about each resource including game system, name, type, and description.
            """)
    public String listResources() {
        StringBuilder result = new StringBuilder("Available Game Systems and Resources:\n\n");

        for (GameSystem gameSystem : gameSystems.values()) {
            result.append(String.format("=== %s ===\n", gameSystem.getName()));
            result.append(String.format("%s\n\n", gameSystem.getDescription()));

            // Group resources by type
            Map<String, List<ResourceInfo>> byType = gameSystem.getResources().values().stream()
                    .collect(Collectors.groupingBy(r -> r.type));

            if (byType.containsKey("core")) {
                result.append("CORE RULEBOOKS:\n");
                for (Map.Entry<String, ResourceInfo> entry : gameSystem.getResources().entrySet()) {
                    if (entry.getValue().type.equals("core")) {
                        result.append(String.format("  - %s [ID: %s]\n    Description: %s\n\n",
                                entry.getValue().name, entry.getKey(), entry.getValue().description));
                    }
                }
            }

            if (byType.containsKey("adventure")) {
                result.append("ADVENTURES:\n");
                for (Map.Entry<String, ResourceInfo> entry : gameSystem.getResources().entrySet()) {
                    if (entry.getValue().type.equals("adventure")) {
                        result.append(String.format("  - %s [ID: %s]\n    Description: %s\n\n",
                                entry.getValue().name, entry.getKey(), entry.getValue().description));
                    }
                }
            }

            result.append("\n");
        }

        return result.toString();
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

        if (maxResults == null) {
            maxResults = 5;
        }

        List<SearchResult> results = new ArrayList<>();

        Collection<Map.Entry<String, ResourceInfo>> resourcesToSearch;
        if (resourceName != null && !resourceName.trim().isEmpty()) {
            if (!resources.containsKey(resourceName)) {
                return "Error: Unknown resource. Use ChatDM_list_resources to see available resources.";
            }
            resourcesToSearch = Collections.singletonList(
                    Map.entry(resourceName, resources.get(resourceName))
            );
        } else {
            resourcesToSearch = resources.entrySet();
        }

        for (Map.Entry<String, ResourceInfo> entry : resourcesToSearch) {
            try {
                results.addAll(searchInPdf(entry.getValue(), query, maxResults));
            } catch (IOException e) {
                return "Error reading resource: " + e.getMessage();
            }
        }

        if (results.isEmpty()) {
            return "No results found for: " + query;
        }

        // Sort by relevance (simple: by order found) and limit
        results = results.stream()
                .limit(maxResults)
                .collect(Collectors.toList());

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
                    String context = extractContext(pageText, queryLower, 150);
                    results.add(new SearchResult(resource.name, i, context));
                }
            }
        }

        return results;
    }

    private String extractContext(String text, String query, int contextLength) {
        String textLower = text.toLowerCase();
        int index = textLower.indexOf(query);

        if (index == -1) {
            return text.substring(0, Math.min(contextLength, text.length()));
        }

        int start = Math.max(0, index - contextLength / 2);
        int end = Math.min(text.length(), index + query.length() + contextLength / 2);

        String context = text.substring(start, end);

        // Clean up context
        context = context.replaceAll("\\s+", " ").trim();

        return context;
    }

    public static class ResourceInfo {
        public String name;
        public String path;
        public String type; // "core" or "adventure"
        public String description;
        public String gameSystemId; // Reference to parent game system

        public ResourceInfo(String name, String path, String type, String description) {
            this(name, path, type, description, null);
        }

        public ResourceInfo(String name, String path, String type, String description, String gameSystemId) {
            this.name = name;
            this.path = path;
            this.description = description;
            this.type = type;
            this.gameSystemId = gameSystemId;
        }
    }

    public static class SearchResult {
        public String resourceName;
        public int pageNumber;
        public String context;

        public SearchResult(String resourceName, int pageNumber, String context) {
            this.resourceName = resourceName;
            this.pageNumber = pageNumber;
            this.context = context;
        }

        @Override
        public String toString() {
            return String.format("[%s - Page %d] ...%s...", resourceName, pageNumber, context);
        }
    }
}
