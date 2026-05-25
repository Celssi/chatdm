package fi.celssi.chatdm.service.wryterio;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import fi.celssi.chatdm.service.shared.WryterioContentGuard;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WryterioBookTools {

    private final WryterioClient client;

    public WryterioBookTools(WryterioClient client) {
        this.client = client;
    }

    @Tool(name = "ChatDM_list_wryterio_books", description = """
            Fetch all books available from Wryterio. Returns list with id and name.
            WHEN TO USE: First step to discover books before syncing or fetching. Use bookId with ChatDM_sync_wryterio_book_to_cloud when user wants to load/save.
            Token: pass wryterioToken param, or set X-Wryterio-Token in MCP config headers.
            Parameters:
            - wryterioToken: Optional. API token; uses header value if omitted
            """)
    public String listWryterioBooks(String wryterioToken) {
        String token = client.resolveToken(wryterioToken);
        if (token == null) {
            return "Error: Wryterio token required. Pass wryterioToken or set X-Wryterio-Token in MCP config.";
        }

        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) {
            return "Error: Wryterio API not configured. Set chatdm.wryterio.api-url (e.g. https://api.wryterio.com).";
        }

        try {
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = client.restTemplate().exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }

            List<Map<String, Object>> books = client.objectMapper().readValue(response.getBody(),
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
            return client.formatWryterioError("fetching Wryterio books", e);
        }
    }

    @Tool(name = "ChatDM_get_wryterio_book", description = """
            Fetch book metadata (title, author, description, chapter count) from Wryterio.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String getWryterioBook(String wryterioToken, String bookId) {
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> book = client.objectMapper().readValue(response.getBody(), new TypeReference<>() {});
            return String.format("Book: %s (id: %s)\nAuthor: %s\nDescription: %s\nChapters: %d",
                    book.get("title"), book.get("id"), book.getOrDefault("author", ""), book.getOrDefault("description", ""),
                    ((Number) book.getOrDefault("chapterCount", 0)).intValue());
        } catch (Exception e) {
            return client.formatWryterioError("fetching book", e);
        }
    }

    @Tool(name = "ChatDM_update_wryterio_book", description = """
            Update book metadata (title, author, description) in Wryterio.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            - title: Optional. New title
            - author: Optional. New author (pass empty to clear)
            - description: Optional. New description (pass empty to clear)
            """)
    public String updateWryterioBook(String wryterioToken, String bookId, String title, String author, String description) {
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";
        if ((title == null || title.isBlank()) && author == null && description == null) {
            return "Error: At least one of title, author, or description is required.";
        }

        try {
            Map<String, Object> updates = new HashMap<>();
            if (title != null && !title.isBlank()) updates.put("title", title.trim());
            if (author != null) updates.put("author", author.isBlank() ? null : author);
            if (description != null) updates.put("description", description.isBlank() ? null : description);
            String body = client.objectMapper().writeValueAsString(updates);

            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.PATCH, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return "Book metadata updated.";
        } catch (Exception e) {
            return client.formatWryterioError("updating book", e);
        }
    }

    @Tool(name = "ChatDM_create_wryterio_book", description = """
            Create a new book in Wryterio.
            Parameters:
            - wryterioToken: Optional. API token
            - title: Required. Book title
            """)
    public String createWryterioBook(String wryterioToken, String title) {
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (title == null || title.isBlank()) return "Error: title is required.";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";

        try {
            String body = client.objectMapper().writeValueAsString(Map.of("title", title.trim()));
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> result = client.objectMapper().readValue(response.getBody(), new TypeReference<>() {});
            return String.format("Book created: %s (id: %s)", result.get("title"), result.get("id"));
        } catch (Exception e) {
            return client.formatWryterioError("creating book", e);
        }
    }

    @Tool(name = "ChatDM_search_wryterio_books", description = """
            Search books by name/title in Wryterio.
            Parameters:
            - wryterioToken: Optional. API token
            - query: Required. Search query
            """)
    public String searchWryterioBooks(String wryterioToken, String query) {
        String token = client.resolveToken(wryterioToken);
        if (token == null) {
            return "Error: Wryterio token required.";
        }
        if (query == null || query.isBlank()) {
            return "Error: Search query is required.";
        }

        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) {
            return "Error: Wryterio API not configured.";
        }

        try {
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books?q=" + URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = client.restTemplate().exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }

            List<Map<String, Object>> books = client.objectMapper().readValue(response.getBody(),
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
            return client.formatWryterioError("searching Wryterio books", e);
        }
    }

    @Tool(name = "ChatDM_get_wryterio_book_cover", description = """
            Get the book cover image URL for a Wryterio book.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String getWryterioBookCover(String wryterioToken, String bookId) {
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> book = client.objectMapper().readValue(response.getBody(), new TypeReference<>() {});
            Object coverObj = book.get("coverImageUrl");
            if (coverObj == null || (coverObj instanceof String && ((String) coverObj).isBlank())) {
                return "No cover image set for this book.";
            }
            return (String) coverObj;
        } catch (Exception e) {
            return client.formatWryterioError("getting book cover", e);
        }
    }

    @Tool(name = "ChatDM_get_wryterio_book_plan", description = """
            Read the book plan (outline document) for a Wryterio book.
            The book plan is a markdown document with logline, characters, Save the Cat structure, chapter outlines, etc.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String getWryterioBookPlan(String wryterioToken, String bookId) {
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> book = client.objectMapper().readValue(response.getBody(), new TypeReference<>() {});
            Object planObj = book.get("bookPlan");
            if (planObj == null || (planObj instanceof String && ((String) planObj).isBlank())) {
                return "No book plan set for this book.";
            }
            return (String) planObj;
        } catch (Exception e) {
            return client.formatWryterioError("getting book plan", e);
        }
    }

    @Tool(name = "ChatDM_update_wryterio_book_plan", description = """
            Update the book plan (outline document) for a Wryterio book.
            Pass the full plan content as markdown. Can include logline, characters, Save the Cat structure, chapter outlines, etc.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            - bookPlan: Required. The full book plan content (markdown)
            """)
    public String updateWryterioBookPlan(String wryterioToken, String bookId, String bookPlan) {
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (bookPlan == null) return "Error: bookPlan is required.";
        if (!WryterioContentGuard.isMeaningfulContent(bookPlan)) return "Error: bookPlan must not be a placeholder (e.g. \"null\").";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";

        try {
            String body = client.objectMapper().writeValueAsString(Map.of("bookPlan", bookPlan));
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.PATCH, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return "Book plan updated.";
        } catch (Exception e) {
            return client.formatWryterioError("updating book plan", e);
        }
    }

    @Tool(name = "ChatDM_get_wryterio_plot_timeline", description = """
            Read the plot timeline (Juonen aikajana, Save the Cat structure) for a Wryterio book.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String getWryterioPlotTimeline(String wryterioToken, String bookId) {
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId + "/plot-timeline";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            List<Map<String, Object>> points = client.objectMapper().readValue(response.getBody(), new TypeReference<>() {});
            if (points == null || points.isEmpty()) return "No plot timeline points. Plot timeline is empty.";

            StringBuilder sb = new StringBuilder("Plot timeline (Save the Cat):\n");
            for (Map<String, Object> p : points) {
                String name = String.valueOf(p.getOrDefault("name", ""));
                String description = String.valueOf(p.getOrDefault("description", ""));
                if ("null".equals(description)) description = "";
                int pctStart = ((Number) p.getOrDefault("percentageStart", 0)).intValue();
                int pctEnd = ((Number) p.getOrDefault("percentageEnd", 0)).intValue();
                Object actObj = p.getOrDefault("act", 0);
                String actLabel;
                if (actObj instanceof Number) {
                    int act = ((Number) actObj).intValue();
                    actLabel = act == 0 ? "Custom" : "Act " + act;
                } else {
                    actLabel = String.valueOf(actObj);
                }
                String notes = String.valueOf(p.getOrDefault("notes", ""));
                if ("null".equals(notes)) notes = "";
                sb.append("  - ").append(name).append(" (").append(actLabel).append(", ").append(pctStart).append("-").append(pctEnd).append("%)\n");
                if (!description.isEmpty()) sb.append("    ").append(description.length() > 100 ? description.substring(0, 100) + "..." : description).append("\n");
                if (!notes.isEmpty()) sb.append("    Notes: ").append(notes.length() > 80 ? notes.substring(0, 80) + "..." : notes).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return client.formatWryterioError("getting plot timeline", e);
        }
    }

    @Tool(name = "ChatDM_update_wryterio_plot_timeline", description = """
            Replace the plot timeline (Juonen aikajana) for a Wryterio book. Uses Save the Cat structure.
            Pass plotTimeline as JSON array. Each point: { name, description?, percentageStart, percentageEnd, act, notes? }
            act: 0 (Custom), 1 (Act 1), 2 (Act 2A), 3 (Act 2B), 4 (Act 3), or custom string (e.g. "Prologi")
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            - plotTimelineJson: Required. JSON array of plot points, e.g. [{"name":"Opening Image","description":"...","percentageStart":0,"percentageEnd":5,"act":1}]
            """)
    public String updateWryterioPlotTimeline(String wryterioToken, String bookId, String plotTimelineJson) {
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (plotTimelineJson == null || plotTimelineJson.isBlank()) return "Error: plotTimelineJson is required.";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";

        try {
            Object parsed = client.objectMapper().readValue(plotTimelineJson.trim(), Object.class);
            String body = client.objectMapper().writeValueAsString(Map.of("plotTimeline", parsed));
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId + "/plot-timeline";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> result = client.objectMapper().readValue(response.getBody() != null ? response.getBody() : "{}", new TypeReference<>() {});
            int count = ((Number) result.getOrDefault("count", 0)).intValue();
            return "Plot timeline updated. " + count + " point(s).";
        } catch (Exception e) {
            return client.formatWryterioError("updating plot timeline", e);
        }
    }
}
