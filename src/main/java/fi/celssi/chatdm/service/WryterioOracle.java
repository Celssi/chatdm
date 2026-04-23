package fi.celssi.chatdm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.celssi.chatdm.util.WryterioTokenHolder;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
            WHEN TO USE: First step to discover books before syncing or fetching. Use bookId with ChatDM_sync_wryterio_book_to_cloud when user wants to load/save.
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
            return formatWryterioError("fetching Wryterio books", e);
        }
    }

    @Tool(name = "ChatDM_get_wryterio_book", description = """
            Fetch book metadata (title, author, description, chapter count) from Wryterio.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String getWryterioBook(String wryterioToken, String bookId) {
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> book = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            return String.format("Book: %s (id: %s)\nAuthor: %s\nDescription: %s\nChapters: %d",
                    book.get("title"), book.get("id"), book.getOrDefault("author", ""), book.getOrDefault("description", ""),
                    ((Number) book.getOrDefault("chapterCount", 0)).intValue());
        } catch (Exception e) {
            return formatWryterioError("fetching book", e);
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
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";
        if ((title == null || title.isBlank()) && author == null && description == null) {
            return "Error: At least one of title, author, or description is required.";
        }

        try {
            Map<String, Object> updates = new HashMap<>();
            if (title != null && !title.isBlank()) updates.put("title", title.trim());
            if (author != null) updates.put("author", author.isBlank() ? null : author);
            if (description != null) updates.put("description", description.isBlank() ? null : description);
            String body = objectMapper.writeValueAsString(updates);

            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return "Book metadata updated.";
        } catch (Exception e) {
            return formatWryterioError("updating book", e);
        }
    }

    @Tool(name = "ChatDM_create_wryterio_book", description = """
            Create a new book in Wryterio.
            Parameters:
            - wryterioToken: Optional. API token
            - title: Required. Book title
            """)
    public String createWryterioBook(String wryterioToken, String title) {
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (title == null || title.isBlank()) return "Error: title is required.";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";

        try {
            String body = objectMapper.writeValueAsString(Map.of("title", title.trim()));
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> result = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            return String.format("Book created: %s (id: %s)", result.get("title"), result.get("id"));
        } catch (Exception e) {
            return formatWryterioError("creating book", e);
        }
    }

    @Tool(name = "ChatDM_update_wryterio_chapter", description = """
            Partially update one chapter. The Wryterio API merges only the fields you supply; all other fields stay unchanged.
            WHEN TO USE — summary / synopsis / metadata only: pass bookId, chapterIndex, and description (and/or title, targetWordCount). Do NOT pass content. Existing chapter body text is preserved.
            WHEN TO USE — replace chapter text: pass content (markdown) with the full replacement text. Only then is the body rewritten.
            DANGER: If you pass content as empty, the literal string "null", or other junk, you can wipe or corrupt the stored chapter. When you only mean to set description, leave content unset / do not pass it.
            RELATED: ChatDM_get_wryterio_chapter (metadata + content length) or ChatDM_fetch_wryterio_chapter (full markdown) before editing if unsure.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            - chapterIndex: Required. 1-based chapter number
            - title: Optional. New chapter title
            - content: Optional. Full chapter body as markdown. Omit entirely when updating only title, description, or targetWordCount.
            - description: Optional. Chapter summary / synopsis; safe to set without passing content
            - targetWordCount: Optional. Target word count (e.g. "3000")
            """)
    public String updateWryterioChapter(String wryterioToken, String bookId, int chapterIndex, String title, String content, String description, String targetWordCount) {
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (chapterIndex < 1) return "Error: chapterIndex must be >= 1.";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";
        if ((title == null || title.isBlank()) && (content == null || content.isBlank()) && (description == null || description.isBlank()) && (targetWordCount == null || targetWordCount.isBlank())) {
            return "Error: At least one of title, content, description, or targetWordCount is required.";
        }

        try {
            Map<String, Object> updates = new LinkedHashMap<>();
            if (isMeaningfulContent(title)) updates.put("title", title);
            if (isMeaningfulContent(content)) updates.put("content", content);
            if (isMeaningfulContent(description)) updates.put("description", description);
            if (isMeaningfulContent(targetWordCount)) updates.put("targetWordCount", targetWordCount);
            if (updates.isEmpty()) {
                return "Error: No valid updates. Rejecting placeholder values (e.g. \"null\"). Pass real content or omit content when updating only metadata.";
            }
            String body = objectMapper.writeValueAsString(updates);

            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/chapters/" + chapterIndex;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return "Chapter " + chapterIndex + " updated.";
        } catch (Exception e) {
            return formatWryterioError("updating chapter", e);
        }
    }

    @Tool(name = "ChatDM_add_wryterio_chapter", description = """
            Add a new chapter to a Wryterio book.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            - title: Required. Chapter title
            - content: Optional. Chapter content (markdown)
            - description: Optional. Chapter description
            - targetWordCount: Optional. Target word count (e.g. "3000")
            """)
    public String addWryterioChapter(String wryterioToken, String bookId, String title, String content, String description, String targetWordCount) {
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";
        if (!isMeaningfulContent(title)) {
            return "Error: title is required and must not be a placeholder (e.g. \"null\").";
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("title", title.trim());
            if (isMeaningfulContent(content)) payload.put("content", content);
            if (isMeaningfulContent(description)) payload.put("description", description);
            if (isMeaningfulContent(targetWordCount)) payload.put("targetWordCount", targetWordCount);
            String body = objectMapper.writeValueAsString(payload);

            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/chapters";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> result = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            return String.format("Chapter added: %s (index: %s)", result.get("title"), result.get("index"));
        } catch (Exception e) {
            return formatWryterioError("adding chapter", e);
        }
    }

    @Tool(name = "ChatDM_list_wryterio_story_elements", description = """
            List story elements (characters, places, items) for a Wryterio book.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String listWryterioStoryElements(String wryterioToken, String bookId) {
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/story-elements";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            List<Map<String, Object>> elements = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            if (elements == null || elements.isEmpty()) return "No story elements found.";

            StringBuilder sb = new StringBuilder("Wryterio story elements:\n");
            for (Map<String, Object> el : elements) {
                sb.append("  - ").append(el.get("name")).append(" [").append(el.get("type")).append("] (id: ").append(el.get("id")).append(")\n");
                String bio = String.valueOf(el.getOrDefault("bio", ""));
                if (!bio.isEmpty() && !"null".equals(bio)) {
                    sb.append("    ").append(bio.length() > 80 ? bio.substring(0, 80) + "..." : bio).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return formatWryterioError("listing story elements", e);
        }
    }

    @Tool(name = "ChatDM_add_wryterio_story_element", description = """
            Add a story element (character, place, item) to a Wryterio book.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            - name: Required. Element name
            - description: Optional. Bio/description
            - type: Required. One of: character, place, item
            """)
    public String addWryterioStoryElement(String wryterioToken, String bookId, String name, String description, String type) {
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (name == null || name.isBlank()) return "Error: name is required.";
        if (type == null || !java.util.Set.of("character", "place", "item").contains(type.toLowerCase())) {
            return "Error: type must be one of: character, place, item.";
        }
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";

        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "name", name.trim(),
                    "description", description != null ? description : "",
                    "type", type.toLowerCase()
            ));
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/story-elements";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> result = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            return String.format("Story element added: %s [%s] (id: %s)", result.get("name"), result.get("type"), result.get("id"));
        } catch (Exception e) {
            return formatWryterioError("adding story element", e);
        }
    }

    @Tool(name = "ChatDM_update_wryterio_story_element", description = """
            Update a story element in Wryterio.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            - elementId: Required. Story element ID
            - name: Optional. New name
            - description: Optional. New description
            - type: Optional. New type (character, place, item)
            """)
    public String updateWryterioStoryElement(String wryterioToken, String bookId, String elementId, String name, String description, String type) {
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (elementId == null || elementId.isBlank()) return "Error: elementId is required.";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";
        if ((name == null || name.isBlank()) && description == null && (type == null || type.isBlank())) {
            return "Error: At least one of name, description, or type is required.";
        }
        if (type != null && !type.isBlank() && !java.util.Set.of("character", "place", "item").contains(type.toLowerCase())) {
            return "Error: type must be one of: character, place, item.";
        }

        try {
            Map<String, Object> updates = new LinkedHashMap<>();
            if (isMeaningfulContent(name)) updates.put("name", name);
            if (isMeaningfulContent(description)) updates.put("description", description);
            if (type != null && !type.isBlank()) updates.put("type", type.toLowerCase());
            if (updates.isEmpty()) {
                return "Error: No valid updates. Rejecting placeholder values (e.g. \"null\"). Pass real content or omit fields.";
            }
            String body = objectMapper.writeValueAsString(updates);

            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/story-elements/" + elementId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return "Story element updated.";
        } catch (Exception e) {
            return formatWryterioError("updating story element", e);
        }
    }

    @Tool(name = "ChatDM_delete_wryterio_story_element", description = """
            Delete a story element from a Wryterio book.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            - elementId: Required. Story element ID
            """)
    public String deleteWryterioStoryElement(String wryterioToken, String bookId, String elementId) {
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (elementId == null || elementId.isBlank()) return "Error: elementId is required.";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/story-elements/" + elementId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return "Story element deleted.";
        } catch (Exception e) {
            return formatWryterioError("deleting story element", e);
        }
    }

    @Tool(name = "ChatDM_get_wryterio_plot_timeline", description = """
            Read the plot timeline (Juonen aikajana, Save the Cat structure) for a Wryterio book.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String getWryterioPlotTimeline(String wryterioToken, String bookId) {
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/plot-timeline";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            List<Map<String, Object>> points = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
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
            return formatWryterioError("getting plot timeline", e);
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
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (plotTimelineJson == null || plotTimelineJson.isBlank()) return "Error: plotTimelineJson is required.";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";

        try {
            Object parsed = objectMapper.readValue(plotTimelineJson.trim(), Object.class);
            String body = objectMapper.writeValueAsString(java.util.Map.of("plotTimeline", parsed));
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/plot-timeline";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> result = objectMapper.readValue(response.getBody() != null ? response.getBody() : "{}", new TypeReference<>() {});
            int count = ((Number) result.getOrDefault("count", 0)).intValue();
            return "Plot timeline updated. " + count + " point(s).";
        } catch (Exception e) {
            return formatWryterioError("updating plot timeline", e);
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
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> book = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object planObj = book.get("bookPlan");
            if (planObj == null || (planObj instanceof String && ((String) planObj).isBlank())) {
                return "No book plan set for this book.";
            }
            return (String) planObj;
        } catch (Exception e) {
            return formatWryterioError("getting book plan", e);
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
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (bookPlan == null) return "Error: bookPlan is required.";
        if (!isMeaningfulContent(bookPlan)) return "Error: bookPlan must not be a placeholder (e.g. \"null\").";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";

        try {
            String body = objectMapper.writeValueAsString(Map.of("bookPlan", bookPlan));
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return "Book plan updated.";
        } catch (Exception e) {
            return formatWryterioError("updating book plan", e);
        }
    }

    @Tool(name = "ChatDM_get_wryterio_book_cover", description = """
            Get the book cover image URL for a Wryterio book.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String getWryterioBookCover(String wryterioToken, String bookId) {
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> book = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object coverObj = book.get("coverImageUrl");
            if (coverObj == null || (coverObj instanceof String && ((String) coverObj).isBlank())) {
                return "No cover image set for this book.";
            }
            return (String) coverObj;
        } catch (Exception e) {
            return formatWryterioError("getting book cover", e);
        }
    }

    /** Rejects placeholder/null values that would overwrite real content with garbage. */
    private static boolean isMeaningfulContent(String s) {
        if (s == null || s.isBlank()) return false;
        String t = s.trim();
        if (t.length() < 2) return false;
        String lower = t.toLowerCase();
        if ("null".equals(lower) || "undefined".equals(lower)) return false;
        return true;
    }

    /** Extracts human-readable error from Wryterio API response body { "error": "..." }. */
    private String parseErrorFromBody(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            Map<String, Object> map = objectMapper.readValue(body, new TypeReference<>() {});
            Object err = map.get("error");
            return err != null ? String.valueOf(err).trim() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /** Formats API/network errors with API error message when available. */
    private String formatWryterioError(String context, Exception e) {
        if (e instanceof RestClientResponseException ex) {
            String apiError = parseErrorFromBody(ex.getResponseBodyAsString());
            return "Error " + context + ": " + (apiError != null && !apiError.isBlank() ? apiError : ex.getStatusText());
        }
        return "Error " + context + ": " + e.getMessage();
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
            return formatWryterioError("searching Wryterio books", e);
        }
    }

    @Tool(name = "ChatDM_fetch_wryterio_book", description = """
            Fetch full book markdown from Wryterio. Returns raw markdown only—does NOT save to storage.
            WHEN TO USE: When you need the content in-memory only (e.g. to display or process). Do NOT use for "load book" or "sync"—use ChatDM_sync_wryterio_book_to_cloud instead.
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
            return formatWryterioError("fetching book", e);
        }
    }

    @Tool(name = "ChatDM_list_wryterio_chapters", description = """
            List chapters: 1-based index, title, and per-chapter target word count (from API; descriptions are not listed here—use ChatDM_get_wryterio_chapter for description).
            WHEN TO USE: Resolve chapterIndex before fetch/update. ChatDM_fetch_wryterio_chapter = full text; ChatDM_get_wryterio_chapter = title, description, lengths.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String listWryterioChapters(String wryterioToken, String bookId) {
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
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/chapters";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }

            List<Map<String, Object>> chapters = objectMapper.readValue(response.getBody(),
                    new TypeReference<>() {});
            if (chapters == null || chapters.isEmpty()) {
                return "No chapters found for this book.";
            }

            StringBuilder sb = new StringBuilder("Wryterio chapters:\n");
            for (Map<String, Object> c : chapters) {
                int index = ((Number) c.getOrDefault("index", 0)).intValue();
                String title = String.valueOf(c.getOrDefault("title", ""));
                String targetWordCount = String.valueOf(c.getOrDefault("targetWordCount", ""));
                if ("null".equals(targetWordCount)) targetWordCount = "";
                sb.append("  ").append(index).append(". ").append(title);
                if (!targetWordCount.isEmpty()) sb.append(" [target: ").append(targetWordCount).append(" words]");
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return formatWryterioError("listing chapters", e);
        }
    }

    @Tool(name = "ChatDM_fetch_wryterio_chapter", description = """
            Fetch a single chapter from Wryterio by 1-based index. Returns markdown CONTENT only (chapter text).
            CONTRAST: ChatDM_get_wryterio_chapter returns METADATA (title, description, targetWordCount), not full content.
            RELATED: ChatDM_list_wryterio_chapters first to get chapter indices.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            - chapterIndex: Required. 1-based chapter number
            """)
    public String fetchWryterioChapter(String wryterioToken, String bookId, int chapterIndex) {
        String token = resolveToken(wryterioToken);
        if (token == null) {
            return "Error: Wryterio token required.";
        }
        if (bookId == null || bookId.isBlank()) {
            return "Error: bookId is required.";
        }
        if (chapterIndex < 1) {
            return "Error: chapterIndex must be >= 1.";
        }
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) {
            return "Error: Wryterio API not configured.";
        }

        try {
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/chapters/" + chapterIndex;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return response.getBody() != null ? response.getBody() : "";
        } catch (Exception e) {
            return formatWryterioError("fetching chapter", e);
        }
    }

    @Tool(name = "ChatDM_get_wryterio_chapter", description = """
            Read chapter METADATA: title, description, targetWordCount, and content length. Does NOT return the full chapter body.
            CONTRAST: ChatDM_fetch_wryterio_chapter returns the full chapter CONTENT (markdown).
            Use before writing a new description: then call ChatDM_update_wryterio_chapter with only description (omit content) to avoid touching the chapter text.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            - chapterIndex: Required. 1-based chapter number
            """)
    public String getWryterioChapter(String wryterioToken, String bookId, int chapterIndex) {
        String token = resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (chapterIndex < 1) return "Error: chapterIndex must be >= 1.";
        if (wryterioApiUrl == null || wryterioApiUrl.isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/chapters/" + chapterIndex + "?format=json";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> ch = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            String title = String.valueOf(ch.getOrDefault("title", ""));
            String description = String.valueOf(ch.getOrDefault("description", ""));
            if ("null".equals(description)) description = "";
            String targetWordCount = String.valueOf(ch.getOrDefault("targetWordCount", ""));
            if ("null".equals(targetWordCount)) targetWordCount = "";
            int contentLen = 0;
            Object contentObj = ch.get("content");
            if (contentObj != null) {
                String content = contentObj.toString();
                contentLen = content.length();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Chapter ").append(chapterIndex).append(": ").append(title).append("\n");
            sb.append("Description: ").append(description.isEmpty() ? "(none)" : description).append("\n");
            sb.append("Target word count: ").append(targetWordCount.isEmpty() ? "(none)" : targetWordCount).append("\n");
            sb.append("Content length: ").append(contentLen).append(" chars");
            return sb.toString();
        } catch (Exception e) {
            return formatWryterioError("getting chapter", e);
        }
    }

    @Tool(name = "ChatDM_sync_wryterio_book_to_cloud", description = """
            Fetch book from Wryterio and SAVE all data to cloud storage: chapters, metadata, story elements (characters, places, items).
            WHEN TO USE: User says "load book", "sync from Wryterio", "lataa kirja", or wants to save a Wryterio book for later. Do NOT use ChatDM_fetch_wryterio_book for loading—it only returns content, does not save.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID (from ChatDM_list_wryterio_books)
            """)
    public String syncWryterioBookToCloud(String wryterioToken, String bookId) {
        String markdown = fetchWryterioBook(wryterioToken, bookId);
        if (markdown.startsWith("Error:")) {
            return markdown;
        }
        String bookResult = novelOracle.syncBook(markdown, null);
        if (bookResult.startsWith("Error:")) {
            return bookResult;
        }
        String bookTitle = extractTitleFromMarkdown(markdown);
        String elementsResult = syncWryterioCharactersToCloud(wryterioToken, bookId, bookTitle);
        if (elementsResult.startsWith("Error:")) {
            return bookResult + " Story elements: " + elementsResult;
        }
        int elementsCount = 0;
        try {
            int idx = elementsResult.indexOf("Synced ");
            if (idx >= 0) {
                int end = elementsResult.indexOf(" story", idx);
                if (end > idx) {
                    elementsCount = Integer.parseInt(elementsResult.substring(idx + 7, end).trim());
                }
            }
        } catch (NumberFormatException ignored) {
        }
        if (elementsCount > 0) {
            return bookResult + " " + elementsCount + " story elements synced.";
        }
        return bookResult;
    }

    private String extractTitleFromMarkdown(String markdown) {
        if (markdown == null) return null;
        int start = markdown.indexOf("# ");
        if (start == -1) return null;
        start += 2;
        int end = markdown.indexOf("\n", start);
        if (end == -1) end = markdown.length();
        return markdown.substring(start, end).trim();
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
            return formatWryterioError("fetching characters", e);
        }
    }

    @Tool(name = "ChatDM_sync_wryterio_characters_to_cloud", description = """
            Fetch story elements (characters, places, items) from Wryterio and save as bios under the book in cloud storage.
            WHEN TO USE: To sync Wryterio story elements to cloud for use with ChatDM_load_book_bio and novel_character_dialogue_prompt. ChatDM_sync_wryterio_book_to_cloud calls this automatically.
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
            String url = wryterioApiUrl.replaceAll("/$", "") + "/api/books/" + bookId + "/story-elements";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }

            List<Map<String, Object>> elements = objectMapper.readValue(response.getBody(),
                    new TypeReference<>() {});
            if (elements == null || elements.isEmpty()) {
                return "No story elements to sync.";
            }

            String targetBook = bookName != null && !bookName.isBlank() ? bookName.trim() : bookId;
            int count = 0;
            for (Map<String, Object> el : elements) {
                String name = String.valueOf(el.getOrDefault("name", ""));
                String bio = String.valueOf(el.getOrDefault("bio", el.getOrDefault("description", "")));
                String type = String.valueOf(el.getOrDefault("type", "character")).toLowerCase();
                if (!"character".equals(type) && !"place".equals(type) && !"item".equals(type)) {
                    type = "character";
                }
                if (!name.isEmpty() && !"null".equals(name)) {
                    novelOracle.saveBookBio(targetBook, type, name, bio != null && !"null".equals(bio) ? bio : "");
                    count++;
                }
            }
            return String.format("Synced %d story elements (characters, places, items) to cloud for book '%s'.", count, targetBook);
        } catch (Exception e) {
            return formatWryterioError("syncing story elements", e);
        }
    }

    private String resolveToken(String paramToken) {
        if (paramToken != null && !paramToken.isBlank()) {
            return paramToken.trim();
        }
        return WryterioTokenHolder.get();
    }
}
