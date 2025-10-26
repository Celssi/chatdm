package fi.celssi.chatdm.model;

/**
 * Represents information about a text match found during search.
 */
public class MatchInfo {
    private final int start;
    private final int end;
    private final String text;

    public MatchInfo(int start, int end, String text) {
        this.start = start;
        this.end = end;
        this.text = text;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getText() {
        return text;
    }
}
