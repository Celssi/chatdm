package fi.celssi.chatdm.service.shared;

/**
 * Validates Wryterio API field values to prevent accidental content wipes.
 */
public final class WryterioContentGuard {

    private WryterioContentGuard() {
    }

    /** Rejects placeholder/null values that would overwrite real content with garbage. */
    public static boolean isMeaningfulContent(String s) {
        if (s == null || s.isBlank()) return false;
        String t = s.trim();
        if (t.length() < 2) return false;
        String lower = t.toLowerCase();
        if ("null".equals(lower) || "undefined".equals(lower)) return false;
        return true;
    }

    public static boolean isConfirmOverwrite(String value) {
        if (value == null) return false;
        String v = value.trim().toLowerCase();
        return "true".equals(v) || "yes".equals(v) || "1".equals(v);
    }
}
