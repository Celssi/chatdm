package fi.celssi.chatdm.service.shared;

public final class ToolResponseHelper {

    private ToolResponseHelper() {
    }

    public static String error(String message) {
        return "Error: " + message;
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return error(fieldName + " is required");
        }
        return null;
    }
}
