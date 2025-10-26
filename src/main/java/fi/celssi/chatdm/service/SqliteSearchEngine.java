package fi.celssi.chatdm.service;

import fi.celssi.chatdm.model.SearchResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Fast search engine using pre-built SQLite FTS5 index.
 * Provides sub-second search across all PDFs.
 */
@Service
public class SqliteSearchEngine {

    private static final String DB_PATH = "search_index.db";
    private static final int SNIPPET_LENGTH = 150;

    private Connection connection;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource dbResource = new ClassPathResource(DB_PATH);
            String dbUrl = "jdbc:sqlite::resource:" + dbResource.getURL();
            connection = DriverManager.getConnection(dbUrl);
            // System.out.println("✓ Connected to search index: " + DB_PATH);
        } catch (Exception e) {
            System.err.println("⚠ Warning: Could not load search index. Search will be unavailable.");
            System.err.println("  Run 'mvn process-resources' to build the search index.");
            connection = null;
        }
    }

    @PreDestroy
    public void cleanup() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Search for a query within a specific game system.
     */
    public List<SearchResult> search(String gameSystemId, String query, int maxResults) {
        if (connection == null) {
            return new ArrayList<>();
        }

        List<SearchResult> results = new ArrayList<>();

        try {
            // FTS5 query with BM25 ranking
            String sql = """
                        SELECT 
                            resource_name,
                            page_number,
                            snippet(pdf_search_fts, 4, '**', '**', '...', ?) as snippet,
                            bm25(pdf_search_fts) as score
                        FROM pdf_search_fts
                        WHERE pdf_search_fts MATCH ? AND game_system = ?
                        ORDER BY score
                        LIMIT ?
                    """;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, SNIPPET_LENGTH);
                ps.setString(2, query);
                ps.setString(3, gameSystemId);
                ps.setInt(4, maxResults);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String resourceName = rs.getString("resource_name");
                        int pageNumber = rs.getInt("page_number");
                        String snippet = rs.getString("snippet");
                        double score = -rs.getDouble("score"); // BM25 returns negative scores

                        // Clean up snippet
                        snippet = snippet.replaceAll("\\s+", " ").trim();

                        // Count matches (rough estimate based on bold markers)
                        int matchCount = (snippet.length() - snippet.replace("**", "").length()) / 4;

                        results.add(new SearchResult(resourceName, pageNumber, snippet, score, matchCount));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Search error: " + e.getMessage());
        }

        return results;
    }

    /**
     * Check if the search index is available.
     */
    public boolean isAvailable() {
        return connection != null;
    }
}
