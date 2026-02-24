package fi.celssi.chatdm.service;

import fi.celssi.chatdm.model.MatchInfo;
import fi.celssi.chatdm.model.ResourceInfo;
import fi.celssi.chatdm.model.SearchResult;
import fi.celssi.chatdm.util.PdfTextCache;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for performing advanced PDF search operations with fuzzy matching,
 * regex support, relevance scoring, and parallel processing.
 */
@Service
public class PdfSearchEngine {

    private static final int DEFAULT_CONTEXT_LENGTH = 150;
    private static final int MAX_MATCHES_PER_PAGE = 3;

    private final PdfTextCache pdfTextCache;
    private final ExecutorService executorService;
    private final EnglishAnalyzer analyzer;

    public PdfSearchEngine(PdfTextCache pdfTextCache) {
        this.pdfTextCache = pdfTextCache;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.analyzer = new EnglishAnalyzer();
    }

    @PreDestroy
    public void cleanup() {
        executorService.shutdown();
        analyzer.close();
    }

    /**
     * Performs a parallel search across multiple PDF resources.
     *
     * @param resourcesToSearch Collection of resources to search
     * @param query             Search query or regex pattern
     * @param maxResults        Maximum number of results to return
     * @param useRegex          Whether to treat query as regex
     * @return Sorted list of search results by relevance
     */
    public List<SearchResult> performParallelSearch(
            Collection<Map.Entry<String, ResourceInfo>> resourcesToSearch,
            String query,
            int maxResults,
            boolean useRegex) {

        List<Future<List<SearchResult>>> futures = new ArrayList<>();

        // Submit parallel search tasks
        for (Map.Entry<String, ResourceInfo> entry : resourcesToSearch) {
            futures.add(executorService.submit(() -> {
                try {
                    return searchInPdf(entry.getValue(), query, maxResults * 2, useRegex);
                } catch (Exception e) {
                    System.err.println("Error searching " + entry.getValue().name + ": " + e.getMessage());
                    return new ArrayList<>();
                }
            }));
        }

        // Collect all results
        List<SearchResult> allResults = new ArrayList<>();
        for (Future<List<SearchResult>> future : futures) {
            try {
                allResults.addAll(future.get(30, TimeUnit.SECONDS));
            } catch (Exception e) {
                System.err.println("Error collecting search results: " + e.getMessage());
            }
        }

        // Sort by relevance score and limit results
        Collections.sort(allResults);
        return allResults.stream().limit(maxResults).collect(Collectors.toList());
    }

    /**
     * Searches within a single PDF resource.
     */
    private List<SearchResult> searchInPdf(
            ResourceInfo resource,
            String query,
            int maxPerResource,
            boolean useRegex) throws IOException {

        List<SearchResult> results = new ArrayList<>();
        int totalPages = pdfTextCache.getPageCount(resource.path);

        // Prepare search patterns
        Set<String> stemmedQueryTerms = new HashSet<>();
        Pattern regexPattern = null;

        if (useRegex) {
            try {
                regexPattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            } catch (Exception e) {
                throw new IOException("Invalid regex pattern: " + e.getMessage());
            }
        } else {
            stemmedQueryTerms = stemQuery(query);
        }

        // Search through pages
        for (int i = 1; i <= totalPages; i++) {
            String pageText = pdfTextCache.getPageText(resource.path, i);
            if (pageText == null) continue;

            List<MatchInfo> matches;
            if (useRegex) {
                matches = findRegexMatches(pageText, regexPattern);
            } else {
                matches = findFuzzyMatches(pageText, query, stemmedQueryTerms);
            }

            if (!matches.isEmpty()) {
                double score = calculateRelevanceScore(matches, pageText.length());
                String context = extractMultiMatchContext(pageText, matches);
                results.add(new SearchResult(resource.name, i, context, score, matches.size()));
            }

            if (results.size() >= maxPerResource) {
                break;
            }
        }

        return results;
    }

    /**
     * Stems query terms using Lucene's English analyzer for fuzzy matching.
     */
    private Set<String> stemQuery(String query) {
        Set<String> stems = new HashSet<>();
        try (TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(query))) {
            CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                stems.add(termAttr.toString());
            }
            tokenStream.end();
        } catch (IOException e) {
            stems.add(query.toLowerCase());
        }
        return stems;
    }

    /**
     * Finds regex pattern matches in text.
     */
    private List<MatchInfo> findRegexMatches(String text, Pattern pattern) {
        List<MatchInfo> matches = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find() && matches.size() < MAX_MATCHES_PER_PAGE) {
            matches.add(new MatchInfo(matcher.start(), matcher.end(), matcher.group()));
        }
        return matches;
    }

    /**
     * Finds fuzzy/stemmed matches in text.
     */
    private List<MatchInfo> findFuzzyMatches(String text, String originalQuery, Set<String> stemmedTerms) {
        List<MatchInfo> matches = new ArrayList<>();
        String textLower = text.toLowerCase();
        String queryLower = originalQuery.toLowerCase();

        // First, find exact matches
        int index = 0;
        while ((index = textLower.indexOf(queryLower, index)) != -1 && matches.size() < MAX_MATCHES_PER_PAGE) {
            matches.add(new MatchInfo(index, index + queryLower.length(),
                    text.substring(index, index + queryLower.length())));
            index += queryLower.length();
        }

        // Then find stemmed matches if we haven't reached the limit
        if (matches.size() < MAX_MATCHES_PER_PAGE) {
            try (TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(text))) {
                CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
                tokenStream.reset();
                int position = 0;
                while (tokenStream.incrementToken() && matches.size() < MAX_MATCHES_PER_PAGE) {
                    String term = termAttr.toString();
                    if (stemmedTerms.contains(term)) {
                        int termIndex = textLower.indexOf(term, position);
                        if (termIndex != -1) {
                            final int pos = termIndex;
                            boolean alreadyMatched = matches.stream().anyMatch(m ->
                                    pos >= m.getStart() && pos < m.getEnd());
                            if (!alreadyMatched) {
                                matches.add(new MatchInfo(termIndex, termIndex + term.length(),
                                        text.substring(termIndex, termIndex + term.length())));
                                position = termIndex + term.length();
                            }
                        }
                    }
                }
                tokenStream.end();
            } catch (IOException e) {
                // Ignore stemming errors
            }
        }

        return matches;
    }

    /**
     * Calculates relevance score based on match count, density, and position.
     */
    private double calculateRelevanceScore(List<MatchInfo> matches, int pageLength) {
        if (matches.isEmpty()) return 0.0;

        double matchCountScore = matches.size() * 10.0;
        double densityScore = (matches.size() * 100.0) / Math.max(pageLength, 1);

        double positionScore = 0.0;
        for (MatchInfo match : matches) {
            double normalizedPosition = 1.0 - ((double) match.getStart() / Math.max(pageLength, 1));
            positionScore += normalizedPosition * 5.0;
        }

        return matchCountScore + densityScore + positionScore;
    }

    /**
     * Extracts context for multiple matches with highlighting.
     */
    private String extractMultiMatchContext(String text, List<MatchInfo> matches) {
        if (matches.isEmpty()) {
            return text.substring(0, Math.min(DEFAULT_CONTEXT_LENGTH, text.length()));
        }

        List<MatchInfo> limitedMatches = matches.stream()
                .limit(MAX_MATCHES_PER_PAGE)
                .collect(Collectors.toList());

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < limitedMatches.size(); i++) {
            MatchInfo match = limitedMatches.get(i);
            int start = Math.max(0, match.getStart() - DEFAULT_CONTEXT_LENGTH / 2);
            int end = Math.min(text.length(), match.getEnd() + DEFAULT_CONTEXT_LENGTH / 2);

            String snippet = text.substring(start, end).replaceAll("\\s+", " ").trim();

            // Highlight the match
            int matchStartInSnippet = match.getStart() - start;
            int matchEndInSnippet = match.getEnd() - start;
            if (matchStartInSnippet >= 0 && matchEndInSnippet <= snippet.length()) {
                snippet = snippet.substring(0, matchStartInSnippet) +
                        "**" + snippet.substring(matchStartInSnippet, matchEndInSnippet) + "**" +
                        snippet.substring(matchEndInSnippet);
            }

            context.append(snippet);
            if (i < limitedMatches.size() - 1) {
                context.append(" [...] ");
            }
        }

        return context.toString();
    }
}
