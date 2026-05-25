package fi.celssi.chatdm.service.wryterio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.celssi.chatdm.service.NovelOracle;
import fi.celssi.chatdm.util.WryterioTokenHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WryterioClient {

    private final NovelOracle novelOracle;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${chatdm.wryterio.api-url:}")
    private String wryterioApiUrl;

    public WryterioClient(NovelOracle novelOracle, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.novelOracle = novelOracle;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    NovelOracle novelOracle() {
        return novelOracle;
    }

    RestTemplate restTemplate() {
        return restTemplate;
    }

    ObjectMapper objectMapper() {
        return objectMapper;
    }

    String wryterioApiUrl() {
        return wryterioApiUrl;
    }

    String resolveToken(String paramToken) {
        if (paramToken != null && !paramToken.isBlank()) {
            return paramToken.trim();
        }
        return WryterioTokenHolder.get();
    }

    String parseErrorFromBody(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            Map<String, Object> map = objectMapper.readValue(body, new TypeReference<>() {});
            Object err = map.get("error");
            return err != null ? String.valueOf(err).trim() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    String formatWryterioError(String context, Exception e) {
        if (e instanceof RestClientResponseException ex) {
            String apiError = parseErrorFromBody(ex.getResponseBodyAsString());
            return "Error " + context + ": " + (apiError != null && !apiError.isBlank() ? apiError : ex.getStatusText());
        }
        return "Error " + context + ": " + e.getMessage();
    }

    String extractTitleFromMarkdown(String markdown) {
        if (markdown == null) return null;
        int start = markdown.indexOf("# ");
        if (start == -1) return null;
        start += 2;
        int end = markdown.indexOf("\n", start);
        if (end == -1) end = markdown.length();
        return markdown.substring(start, end).trim();
    }
}
