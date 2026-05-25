package fi.celssi.chatdm.service;

import fi.celssi.chatdm.service.wryterio.WryterioBookTools;
import fi.celssi.chatdm.service.wryterio.WryterioChapterTools;
import fi.celssi.chatdm.util.WryterioTokenHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@TestPropertySource(properties = {"chatdm.wryterio.api-url=https://api.test.wryterio.local"})
class WryterioOracleTest {

    private static final String API_BASE = "https://api.test.wryterio.local";
    private static final String TOKEN = "wryterio_test_token";

    @Autowired
    private WryterioBookTools wryterioBookTools;

    @Autowired
    private WryterioChapterTools wryterioChapterTools;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        WryterioTokenHolder.set(TOKEN);
    }

    @AfterEach
    void tearDown() {
        WryterioTokenHolder.clear();
        if (mockServer != null) {
            mockServer.verify();
        }
    }

    @Test
    void errorExtraction_returnsApiErrorFromResponseBody() {
        mockServer
                .expect(requestTo(API_BASE + "/api/books"))
                .andRespond(
                        withStatus(HttpStatus.BAD_REQUEST)
                                .body("{\"error\":\"Invalid token or expired\"}")
                                .contentType(MediaType.APPLICATION_JSON));

        String result = wryterioBookTools.listWryterioBooks(null);

        assertTrue(result.contains("Invalid token or expired"), "Should surface API error message: " + result);
        assertTrue(result.startsWith("Error fetching Wryterio books:"), "Should use error context: " + result);
    }

    @Test
    void errorExtraction_fallsBackToStatusTextWhenNoErrorField() {
        mockServer
                .expect(requestTo(API_BASE + "/api/books"))
                .andRespond(
                        withStatus(HttpStatus.FORBIDDEN)
                                .body("{\"message\":\"Access denied\"}")
                                .contentType(MediaType.APPLICATION_JSON));

        String result = wryterioBookTools.listWryterioBooks(null);

        assertTrue(result.contains("Error fetching Wryterio books:"), "Should have error context: " + result);
    }

    @Test
    void errorExtraction_handles500WithErrorField() {
        mockServer
                .expect(requestTo(API_BASE + "/api/books"))
                .andRespond(
                        withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("{\"error\":\"Database connection failed\"}")
                                .contentType(MediaType.APPLICATION_JSON));

        String result = wryterioBookTools.listWryterioBooks(null);

        assertTrue(result.contains("Database connection failed"), "Should surface server error: " + result);
    }

    @Test
    void createBook_usesObjectMapperForJsonSerialization() {
        mockServer
                .expect(requestTo(API_BASE + "/api/books"))
                .andRespond(
                        withSuccess()
                                .body("{\"id\":\"book-1\",\"title\":\"My Title\"}")
                                .contentType(MediaType.APPLICATION_JSON));

        String result = wryterioBookTools.createWryterioBook(null, "My Title");

        assertFalse(result.startsWith("Error:"), "Should succeed: " + result);
        assertTrue(result.contains("book-1"), "Should contain created book id: " + result);
    }

    @Test
    void updateChapter_rejectsContentWithoutConfirmOverwrite() {
        String result = wryterioChapterTools.updateWryterioChapter(
                null, "book-1", 1, null, "New chapter body text", null, null, null);

        assertTrue(result.contains("confirmOverwrite=true"), "Should require explicit overwrite: " + result);
    }

    @Test
    void updateChapter_allowsMetadataOnlyWithoutContent() {
        mockServer
                .expect(requestTo(API_BASE + "/api/books/book-1/chapters/1"))
                .andRespond(withSuccess().body("{}").contentType(MediaType.APPLICATION_JSON));

        String result = wryterioChapterTools.updateWryterioChapter(
                null, "book-1", 1, null, null, "A synopsis", null, null);

        assertFalse(result.startsWith("Error:"), "Metadata-only update should succeed: " + result);
    }
}
