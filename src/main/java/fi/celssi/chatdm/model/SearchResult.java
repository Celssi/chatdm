package fi.celssi.chatdm.model;

/**
 * Represents a search result from a PDF resource.
 */
public class SearchResult {
    public String resourceName;
    public int pageNumber;
    public String context;

    public SearchResult(String resourceName, int pageNumber, String context) {
        this.resourceName = resourceName;
        this.pageNumber = pageNumber;
        this.context = context;
    }

    @Override
    public String toString() {
        return String.format("[%s - Page %d] ...%s...", resourceName, pageNumber, context);
    }
}
