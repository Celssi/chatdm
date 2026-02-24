package fi.celssi.chatdm.util;

/**
 * Thread-local holder for Wryterio API token extracted from X-Wryterio-Token request header.
 * Used when token is passed via MCP config headers instead of tool parameter.
 */
public final class WryterioTokenHolder {

    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();

    public static void set(String token) {
        TOKEN.set(token);
    }

    public static String get() {
        return TOKEN.get();
    }

    public static void clear() {
        TOKEN.remove();
    }

    private WryterioTokenHolder() {
    }
}
