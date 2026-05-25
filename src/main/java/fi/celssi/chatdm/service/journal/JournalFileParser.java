package fi.celssi.chatdm.service.journal;

import java.time.format.DateTimeFormatter;

public final class JournalFileParser {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final String CHARACTERS = "characters";
    public static final String ADVENTURES = "adventures";
    public static final String NPCS = "npcs";
    public static final String CAMPAIGNS = "campaigns";
    public static final String LOCATIONS = "locations";

    private JournalFileParser() {
    }

    public static String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_]", "_").toLowerCase();
    }

    public static String extractValue(String content, String prefix) {
        int start = content.indexOf(prefix);
        if (start == -1) return "Unknown";

        start += prefix.length();
        int end = content.indexOf("\n", start);
        if (end == -1) end = content.length();

        String value = content.substring(start, end).trim();
        return value.isEmpty() ? "Unknown" : value;
    }

    public static String extractMarkdownTitle(String content) {
        int start = content.indexOf("# ");
        if (start == -1) return "Untitled";

        start += 2;
        int end = content.indexOf("\n", start);
        if (end == -1) end = content.length();

        return content.substring(start, end).trim();
    }

    public static String extractCampaignName(String content) {
        String markdownCampaign = extractValue(content, "**Campaign:**");
        return markdownCampaign.equals("Unknown") ? null : markdownCampaign;
    }
}
