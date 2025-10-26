package fi.celssi.chatdm.model;

/**
 * Represents a search result from a PDF resource.
 */
public class SearchResult implements Comparable<SearchResult> {
    public String resourceName;
    public int pageNumber;
    public String context;
    public double score;
    public int matchCount;

    public SearchResult(String resourceName, int pageNumber, String context, double score, int matchCount) {
        this.resourceName = resourceName;
        this.pageNumber = pageNumber;
        this.context = context;
        this.score = score;
        this.matchCount = matchCount;
    }

    @Override
    public String toString() {
        return String.format("[%s - Page %d | %d match(es) | Score: %.2f]\n...%s...",
            resourceName, pageNumber, matchCount, score, context);
    }

    @Override
    public int compareTo(SearchResult other) {
        return Double.compare(other.score, this.score); // Higher score first
    }
}
