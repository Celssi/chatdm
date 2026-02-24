package fi.celssi.chatdm.service;

import fi.celssi.chatdm.storage.JournalStorage;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NovelOracle {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String BOOKS = "books";
    private static final Pattern CHAPTER_SPLIT = Pattern.compile("(?m)\n## ");

    private final JournalStorage storage;

    public NovelOracle(JournalStorage storage) {
        this.storage = storage;
    }

    @Tool(name = "ChatDM_sync_book", description = """
            Sync a book from markdown to cloud storage. If the book exists, updates all chapters.
            If new, creates a folder and splits into numbered chapter files (1.md, 2.md, ...).
            Chapter titles may change; files are numbered for stability.
            Parameters:
            - bookMarkdown: Required. Full book markdown with H2 (## ) chapter headings
            - bookTitle: Optional. Display title; if omitted, extracted from first # line
            """)
    public String syncBook(String bookMarkdown, String bookTitle) {
        if (bookMarkdown == null || bookMarkdown.trim().isEmpty()) {
            return "Error: Book markdown is required";
        }

        try {
            String title = bookTitle != null && !bookTitle.trim().isEmpty()
                    ? bookTitle.trim()
                    : extractTitle(bookMarkdown);
            String bookId = sanitizeFilename(title);

            if (bookId.isEmpty()) {
                return "Error: Could not determine book title";
            }

            String[] parts = CHAPTER_SPLIT.split(bookMarkdown);
            String frontMatter = parts.length > 0 ? parts[0].trim() : "";
            List<String> chapters = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) {
                chapters.add("## " + parts[i].trim());
            }

            String metaContent = String.format("""
                    TITLE: %s
                    CREATED: %s
                    LAST_UPDATED: %s
                    CHAPTER_COUNT: %d
                    
                    %s
                    """, title, LocalDateTime.now().format(DATE_FORMAT),
                    LocalDateTime.now().format(DATE_FORMAT), chapters.size(), frontMatter);

            boolean isUpdate = storage.exists(BOOKS, bookId + "_meta.txt");
            if (isUpdate) {
                String existingMeta = storage.read(BOOKS, bookId + "_meta.txt");
                if (existingMeta != null) {
                    String created = extractValue(existingMeta, "CREATED:");
                    metaContent = String.format("""
                            TITLE: %s
                            CREATED: %s
                            LAST_UPDATED: %s
                            CHAPTER_COUNT: %d
                            
                            %s
                            """, title, created, LocalDateTime.now().format(DATE_FORMAT),
                            chapters.size(), frontMatter);
                }
            }

            storage.write(BOOKS, bookId + "_meta.txt", metaContent);

            for (int i = 0; i < chapters.size(); i++) {
                storage.write(BOOKS, bookId + "_" + (i + 1) + ".md", chapters.get(i));
            }

            return String.format("Book '%s' %s. %d chapters.", title, isUpdate ? "updated" : "created", chapters.size());
        } catch (IOException e) {
            return "Error syncing book: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_books", description = """
            List all books in cloud storage.
            Returns book titles and IDs.
            """)
    public String listBooks() {
        try {
            List<String> fileNames = storage.list(BOOKS);
            List<String> bookIds = fileNames.stream()
                    .filter(f -> f.endsWith("_meta.txt"))
                    .map(f -> f.replace("_meta.txt", ""))
                    .sorted()
                    .toList();

            if (bookIds.isEmpty()) {
                return "No books saved yet.";
            }

            return "Books:\n" + bookIds.stream()
                    .map(id -> {
                        try {
                            String meta = storage.read(BOOKS, id + "_meta.txt");
                            String title = meta != null ? extractValue(meta, "TITLE:") : id;
                            return "  - " + title + " (id: " + id + ")";
                        } catch (IOException e) {
                            return "  - " + id;
                        }
                    })
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "Error listing books: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_load_book_chapter", description = """
            Load a single chapter by book and index.
            Parameters:
            - bookName: Required. Book title or ID
            - chapterIndex: Required. 1-based chapter number
            """)
    public String loadBookChapter(String bookName, int chapterIndex) {
        if (bookName == null || bookName.trim().isEmpty()) {
            return "Error: Book name is required";
        }
        if (chapterIndex < 1) {
            return "Error: Chapter index must be >= 1";
        }

        try {
            String bookId = resolveBookId(bookName);
            if (bookId == null) {
                return "Error: Book not found. Use ChatDM_list_books to see available books.";
            }

            String content = storage.read(BOOKS, bookId + "_" + chapterIndex + ".md");
            return content != null ? content : "Error: Chapter " + chapterIndex + " not found.";
        } catch (IOException e) {
            return "Error loading chapter: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_load_book_meta", description = """
            Load book metadata (title, creation date, chapter count, front matter).
            Parameters:
            - bookName: Required. Book title or ID
            """)
    public String loadBookMeta(String bookName) {
        if (bookName == null || bookName.trim().isEmpty()) {
            return "Error: Book name is required";
        }

        try {
            String bookId = resolveBookId(bookName);
            if (bookId == null) {
                return "Error: Book not found. Use ChatDM_list_books to see available books.";
            }

            String content = storage.read(BOOKS, bookId + "_meta.txt");
            return content != null ? content : "Error: Metadata not found.";
        } catch (IOException e) {
            return "Error loading metadata: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_save_book_bio", description = """
            Create or update a character, place, or item bio for a book.
            Parameters:
            - bookName: Required. Book title or ID
            - bioType: Required. One of: character, place, item
            - name: Required. Character/place/item name
            - bio: Required. Bio content
            """)
    public String saveBookBio(String bookName, String bioType, String name, String bio) {
        if (bookName == null || bookName.trim().isEmpty()) {
            return "Error: Book name is required";
        }
        if (bioType == null || !List.of("character", "place", "item").contains(bioType.toLowerCase())) {
            return "Error: bioType must be character, place, or item";
        }
        if (name == null || name.trim().isEmpty()) {
            return "Error: Name is required";
        }
        if (bio == null || bio.trim().isEmpty()) {
            return "Error: Bio content is required";
        }

        try {
            String bookId = resolveBookId(bookName);
            if (bookId == null) {
                return "Error: Book not found. Use ChatDM_list_books to see available books.";
            }

            String sanitizedName = sanitizeFilename(name);
            String fileName = bookId + "_" + bioType.toLowerCase() + "_" + sanitizedName + ".md";

            String content = String.format("""
                    TYPE: %s
                    NAME: %s
                    LAST_UPDATED: %s
                    
                    %s
                    """, bioType.toLowerCase(), name, LocalDateTime.now().format(DATE_FORMAT), bio);

            storage.write(BOOKS, fileName, content);
            return String.format("%s '%s' saved for book '%s'", capitalize(bioType), name, bookName);
        } catch (IOException e) {
            return "Error saving bio: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_book_bios", description = """
            List bios for a book, optionally filtered by type.
            Parameters:
            - bookName: Required. Book title or ID
            - bioType: Optional. If provided, filter by character, place, or item
            """)
    public String listBookBios(String bookName, String bioType) {
        if (bookName == null || bookName.trim().isEmpty()) {
            return "Error: Book name is required";
        }

        try {
            String bookId = resolveBookId(bookName);
            if (bookId == null) {
                return "Error: Book not found. Use ChatDM_list_books to see available books.";
            }

            String prefix = bookId + "_";
            List<String> fileNames = storage.list(BOOKS).stream()
                    .filter(f -> f.startsWith(prefix) && (f.contains("_character_") || f.contains("_place_") || f.contains("_item_")))
                    .toList();

            List<String> bios = fileNames.stream()
                    .map(fileName -> {
                        try {
                            String type = fileName.contains("_character_") ? "character" : fileName.contains("_place_") ? "place" : "item";
                            if (bioType != null && !bioType.trim().isEmpty() && !type.equals(bioType.toLowerCase())) {
                                return null;
                            }
                            String content = storage.read(BOOKS, fileName);
                            String name = content != null ? extractValue(content, "NAME:") : fileName.replace(prefix + type + "_", "").replace(".md", "");
                            return "  - " + name + " (" + type + ")";
                        } catch (IOException e) {
                            return "  - " + fileName;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();

            if (bios.isEmpty()) {
                return "No bios found for this book.";
            }

            return "Bios for " + bookName + ":\n" + String.join("\n", bios);
        } catch (IOException e) {
            return "Error listing bios: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_load_book_bio", description = """
            Load a single bio by book, type, and name.
            Parameters:
            - bookName: Required. Book title or ID
            - bioType: Required. character, place, or item
            - name: Required. Bio name
            """)
    public String loadBookBio(String bookName, String bioType, String name) {
        if (bookName == null || bookName.trim().isEmpty()) {
            return "Error: Book name is required";
        }
        if (bioType == null || !List.of("character", "place", "item").contains(bioType.toLowerCase())) {
            return "Error: bioType must be character, place, or item";
        }
        if (name == null || name.trim().isEmpty()) {
            return "Error: Name is required";
        }

        try {
            String bookId = resolveBookId(bookName);
            if (bookId == null) {
                return "Error: Book not found.";
            }

            String sanitizedName = sanitizeFilename(name);
            String fileName = bookId + "_" + bioType.toLowerCase() + "_" + sanitizedName + ".md";
            String content = storage.read(BOOKS, fileName);
            return content != null ? content : "Error: Bio not found.";
        } catch (IOException e) {
            return "Error loading bio: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_delete_book_bio", description = """
            Delete a single bio from cloud storage.
            Parameters:
            - bookName: Required. Book title or ID
            - bioType: Required. character, place, or item
            - name: Required. Bio name to delete
            """)
    public String deleteBookBio(String bookName, String bioType, String name) {
        if (bookName == null || bookName.trim().isEmpty()) {
            return "Error: Book name is required";
        }
        if (bioType == null || !List.of("character", "place", "item").contains(bioType.toLowerCase())) {
            return "Error: bioType must be character, place, or item";
        }
        if (name == null || name.trim().isEmpty()) {
            return "Error: Name is required";
        }

        try {
            String bookId = resolveBookId(bookName);
            if (bookId == null) {
                return "Error: Book not found.";
            }

            String sanitizedName = sanitizeFilename(name);
            String fileName = bookId + "_" + bioType.toLowerCase() + "_" + sanitizedName + ".md";
            storage.delete(BOOKS, fileName);
            return String.format("Bio '%s' deleted.", name);
        } catch (IOException e) {
            return "Error deleting bio: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_delete_book", description = """
            Delete entire book from cloud storage (metadata, all chapters, all bios).
            Parameters:
            - bookName: Required. Book title or ID
            """)
    public String deleteBook(String bookName) {
        if (bookName == null || bookName.trim().isEmpty()) {
            return "Error: Book name is required";
        }

        try {
            String bookId = resolveBookId(bookName);
            if (bookId == null) {
                return "Error: Book not found.";
            }

            String prefix = bookId + "_";
            List<String> fileNames = storage.list(BOOKS).stream()
                    .filter(f -> f.startsWith(prefix))
                    .toList();

            for (String fileName : fileNames) {
                storage.delete(BOOKS, fileName);
            }

            return String.format("Book '%s' deleted (%d files).", bookName, fileNames.size());
        } catch (IOException e) {
            return "Error deleting book: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_delete_book_chapter", description = """
            Delete a single chapter file from cloud storage.
            Parameters:
            - bookName: Required. Book title or ID
            - chapterIndex: Required. 1-based chapter number
            """)
    public String deleteBookChapter(String bookName, int chapterIndex) {
        if (bookName == null || bookName.trim().isEmpty()) {
            return "Error: Book name is required";
        }
        if (chapterIndex < 1) {
            return "Error: Chapter index must be >= 1";
        }

        try {
            String bookId = resolveBookId(bookName);
            if (bookId == null) {
                return "Error: Book not found.";
            }

            String fileName = bookId + "_" + chapterIndex + ".md";
            storage.delete(BOOKS, fileName);
            return String.format("Chapter %d deleted.", chapterIndex);
        } catch (IOException e) {
            return "Error deleting chapter: " + e.getMessage();
        }
    }

    private String extractTitle(String markdown) {
        int start = markdown.indexOf("# ");
        if (start == -1) return "";
        start += 2;
        int end = markdown.indexOf("\n", start);
        if (end == -1) end = markdown.length();
        return markdown.substring(start, end).trim();
    }

    private String resolveBookId(String bookName) throws IOException {
        String sanitized = sanitizeFilename(bookName);
        if (storage.exists(BOOKS, sanitized + "_meta.txt")) {
            return sanitized;
        }
        List<String> fileNames = storage.list(BOOKS);
        for (String name : fileNames) {
            if (name.endsWith("_meta.txt")) {
                String meta = storage.read(BOOKS, name);
                if (meta != null) {
                    String title = extractValue(meta, "TITLE:");
                    if (title.equalsIgnoreCase(bookName.trim()) || sanitizeFilename(title).equals(sanitized)) {
                        return name.replace("_meta.txt", "");
                    }
                }
            }
        }
        return null;
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_]", "_").toLowerCase().replaceAll("_+", "_");
    }

    private String extractValue(String content, String prefix) {
        int start = content.indexOf(prefix);
        if (start == -1) return "Unknown";
        start += prefix.length();
        int end = content.indexOf("\n", start);
        if (end == -1) end = content.length();
        String value = content.substring(start, end).trim();
        return value.isEmpty() ? "Unknown" : value;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
