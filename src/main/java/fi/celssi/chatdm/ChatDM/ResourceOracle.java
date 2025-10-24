package fi.celssi.chatdm.ChatDM;

import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResourceOracle {

    private Map<String, ResourceInfo> resources;

    public static class ResourceInfo {
        public String name;
        public String path;
        public String type; // "core" or "adventure"
        public String description;

        public ResourceInfo(String name, String path, String type, String description) {
            this.name = name;
            this.path = path;
            this.description = description;
            this.type = type;
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

    @PostConstruct
    public void init() throws IOException {
        resources = new HashMap<>();

        // Register Brambletrek resources
        resources.put("brambletrek-core", new ResourceInfo(
                "Brambletrek Core Rules",
                "pdfs/brambletrek/Brambletrek_-_Complete_Digital_Edition.pdf",
                "core",
                "Complete core rulebook with basic adventures"
        ));

        resources.put("brambletrek-birthday", new ResourceInfo(
                "A Birthday of Wonders",
                "pdfs/brambletrek/Brambletrek_-_A_Birthday_of_Wonders.pdf",
                "adventure",
                "Brambletrek adventure module"
        ));

        resources.put("brambletrek-pumpkin", new ResourceInfo(
                "The Pumpkin Party",
                "pdfs/brambletrek/Brambletrek_-_The_Pumpkin_Party.pdf",
                "adventure",
                "Brambletrek adventure module"
        ));

        resources.put("brambletrek-frost", new ResourceInfo(
                "The Warmth of the First Frost",
                "pdfs/brambletrek/Brambletrek_-_The_Warmth_of_the_First_Frost.pdf",
                "adventure",
                "Brambletrek adventure module"
        ));
    }

    @Tool(name = "ChatDM_list_resources", description = """
            List all available RPG resources (rulebooks and adventures).
            Returns information about each resource including name, type, and description.
            """)
    public String listResources() {
        StringBuilder result = new StringBuilder("Available Resources:\n\n");

        // Group by type
        Map<String, List<ResourceInfo>> byType = resources.values().stream()
                .collect(Collectors.groupingBy(r -> r.type));

        if (byType.containsKey("core")) {
            result.append("CORE RULEBOOKS:\n");
            for (ResourceInfo info : byType.get("core")) {
                result.append(String.format("  - %s\n    Description: %s\n\n",
                        info.name, info.description));
            }
        }

        if (byType.containsKey("adventure")) {
            result.append("ADVENTURES:\n");
            for (ResourceInfo info : byType.get("adventure")) {
                result.append(String.format("  - %s\n    Description: %s\n\n",
                        info.name, info.description));
            }
        }

        return result.toString();
    }

    @Tool(name = "ChatDM_search_resource", description = """
            Search for text within a specific resource or across all resources.
            Parameters:
            - query: The text to search for (required)
            - resourceName: Optional. Leave empty to search all resources, or specify one of:
              'brambletrek-core', 'brambletrek-birthday', 'brambletrek-pumpkin', 'brambletrek-frost'
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
            - resourceName: Required. One of: 'brambletrek-core', 'brambletrek-birthday',
              'brambletrek-pumpkin', 'brambletrek-frost'
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
}
