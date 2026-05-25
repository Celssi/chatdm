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
import java.util.Set;

@Service
public class WryterioStoryElementTools {

    private final WryterioClient client;

    public WryterioStoryElementTools(WryterioClient client) {
        this.client = client;
    }

    @Tool(name = "ChatDM_list_wryterio_story_elements", description = """
            List story elements (characters, places, items) for a Wryterio book.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String listWryterioStoryElements(String wryterioToken, String bookId) {
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId + "/story-elements";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            List<Map<String, Object>> elements = client.objectMapper().readValue(response.getBody(), new TypeReference<>() {});
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
            return client.formatWryterioError("listing story elements", e);
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
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (name == null || name.isBlank()) return "Error: name is required.";
        if (type == null || !Set.of("character", "place", "item").contains(type.toLowerCase())) {
            return "Error: type must be one of: character, place, item.";
        }
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";

        try {
            String body = client.objectMapper().writeValueAsString(Map.of(
                    "name", name.trim(),
                    "description", description != null ? description : "",
                    "type", type.toLowerCase()
            ));
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId + "/story-elements";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            Map<String, Object> result = client.objectMapper().readValue(response.getBody(), new TypeReference<>() {});
            return String.format("Story element added: %s [%s] (id: %s)", result.get("name"), result.get("type"), result.get("id"));
        } catch (Exception e) {
            return client.formatWryterioError("adding story element", e);
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
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (elementId == null || elementId.isBlank()) return "Error: elementId is required.";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";
        if ((name == null || name.isBlank()) && description == null && (type == null || type.isBlank())) {
            return "Error: At least one of name, description, or type is required.";
        }
        if (type != null && !type.isBlank() && !Set.of("character", "place", "item").contains(type.toLowerCase())) {
            return "Error: type must be one of: character, place, item.";
        }

        try {
            Map<String, Object> updates = new LinkedHashMap<>();
            if (WryterioContentGuard.isMeaningfulContent(name)) updates.put("name", name);
            if (WryterioContentGuard.isMeaningfulContent(description)) updates.put("description", description);
            if (type != null && !type.isBlank()) updates.put("type", type.toLowerCase());
            if (updates.isEmpty()) {
                return "Error: No valid updates. Rejecting placeholder values (e.g. \"null\"). Pass real content or omit fields.";
            }
            String body = client.objectMapper().writeValueAsString(updates);

            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId + "/story-elements/" + elementId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return "Story element updated.";
        } catch (Exception e) {
            return client.formatWryterioError("updating story element", e);
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
        String token = client.resolveToken(wryterioToken);
        if (token == null) return "Error: Wryterio token required.";
        if (bookId == null || bookId.isBlank()) return "Error: bookId is required.";
        if (elementId == null || elementId.isBlank()) return "Error: elementId is required.";
        if (client.wryterioApiUrl() == null || client.wryterioApiUrl().isBlank()) return "Error: Wryterio API not configured.";

        try {
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId + "/story-elements/" + elementId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = client.restTemplate().exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return "Story element deleted.";
        } catch (Exception e) {
            return client.formatWryterioError("deleting story element", e);
        }
    }

    @Tool(name = "ChatDM_sync_wryterio_story_elements_to_cloud", description = """
            Fetch story elements (characters, places, items) from Wryterio and save as bios under the book in cloud storage.
            WHEN TO USE: To sync Wryterio story elements to cloud for use with ChatDM_load_book_bio and novel_character_dialogue_prompt. ChatDM_sync_wryterio_book_to_cloud calls this automatically.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            - bookName: Optional. Book name for cloud storage (defaults to book title from Wryterio)
            """)
    public String syncWryterioStoryElementsToCloud(String wryterioToken, String bookId, String bookName) {
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
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId + "/story-elements";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = client.restTemplate().exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }

            List<Map<String, Object>> elements = client.objectMapper().readValue(response.getBody(),
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
                    client.novelOracle().saveBookBio(targetBook, type, name, bio != null && !"null".equals(bio) ? bio : "");
                    count++;
                }
            }
            return String.format("Synced %d story elements (characters, places, items) to cloud for book '%s'.", count, targetBook);
        } catch (Exception e) {
            return client.formatWryterioError("syncing story elements", e);
        }
    }
}
