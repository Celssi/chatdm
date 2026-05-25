package fi.celssi.chatdm.service;

import fi.celssi.chatdm.storage.JournalStorage;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NovelOracle {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String BOOKS = "books";
    private static final Pattern CHAPTER_SPLIT = Pattern.compile("(?m)\n## ");

    private static String bookPath(String bookId) { return BOOKS + "/" + bookId; }
    private static String chaptersPath(String bookId) { return bookPath(bookId) + "/chapters"; }
    private static String charactersPath(String bookId) { return bookPath(bookId) + "/characters"; }
    private static String placesPath(String bookId) { return bookPath(bookId) + "/places"; }
    private static String itemsPath(String bookId) { return bookPath(bookId) + "/items"; }

    private final JournalStorage storage;

    public NovelOracle(JournalStorage storage) {
        this.storage = storage;
    }

    @Tool(name = "ChatDM_sync_book", description = """
            Sync a book from markdown to cloud storage. If the book exists, updates all chapters.
            If new, creates a folder and splits into numbered chapter files (1.md, 2.md, ...).
            WHEN TO USE: User provides full book markdown; first step to save a book.
            Format: Use H2 (## ) headings to split chapters. Title from first # line if omitted.
            RELATED: ChatDM_list_books to see saved books; ChatDM_load_book_chapter to read.
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

            boolean isUpdate = storage.exists(bookPath(bookId), "meta.txt");
            if (isUpdate) {
                String existingMeta = storage.read(bookPath(bookId), "meta.txt");
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

            storage.write(bookPath(bookId), "meta.txt", metaContent);

            for (int i = 0; i < chapters.size(); i++) {
                storage.write(chaptersPath(bookId), (i + 1) + ".md", chapters.get(i));
            }

            return String.format("Book '%s' %s. %d chapters.", title, isUpdate ? "updated" : "created", chapters.size());
        } catch (IOException e) {
            return "Error syncing book: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_books", description = """
            List all books in cloud storage. Returns book titles and IDs.
            WHEN TO USE: First step before loading a chapter or bio; discover available books.
            RELATED: ChatDM_load_book_chapter, ChatDM_list_book_bios, ChatDM_load_book_meta.
            """)
    public String listBooks() {
        try {
            List<String> bookIds = storage.listSubdirs(BOOKS).stream().sorted().toList();

            if (bookIds.isEmpty()) {
                return "No books saved yet.";
            }

            return "Books:\n" + bookIds.stream()
                    .map(id -> {
                        try {
                            String meta = storage.read(bookPath(id), "meta.txt");
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
            Load a single chapter by book and index. Returns chapter markdown.
            WHEN TO USE: Required before novel_what_happens_next_prompt—pass loaded content as recentText.
            RELATED: ChatDM_list_books first; novel_what_happens_next_prompt for "what happens next" suggestions.
            Parameters:
            - bookName: Required. Book title or ID (from ChatDM_list_books)
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

            String content = storage.read(chaptersPath(bookId), chapterIndex + ".md");
            return content != null ? content : "Error: Chapter " + chapterIndex + " not found.";
        } catch (IOException e) {
            return "Error loading chapter: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_load_book_meta", description = """
            Load book metadata (title, creation date, chapter count, front matter).
            WHEN TO USE: Optional context for creative prompts or understanding book structure.
            RELATED: ChatDM_list_books, ChatDM_load_book_chapter.
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

            String content = storage.read(bookPath(bookId), "meta.txt");
            return content != null ? content : "Error: Metadata not found.";
        } catch (IOException e) {
            return "Error loading metadata: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_save_book_bio", description = """
            Create or update a character, place, or item bio for a book.
            WHEN TO USE: Store character/place/item notes for use in novel_character_dialogue_prompt.
            bioType must be: character, place, or item.
            RELATED: ChatDM_list_book_bios, ChatDM_load_book_bio (load before creative prompts).
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
            String fileName = sanitizedName + ".md";
            String subDir = switch (bioType.toLowerCase()) {
                case "character" -> charactersPath(bookId);
                case "place" -> placesPath(bookId);
                default -> itemsPath(bookId);
            };

            String content = String.format("""
                    TYPE: %s
                    NAME: %s
                    LAST_UPDATED: %s
                    
                    %s
                    """, bioType.toLowerCase(), name, LocalDateTime.now().format(DATE_FORMAT), bio);

            storage.write(subDir, fileName, content);
            return String.format("%s '%s' saved for book '%s'", capitalize(bioType), name, bookName);
        } catch (IOException e) {
            return "Error saving bio: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_list_book_bios", description = """
            List bios for a book, optionally filtered by type.
            WHEN TO USE: Before loading a bio for novel_character_dialogue_prompt.
            RELATED: ChatDM_load_book_bio, ChatDM_save_book_bio, novel_character_dialogue_prompt.
            Parameters:
            - bookName: Required. Book title or ID
            - bioType: Optional. Filter by character, place, or item
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

            List<String> bios = new ArrayList<>();
            for (String type : List.of("character", "place", "item")) {
                if (bioType != null && !bioType.trim().isEmpty() && !type.equals(bioType.toLowerCase())) continue;
                String subDir = switch (type) {
                    case "character" -> charactersPath(bookId);
                    case "place" -> placesPath(bookId);
                    default -> itemsPath(bookId);
                };
                for (String fileName : storage.list(subDir)) {
                    try {
                        String content = storage.read(subDir, fileName);
                        String name = content != null ? extractValue(content, "NAME:") : fileName.replace(".md", "");
                        bios.add("  - " + name + " (" + type + ")");
                    } catch (IOException e) {
                        bios.add("  - " + fileName);
                    }
                }
            }
            bios = bios.stream().distinct().sorted().toList();

            if (bios.isEmpty()) {
                return "No bios found for this book.";
            }

            return "Bios for " + bookName + ":\n" + String.join("\n", bios);
        } catch (IOException e) {
            return "Error listing bios: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_load_book_bio", description = """
            Load a single bio by book, type, and name. Returns bio content.
            WHEN TO USE: Before novel_character_dialogue_prompt—pass loaded content as characterBio.
            RELATED: ChatDM_list_book_bios first; novel_character_dialogue_prompt for dialogue suggestions.
            Parameters:
            - bookName: Required. Book title or ID
            - bioType: Required. character, place, or item
            - name: Required. Bio name (from ChatDM_list_book_bios)
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
            String fileName = sanitizedName + ".md";
            String subDir = switch (bioType.toLowerCase()) {
                case "character" -> charactersPath(bookId);
                case "place" -> placesPath(bookId);
                default -> itemsPath(bookId);
            };
            String content = storage.read(subDir, fileName);
            return content != null ? content : "Error: Bio not found.";
        } catch (IOException e) {
            return "Error loading bio: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_delete_book_bio", description = """
            Delete a single bio from cloud storage. DESTRUCTIVE—cannot be undone.
            Scope: One bio only. Use ChatDM_delete_book_chapter or ChatDM_delete_book for broader deletion.
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
            String fileName = sanitizedName + ".md";
            String subDir = switch (bioType.toLowerCase()) {
                case "character" -> charactersPath(bookId);
                case "place" -> placesPath(bookId);
                default -> itemsPath(bookId);
            };
            storage.delete(subDir, fileName);
            return String.format("Bio '%s' deleted.", name);
        } catch (IOException e) {
            return "Error deleting bio: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_delete_book", description = """
            Delete entire book from cloud storage. DESTRUCTIVE—removes metadata, all chapters, all bios.
            Scope: Whole book. Use ChatDM_delete_book_chapter or ChatDM_delete_book_bio for partial deletion.
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

            int deleted = 0;
            if (storage.listSubdirs(BOOKS).contains(bookId)) {
                for (String sub : List.of("chapters", "characters", "places", "items")) {
                    String subDir = bookPath(bookId) + "/" + sub;
                    for (String fileName : storage.list(subDir)) {
                        storage.delete(subDir, fileName);
                        deleted++;
                    }
                }
                storage.delete(bookPath(bookId), "meta.txt");
                deleted++;
            }
            return String.format("Book '%s' deleted (%d files).", bookName, deleted);
        } catch (IOException e) {
            return "Error deleting book: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_delete_book_chapter", description = """
            Delete a single chapter file from cloud storage. DESTRUCTIVE—cannot be undone.
            Scope: One chapter only. Use ChatDM_delete_book to remove entire book.
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

            storage.delete(chaptersPath(bookId), chapterIndex + ".md");
            return String.format("Chapter %d deleted.", chapterIndex);
        } catch (IOException e) {
            return "Error deleting chapter: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_save_book_chapter", description = """
            Save or overwrite a single chapter in cloud storage without re-syncing the whole book.
            WHEN TO USE: Update one chapter after editing; avoids ChatDM_sync_book for small changes.
            RELATED: ChatDM_load_book_chapter, ChatDM_append_book_chapter, ChatDM_sync_book for full-book import.
            Parameters:
            - bookName: Required. Book title or ID (from ChatDM_list_books)
            - chapterIndex: Required. 1-based chapter number to write or overwrite
            - content: Required. Chapter markdown content
            - title: Optional. If provided, prepends ## title to content when content does not start with ##
            """)
    public String saveBookChapter(String bookName, int chapterIndex, String content, String title) {
        if (bookName == null || bookName.trim().isEmpty()) {
            return "Error: Book name is required";
        }
        if (chapterIndex < 1) {
            return "Error: Chapter index must be >= 1";
        }
        if (content == null || content.trim().isEmpty()) {
            return "Error: Chapter content is required";
        }

        try {
            String bookId = resolveBookId(bookName);
            if (bookId == null) {
                return "Error: Book not found. Use ChatDM_list_books or ChatDM_sync_book first.";
            }

            String chapterContent = formatChapterContent(content, title);
            storage.write(chaptersPath(bookId), chapterIndex + ".md", chapterContent);
            updateChapterCount(bookId, chapterIndex);
            return String.format("Chapter %d saved for book '%s'.", chapterIndex, bookName);
        } catch (IOException e) {
            return "Error saving chapter: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_append_book_chapter", description = """
            Append a new chapter to a book with the next available chapter index.
            WHEN TO USE: Add a new chapter without knowing the current chapter count.
            RELATED: ChatDM_save_book_chapter to overwrite; ChatDM_list_books for book discovery.
            Parameters:
            - bookName: Required. Book title or ID
            - content: Required. Chapter markdown content
            - title: Optional. If provided, prepends ## title to content when content does not start with ##
            """)
    public String appendBookChapter(String bookName, String content, String title) {
        if (bookName == null || bookName.trim().isEmpty()) {
            return "Error: Book name is required";
        }
        if (content == null || content.trim().isEmpty()) {
            return "Error: Chapter content is required";
        }

        try {
            String bookId = resolveBookId(bookName);
            if (bookId == null) {
                return "Error: Book not found. Use ChatDM_list_books or ChatDM_sync_book first.";
            }

            int nextIndex = countChapters(bookId) + 1;
            String chapterContent = formatChapterContent(content, title);
            storage.write(chaptersPath(bookId), nextIndex + ".md", chapterContent);
            updateChapterCount(bookId, nextIndex);
            return String.format("Chapter %d appended to book '%s'.", nextIndex, bookName);
        } catch (IOException e) {
            return "Error appending chapter: " + e.getMessage();
        }
    }

    private String formatChapterContent(String content, String title) {
        String trimmed = content.trim();
        if (trimmed.startsWith("## ")) {
            return trimmed;
        }
        if (title != null && !title.trim().isEmpty()) {
            return "## " + title.trim() + "\n\n" + trimmed;
        }
        return trimmed;
    }

    private int countChapters(String bookId) throws IOException {
        int max = 0;
        for (String fileName : storage.list(chaptersPath(bookId))) {
            if (fileName.endsWith(".md")) {
                try {
                    int idx = Integer.parseInt(fileName.replace(".md", ""));
                    max = Math.max(max, idx);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return max;
    }

    private void updateChapterCount(String bookId, int chapterIndex) throws IOException {
        String existingMeta = storage.read(bookPath(bookId), "meta.txt");
        if (existingMeta == null) {
            return;
        }

        int currentCount = 0;
        try {
            currentCount = Integer.parseInt(extractValue(existingMeta, "CHAPTER_COUNT:"));
        } catch (NumberFormatException ignored) {
        }
        int newCount = Math.max(currentCount, chapterIndex);
        String updated = existingMeta.replaceFirst(
                "(?m)^CHAPTER_COUNT: .*$",
                "CHAPTER_COUNT: " + newCount);
        if (updated.equals(existingMeta)) {
            updated = existingMeta + "\nCHAPTER_COUNT: " + newCount + "\n";
        }
        updated = updated.replaceFirst(
                "(?m)^LAST_UPDATED: .*$",
                "LAST_UPDATED: " + LocalDateTime.now().format(DATE_FORMAT));
        storage.write(bookPath(bookId), "meta.txt", updated);
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
        if (storage.exists(bookPath(sanitized), "meta.txt")) {
            return sanitized;
        }
        for (String bookId : storage.listSubdirs(BOOKS)) {
            String meta = storage.read(bookPath(bookId), "meta.txt");
            if (meta != null) {
                String title = extractValue(meta, "TITLE:");
                if (title.equalsIgnoreCase(bookName.trim()) || sanitizeFilename(title).equals(sanitized)) {
                    return bookId;
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
