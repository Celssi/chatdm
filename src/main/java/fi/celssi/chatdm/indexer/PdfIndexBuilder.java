package fi.celssi.chatdm.indexer;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayInputStream;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Standalone tool to build SQLite FTS5 search index from PDF files.
 * Local: mvn exec:java@build-search-index
 * GCS: mvn exec:java@build-search-index-gcs -Dgcs.bucket=BUCKET
 */
public class PdfIndexBuilder {

    private static final String DB_PATH = "target/classes/search_index.db";
    private static final String RESOURCES_PATH = "src/main/resources";

    private static final Map<String, String> GAME_SYSTEMS = Map.of(
            "the-one-ring", "pdfs/lotr",
            "dnd-5e-2024", "pdfs/dnd",
            "brambletrek", "pdfs/brambletrek",
            "my-little-pony", "pdfs/my-little-pony"
    );

    public static void main(String[] args) {
        System.out.println("=== PDF Search Index Builder ===");

        String gcsBucket = System.getProperty("gcs.bucket");
        String gcsPrefix = System.getProperty("gcs.prefix", "pdfs/");

        try {
            PdfIndexBuilder builder = new PdfIndexBuilder();

            if (gcsBucket != null && !gcsBucket.isEmpty()) {
                System.out.println("Building index from GCS: gs://" + gcsBucket + "/" + gcsPrefix);
                Path tempDb = Files.createTempFile("search_index", ".db");
                try {
                    builder.buildIndexFromGcs(gcsBucket, gcsPrefix, tempDb);
                    builder.uploadToGcs(tempDb, gcsBucket, "search_index.db");
                    System.out.println("✓ Search index built and uploaded to gs://" + gcsBucket + "/search_index.db");
                } finally {
                    Files.deleteIfExists(tempDb);
                }
            } else {
                System.out.println("Building search index from local PDFs...");
                Files.deleteIfExists(Paths.get(DB_PATH));
                builder.buildIndexLocal();
                System.out.println("✓ Search index built successfully: " + DB_PATH);
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to build search index: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void buildIndexLocal() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            createSchema(conn);
            for (Map.Entry<String, String> entry : GAME_SYSTEMS.entrySet()) {
                indexGameSystemLocal(conn, entry.getKey(), entry.getValue());
            }
            optimizeIndex(conn);
        }
    }

    public void buildIndexFromGcs(String bucket, String prefix, Path outputPath) throws Exception {
        Storage storage = StorageOptions.getDefaultInstance().getService();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + outputPath.toAbsolutePath())) {
            createSchema(conn);

            for (Map.Entry<String, String> entry : GAME_SYSTEMS.entrySet()) {
                String gameSystemId = entry.getKey();
                String subPrefix = prefix.replaceAll("/$", "") + "/" + entry.getValue().replace("pdfs/", "");
                indexGameSystemFromGcs(conn, storage, bucket, subPrefix, gameSystemId);
            }
            optimizeIndex(conn);
        }
    }

    private void uploadToGcs(Path localFile, String bucket, String objectName) throws IOException {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        storage.create(BlobInfo.newBuilder(BlobId.of(bucket, objectName)).build(), Files.readAllBytes(localFile));
    }

    private void optimizeIndex(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO pdf_search_fts(pdf_search_fts) VALUES('optimize')");
        }
    }

    private void createSchema(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
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

    private void indexGameSystemLocal(Connection conn, String gameSystemId, String pdfDirectory) throws Exception {
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
            indexPdfFromStream(conn, gameSystemId, pdfPath.getFileName().toString(),
                    pdfPath.toString(), Files.newInputStream(pdfPath));
        }
    }

    private void indexGameSystemFromGcs(Connection conn, Storage storage, String bucket, String prefix, String gameSystemId) throws Exception {
        System.out.println("\n→ Indexing game system from GCS: " + gameSystemId + " (prefix: " + prefix + ")");

        List<Blob> blobs = storage.list(bucket, Storage.BlobListOption.prefix(prefix))
                .streamValues()
                .filter(b -> b.getName().toLowerCase().endsWith(".pdf"))
                .collect(Collectors.toList());

        for (Blob blob : blobs) {
            String fileName = blob.getName().substring(blob.getName().lastIndexOf('/') + 1);
            byte[] content = blob.getContent();
            indexPdfFromStream(conn, gameSystemId, fileName, "gs://" + bucket + "/" + blob.getName(),
                    new ByteArrayInputStream(content));
        }
    }

    private void indexPdfFromStream(Connection conn, String gameSystemId, String fileName, String filePath, InputStream is) throws Exception {
        String resourceId = gameSystemId + "-" + fileName.replaceAll("\\.pdf$", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-");
        String resourceName = fileName.replaceAll("\\.pdf$", "").replaceAll("_", " ");

        System.out.println("  • Indexing: " + fileName);

        try (PDDocument document = Loader.loadPDF(is.readAllBytes())) {
            int totalPages = document.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pdf_metadata (resource_id, game_system, resource_name, file_path, total_pages) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, resourceId);
                ps.setString(2, gameSystemId);
                ps.setString(3, resourceName);
                ps.setString(4, filePath);
                ps.setInt(5, totalPages);
                ps.execute();
            }

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
