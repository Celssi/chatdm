package fi.celssi.chatdm.service;

import fi.celssi.chatdm.util.PdfTextCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GameResourceOracleTest {

    @Autowired
    private GameResourceOracle gameResourceOracle;

    @Autowired
    private PdfTextCache pdfTextCache;

    @Autowired
    private PdfSearchEngine searchEngine;

    @BeforeEach
    void setUp() {
        // Ensure the oracle is initialized
        assertNotNull(gameResourceOracle);
        assertNotNull(pdfTextCache);
        assertNotNull(searchEngine);
    }

    @Test
    void testSearchLindonElves() {
        // Test query parameters
        String query = "Lindon elves";
        String gameSystemId = "the-one-ring";
        String resourceName = "";  // Search all resources in the game system
        Integer maxResults = 10;
        Boolean useRegex = false;

        // Execute search
        String result = gameResourceOracle.searchResource(query, gameSystemId, resourceName, maxResults, useRegex);

        // Assertions
        assertNotNull(result, "Search result should not be null");
        assertFalse(result.contains("Error:"), "Search should not return an error");
        assertFalse(result.startsWith("No results found"), "Should find results for 'Lindon elves'");

        // Verify result format
        assertTrue(result.contains("Found"), "Result should start with 'Found' statement");
        assertTrue(result.contains("result(s) for 'Lindon elves'"), "Result should mention the query");

        // Verify relevance scoring is present
        assertTrue(result.contains("Score:"), "Results should include relevance scores");

        // Verify match highlighting
        assertTrue(result.contains("**"), "Results should include match highlighting");

        // Verify page numbers
        assertTrue(result.contains("Page"), "Results should include page numbers");

        // Print results for manual inspection
        System.out.println("\n=== Search Results for 'Lindon elves' ===\n");
        System.out.println(result);
    }

    @Test
    void testSearchWithEmptyQuery() {
        String result = gameResourceOracle.searchResource("", "the-one-ring", "", 10, false);
        assertTrue(result.contains("Error: Query cannot be empty"));
    }

    @Test
    void testSearchWithNullQuery() {
        String result = gameResourceOracle.searchResource(null, "the-one-ring", "", 10, false);
        assertTrue(result.contains("Error: Query cannot be empty"));
    }

    @Test
    void testSearchWithMissingGameSystem() {
        String result = gameResourceOracle.searchResource("test", "", "", 10, false);
        assertTrue(result.contains("Error: gameSystemId is required"));
    }

    @Test
    void testSearchWithInvalidGameSystem() {
        String result = gameResourceOracle.searchResource("test", "invalid-game-system", "", 10, false);
        assertTrue(result.contains("Error: Unknown game system"));
    }

    @Test
    void testSearchWithInvalidResource() {
        String result = gameResourceOracle.searchResource("test", "the-one-ring", "invalid-resource-id", 10, false);
        assertTrue(result.contains("Error: Unknown"));
    }

    @Test
    void testSearchWithRegex() {
        // Test regex search for pattern matching
        String query = "(Lindon|Rivendell).*elves?";
        String result = gameResourceOracle.searchResource(query, "the-one-ring", "", 5, true);

        assertNotNull(result);
        assertFalse(result.contains("Invalid regex pattern"));

        System.out.println("\n=== Regex Search Results ===\n");
        System.out.println(result);
    }

    @Test
    void testSearchSpecificResource() {
        // First, list resources to ensure we have valid resource IDs
        String resourceList = gameResourceOracle.listResources();
        assertNotNull(resourceList);
        assertTrue(resourceList.contains("The One Ring"));

        // Search in The One Ring core rulebook only
        String result = gameResourceOracle.searchResource("Lindon", "the-one-ring", "the-one-ring-core", 5, false);

        assertNotNull(result);

        System.out.println("\n=== Single Resource Search Results ===\n");
        System.out.println(result);
    }

    @Test
    void testFuzzySearch() {
        // Test fuzzy matching - searching for "elf" should also find "elves"
        String result = gameResourceOracle.searchResource("elf", "dnd-5e-2024", "", 5, false);

        assertNotNull(result);

        System.out.println("\n=== Fuzzy Search Results (elf -> elves) ===\n");
        System.out.println(result);
    }

    @Test
    void testListResources() {
        String result = gameResourceOracle.listResources();

        assertNotNull(result);
        assertTrue(result.contains("Game Systems and Resources"));
        assertTrue(result.contains("The One Ring"));
        assertTrue(result.contains("Dungeons & Dragons"));

        System.out.println("\n=== Available Resources ===\n");
        System.out.println(result);
    }

    @Test
    void testGetPage() {
        // Try to get page 1 from The One Ring core rulebook
        String result = gameResourceOracle.getPage("the-one-ring-core", 1);

        assertNotNull(result);
        assertFalse(result.contains("Error:"), "Should successfully retrieve page 1");
        assertTrue(result.contains("Page 1"));

        System.out.println("\n=== Page Content Sample ===\n");
        System.out.println(result.substring(0, Math.min(500, result.length())) + "...");
    }

    @Test
    void testGetInvalidPage() {
        String result = gameResourceOracle.getPage("the-one-ring-core", 99999);

        assertTrue(result.contains("Error:") || result.contains("does not exist"));
    }
}
