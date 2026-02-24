package fi.celssi.chatdm.service;

import fi.celssi.chatdm.model.SearchResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Fast search engine using pre-built SQLite FTS5 index.
 * Provides sub-second search across all PDFs.
 * Supports classpath (local) and GCS (cloud) index locations.
 */
@Service
public class SqliteSearchEngine {

    private static final String DEFAULT_DB_PATH = "search_index.db";
    private static final int SNIPPET_LENGTH = 150;

    private final ResourceLoader resourceLoader;
    private final String searchIndexPath;

    private Connection connection;
    private Path tempIndexPath;

    public SqliteSearchEngine(ResourceLoader resourceLoader,
                              @Value("${chatdm.search-index.path:}") String searchIndexPath) {
        this.resourceLoader = resourceLoader;
        this.searchIndexPath = searchIndexPath != null && !searchIndexPath.isEmpty() ? searchIndexPath : "";
    }

    @PostConstruct
    public void init() {
        try {
            String dbUrl;
            if (searchIndexPath.startsWith("gs://")) {
                Resource dbResource = resourceLoader.getResource(searchIndexPath);
                tempIndexPath = Files.createTempFile("search_index", ".db");
                try (InputStream is = dbResource.getInputStream()) {
                    Files.copy(is, tempIndexPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                dbUrl = "jdbc:sqlite:" + tempIndexPath.toAbsolutePath();
            } else {
                ClassPathResource dbResource = new ClassPathResource(DEFAULT_DB_PATH);
                dbUrl = "jdbc:sqlite::resource:" + dbResource.getURL();
            }
            connection = DriverManager.getConnection(dbUrl);
        } catch (Exception e) {
            System.err.println("⚠ Warning: Could not load search index. Search will be unavailable.");
            System.err.println("  Run 'mvn process-resources' to build the search index, or upload to GCS.");
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
        if (tempIndexPath != null) {
            try {
                Files.deleteIfExists(tempIndexPath);
            } catch (IOException e) {
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
