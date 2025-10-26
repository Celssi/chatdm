package fi.celssi.chatdm.indexer;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone tool to build SQLite FTS5 search index from PDF files.
 * Run as: mvn exec:java@build-search-index
 */
public class PdfIndexBuilder {

    private static final String DB_PATH = "target/classes/search_index.db";
    private static final String RESOURCES_PATH = "src/main/resources";

    public static void main(String[] args) {
        System.out.println("=== PDF Search Index Builder ===");
        System.out.println("Building search index from PDFs...");

        try {
            // Delete old index if exists
            Files.deleteIfExists(Paths.get(DB_PATH));

            PdfIndexBuilder builder = new PdfIndexBuilder();
            builder.buildIndex();

            System.out.println("✓ Search index built successfully: " + DB_PATH);
        } catch (Exception e) {
            System.err.println("✗ Failed to build search index: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void buildIndex() throws Exception {
        // Create database with FTS5 table
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            createSchema(conn);

            // Index all game systems
            indexGameSystem(conn, "the-one-ring", "pdfs/lotr");
            indexGameSystem(conn, "dnd-5e-2024", "pdfs/dnd");
            indexGameSystem(conn, "brambletrek", "pdfs/brambletrek");
            indexGameSystem(conn, "my-little-pony", "pdfs/my-little-pony");

            // Optimize FTS5 index
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO pdf_search_fts(pdf_search_fts) VALUES('optimize')");
            }
        }
    }

    private void createSchema(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // Create FTS5 virtual table
            stmt.execute("""
                        CREATE VIRTUAL TABLE pdf_search_fts USING fts5(
                            game_system,
                            resource_id,
                            resource_name,
                            page_number,
                            content,
                            tokenize = 'porter unicode61'
                        )
                    """);

            // Create metadata table
            stmt.execute("""
                        CREATE TABLE pdf_metadata (
                            resource_id TEXT PRIMARY KEY,
                            game_system TEXT NOT NULL,
                            resource_name TEXT NOT NULL,
                            file_path TEXT NOT NULL,
                            total_pages INTEGER NOT NULL,
                            indexed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                    """);

            System.out.println("✓ Database schema created");
        }
    }

    private void indexGameSystem(Connection conn, String gameSystemId, String pdfDirectory) throws Exception {
        Path dir = Paths.get(RESOURCES_PATH, pdfDirectory);
        if (!Files.exists(dir)) {
            System.out.println("⚠ Directory not found: " + dir);
            return;
        }

        System.out.println("\n→ Indexing game system: " + gameSystemId);

        List<Path> pdfFiles = new ArrayList<>();
        Files.walk(dir, 1)
                .filter(p -> p.toString().toLowerCase().endsWith(".pdf"))
                .forEach(pdfFiles::add);

        for (Path pdfPath : pdfFiles) {
            indexPdf(conn, gameSystemId, pdfPath);
        }
    }

    private void indexPdf(Connection conn, String gameSystemId, Path pdfPath) throws Exception {
        String fileName = pdfPath.getFileName().toString();
        String resourceId = gameSystemId + "-" + fileName.replaceAll("\\.pdf$", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-");
        String resourceName = fileName.replaceAll("\\.pdf$", "").replaceAll("_", " ");

        System.out.println("  • Indexing: " + fileName);

        try (InputStream is = Files.newInputStream(pdfPath);
             PDDocument document = Loader.loadPDF(is.readAllBytes())) {

            int totalPages = document.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();

            // Insert metadata
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pdf_metadata (resource_id, game_system, resource_name, file_path, total_pages) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, resourceId);
                ps.setString(2, gameSystemId);
                ps.setString(3, resourceName);
                ps.setString(4, pdfPath.toString());
                ps.setInt(5, totalPages);
                ps.execute();
            }

            // Index each page
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pdf_search_fts (game_system, resource_id, resource_name, page_number, content) VALUES (?, ?, ?, ?, ?)")) {

                for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                    stripper.setStartPage(pageNum);
                    stripper.setEndPage(pageNum);
                    String pageText = stripper.getText(document);

                    ps.setString(1, gameSystemId);
                    ps.setString(2, resourceId);
                    ps.setString(3, resourceName);
                    ps.setInt(4, pageNum);
                    ps.setString(5, pageText);
                    ps.addBatch();

                    if (pageNum % 50 == 0) {
                        ps.executeBatch();
                        System.out.println("    ↳ Indexed " + pageNum + "/" + totalPages + " pages");
                    }
                }
                ps.executeBatch();
            }

            System.out.println("    ✓ Completed: " + totalPages + " pages indexed");

        } catch (IOException e) {
            System.err.println("    ✗ Error indexing " + fileName + ": " + e.getMessage());
        }
    }
}
