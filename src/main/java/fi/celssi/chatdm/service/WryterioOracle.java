package fi.celssi.chatdm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.celssi.chatdm.util.WryterioTokenHolder;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class WryterioOracle {

    private final NovelOracle novelOracle;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${chatdm.wryterio.api-url:}")
    private String wryterioApiUrl;

    public WryterioOracle(NovelOracle novelOracle,
                         RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.novelOracle = novelOracle;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Tool(name = "ChatDM_list_wryterio_books", description = """
            Fetch all books available from Wryterio. Returns list with id and name.
            Token: pass wryterioToken param, or set X-Wryterio-Token in MCP config headers.
            Parameters:
            - wryterioToken: Optional. API token; uses header value if omitted
            """)
    public String listWryterioBooks(String wryterioToken) {
        String token = resolveToken(wryterioToken);
        if (token == null) {
            return "Error: Wryterio token required. Pass wryterioToken or set X-Wryterio-Token in MCP config.";
        }

        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) {
            return "Error: Wryterio API not configured. Set chatdm.wryterio.api-url (e.g. https://api.wryterio.com).";
        }

        try {
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }

            List<Map<String, Object>> books = objectMapper.readValue(response.getBody(),
                    new TypeReference<>() {});
            if (books == null || books.isEmpty()) {
                return "No books found in Wryterio.";
            }

            StringBuilder sb = new StringBuilder("Wryterio books:\n");
            for (Map<String, Object> b : books) {
                String id = String.valueOf(b.getOrDefault("id", ""));
                String name = String.valueOf(b.getOrDefault("name", ""));
                sb.append("  - ").append(name).append(" (id: ").append(id).append(")\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error fetching Wryterio books: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_search_wryterio_books", description = """
            Search books by name/title in Wryterio.
            Parameters:
            - wryterioToken: Optional. API token
            - query: Required. Search query
            """)
    public String searchWryterioBooks(String wryterioToken, String query) {
        String token = resolveToken(wryterioToken);
        if (token == null) {
            return "Error: Wryterio token required.";
        }
        if (query == null || query.isBlank()) {
            return "Error: Search query is required.";
        }

        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) {
            return "Error: Wryterio API not configured.";
        }

        try {
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books?q=" + java.net.URLEncoder.encode(query.trim(), java.nio.charset.StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }

            List<Map<String, Object>> books = objectMapper.readValue(response.getBody(),
                    new TypeReference<>() {});
            if (books == null || books.isEmpty()) {
                return "No matching books found.";
            }

            StringBuilder sb = new StringBuilder("Wryterio books matching '" + query + "':\n");
            for (Map<String, Object> b : books) {
                String id = String.valueOf(b.getOrDefault("id", ""));
                String name = String.valueOf(b.getOrDefault("name", ""));
                sb.append("  - ").append(name).append(" (id: ").append(id).append(")\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error searching Wryterio books: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_fetch_wryterio_book", description = """
            Fetch full book markdown from Wryterio.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String fetchWryterioBook(String wryterioToken, String bookId) {
        String token = resolveToken(wryterioToken);
        if (token == null) {
            return "Error: Wryterio token required.";
        }
        if (bookId == null || bookId.isBlank()) {
            return "Error: bookId is required.";
        }

        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) {
            return "Error: Wryterio API not configured.";
        }

        try {
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/export";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return response.getBody() != null ? response.getBody() : "";
        } catch (Exception e) {
            return "Error fetching book: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_sync_wryterio_book_to_cloud", description = """
            Fetch book from Wryterio and sync to cloud storage in one call.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String syncWryterioBookToCloud(String wryterioToken, String bookId) {
        String markdown = fetchWryterioBook(wryterioToken, bookId);
        if (markdown.startsWith("Error:")) {
            return markdown;
        }
        return novelOracle.syncBook(markdown, null);
    }

    @Tool(name = "ChatDM_fetch_wryterio_characters", description = """
            Fetch character list for a book from Wryterio.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String fetchWryterioCharacters(String wryterioToken, String bookId) {
        String token = resolveToken(wryterioToken);
        if (token == null) {
            return "Error: Wryterio token required.";
        }
        if (bookId == null || bookId.isBlank()) {
            return "Error: bookId is required.";
        }

        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) {
            return "Error: Wryterio API not configured.";
        }

        try {
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/characters";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }

            List<Map<String, Object>> chars = objectMapper.readValue(response.getBody(),
                    new TypeReference<>() {});
            if (chars == null || chars.isEmpty()) {
                return "No characters found for this book.";
            }

            StringBuilder sb = new StringBuilder("Wryterio characters:\n");
            for (Map<String, Object> c : chars) {
                String name = String.valueOf(c.getOrDefault("name", ""));
                String bio = String.valueOf(c.getOrDefault("bio", ""));
                sb.append("  - ").append(name).append("\n");
                if (!bio.isEmpty() && !"null".equals(bio)) {
                    sb.append("    ").append(bio.substring(0, Math.min(80, bio.length()))).append(bio.length() > 80 ? "..." : "").append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error fetching characters: " + e.getMessage();
        }
    }

    @Tool(name = "ChatDM_sync_wryterio_characters_to_cloud", description = """
            Fetch characters from Wryterio and save as bios under the book in cloud storage.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            - bookName: Optional. Book name for cloud storage (defaults to book title from Wryterio)
            """)
    public String syncWryterioCharactersToCloud(String wryterioToken, String bookId, String bookName) {
        String token = resolveToken(wryterioToken);
        if (token == null) {
            return "Error: Wryterio token required.";
        }
        if (bookId == null || bookId.isBlank()) {
            return "Error: bookId is required.";
        }

        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) {
            return "Error: Wryterio API not configured.";
        }

        try {
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/characters";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }

            List<Map<String, Object>> chars = objectMapper.readValue(response.getBody(),
                    new TypeReference<>() {});
            if (chars == null || chars.isEmpty()) {
                return "No characters to sync.";
            }

            String targetBook = bookName != null && !bookName.isBlank() ? bookName.trim() : bookId;
            int count = 0;
            for (Map<String, Object> c : chars) {
                String name = String.valueOf(c.getOrDefault("name", ""));
                String bio = String.valueOf(c.getOrDefault("bio", ""));
                if (!name.isEmpty() && !"null".equals(name)) {
                    novelOracle.saveBookBio(targetBook, "character", name, bio != null ? bio : "");
                    count++;
                }
            }
            return String.format("Synced %d characters to cloud for book '%s'.", count, targetBook);
        } catch (Exception e) {
            return "Error syncing characters: " + e.getMessage();
        }
    }

    private String resolveToken(String paramToken) {
        if (paramToken != null && !paramToken.isBlank()) {
            return paramToken.trim();
        }
        return WryterioTokenHolder.get();
    }
}
