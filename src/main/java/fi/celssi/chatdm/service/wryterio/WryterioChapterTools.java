package fi.celssi.chatdm.service.wryterio;

import com.fasterxml.jackson.core.type.TypeReference;
import fi.celssi.chatdm.service.shared.WryterioContentGuard;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WryterioChapterTools {

    private final WryterioClient client;

    public WryterioChapterTools(WryterioClient client) {
        this.client = client;
    }

    @Tool(name = "ChatDM_list_wryterio_chapters", description = """
            List chapters: 1-based index, title, and per-chapter target word count (from API; descriptions are not listed here—use ChatDM_get_wryterio_chapter for description).
            WHEN TO USE: Resolve chapterIndex before fetch/update. ChatDM_fetch_wryterio_chapter = full text; ChatDM_get_wryterio_chapter = title, description, lengths.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String listWryterioChapters(String wryterioToken, String bookId) {
        String token = client.resolveToken(wryterioToken);
        if (token == null) {
            return "Error: Wryterio token required.";
        }
        if (bookId == null || bookId.isBlank()) {
            return "Error: bookId is required.";
        }
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) {
            return "Error: Wryterio API not configured.";
        }

        try {
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId + "/chapters";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = client.restTemplate().exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }

            List<Map<String, Object>> chapters = client.objectMapper().readValue(response.getBody(),
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
            return client.formatWryterioError("listing chapters", e);
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
        String token = client.resolveToken(wryterioToken);
        if (token == null) {
            return "Error: Wryterio token required.";
        }
        if (bookId == null || bookId.isBlank()) {
            return "Error: bookId is required.";
        }
        if (chapterIndex < 1) {
            return "Error: chapterIndex must be >= 1.";
        }
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) {
            return "Error: Wryterio API not configured.";
        }

        try {
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId + "/chapters/" + chapterIndex;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = client.restTemplate().exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return response.getBody() != null ? response.getBody() : "";
        } catch (Exception e) {
            return client.formatWryterioError("fetching chapter", e);
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
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (chapterIndex < 1) return "Error: chapterIndex must be >= 1.";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId + "/chapters/" + chapterIndex + "?format=json";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> ch = client.objectMapper().readValue(response.getBody(), new TypeReference<>() {});
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
            return client.formatWryterioError("getting chapter", e);
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
            - confirmOverwrite: Optional. Set to true when replacing full chapter content (required if content is passed)
            """)
    public String updateWryterioChapter(String wryterioToken, String bookId, int chapterIndex, String title, String content, String description, String targetWordCount, String confirmOverwrite) {
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (chapterIndex < 1) return "Error: chapterIndex must be >= 1.";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";
        if ((title == null || title.isBlank()) && (content == null || content.isBlank()) && (description == null || description.isBlank()) && (targetWordCount == null || targetWordCount.isBlank())) {
            return "Error: At least one of title, content, description, or targetWordCount is required.";
        }

        try {
            Map<String, Object> updates = new LinkedHashMap<>();
            if (WryterioContentGuard.isMeaningfulContent(title)) updates.put("title", title);
            if (WryterioContentGuard.isMeaningfulContent(content)) {
                if (!WryterioContentGuard.isConfirmOverwrite(confirmOverwrite)) {
                    return "Error: Replacing chapter content requires confirmOverwrite=true. Omit content when updating only metadata.";
                }
                updates.put("content", content);
            }
            if (WryterioContentGuard.isMeaningfulContent(description)) updates.put("description", description);
            if (WryterioContentGuard.isMeaningfulContent(targetWordCount)) updates.put("targetWordCount", targetWordCount);
            if (updates.isEmpty()) {
                return "Error: No valid updates. Rejecting placeholder values (e.g. \"null\"). Pass real content or omit content when updating only metadata.";
            }
            String body = client.objectMapper().writeValueAsString(updates);

            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId + "/chapters/" + chapterIndex;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return "Chapter " + chapterIndex + " updated.";
        } catch (Exception e) {
            return client.formatWryterioError("updating chapter", e);
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
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";
        if (!WryterioContentGuard.isMeaningfulContent(title)) {
            return "Error: title is required and must not be a placeholder (e.g. \"null\").";
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("title", title.trim());
            if (WryterioContentGuard.isMeaningfulContent(content)) payload.put("content", content);
            if (WryterioContentGuard.isMeaningfulContent(description)) payload.put("description", description);
            if (WryterioContentGuard.isMeaningfulContent(targetWordCount)) payload.put("targetWordCount", targetWordCount);
            String body = client.objectMapper().writeValueAsString(payload);

            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId + "/chapters";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> result = client.objectMapper().readValue(response.getBody(), new TypeReference<>() {});
            return String.format("Chapter added: %s (index: %s)", result.get("title"), result.get("index"));
        } catch (Exception e) {
            return client.formatWryterioError("adding chapter", e);
        }
    }
}
