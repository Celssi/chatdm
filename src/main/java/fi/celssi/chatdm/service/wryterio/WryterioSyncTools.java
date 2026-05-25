package fi.celssi.chatdm.service.wryterio;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class WryterioSyncTools {

    private final WryterioClient client;
    private final WryterioStoryElementTools storyElementTools;

    public WryterioSyncTools(WryterioClient client, WryterioStoryElementTools storyElementTools) {
        this.client = client;
        this.storyElementTools = storyElementTools;
    }

    @Tool(name = "ChatDM_fetch_wryterio_book", description = """
            Fetch full book markdown from Wryterio. Returns raw markdown only—does NOT save to storage.
            WHEN TO USE: When you need the content in-memory only (e.g. to display or process). Do NOT use for "load book" or "sync"—use ChatDM_sync_wryterio_book_to_cloud instead.
            Parameters:
            - wryterioToken: Optional. API token
            - bookId: Required. Wryterio book ID
            """)
    public String fetchWryterioBook(String wryterioToken, String bookId) {
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
            String url = client.wryterioApiUrl().replaceAll("/$", "") + "/api/books/" + bookId + "/export";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = client.restTemplate().exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error: Wryterio API returned " + response.getStatusCode();
            }
            return response.getBody() != null ? response.getBody() : "";
        } catch (Exception e) {
            return client.formatWryterioError("fetching book", e);
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
        String bookResult = client.novelOracle().syncBook(markdown, null);
        if (bookResult.startsWith("Error:")) {
            return bookResult;
        }
        String bookTitle = client.extractTitleFromMarkdown(markdown);
        String elementsResult = storyElementTools.syncWryterioStoryElementsToCloud(wryterioToken, bookId, bookTitle);
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
}
