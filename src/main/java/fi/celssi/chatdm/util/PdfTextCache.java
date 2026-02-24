package fi.celssi.chatdm.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe cache for PDF page text extraction.
 * Caches extracted text to avoid re-parsing PDFs on subsequent searches.
 * Uses Caffeine for bounded cache size (max 500 pages, 1 hour TTL).
 */
@Component
public class PdfTextCache {

    private static final int MAX_CACHE_PAGES = 500;
    private static final int CACHE_EXPIRE_MINUTES = 60;

    private final ResourceResolver resourceResolver;
    private final Cache<String, String> pageCache;
    private final Cache<String, Integer> pageCountCache;

    public PdfTextCache(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
        this.pageCache = Caffeine.newBuilder()
                .maximumSize(MAX_CACHE_PAGES)
                .expireAfterAccess(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .build();
        this.pageCountCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Retrieves page text from cache or extracts it from PDF if not cached.
     *
     * @param resourcePath The resource path to the PDF (classpath or gs://)
     * @param pageNumber   The page number (1-indexed)
     * @return The page text, or null if the page doesn't exist
     * @throws IOException if PDF reading fails
     */
    public String getPageText(String resourcePath, int pageNumber) throws IOException {
        String cacheKey = resourcePath + ":" + pageNumber;
        String cached = pageCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        Resource pdfResource = resourceResolver.resolve(resourcePath);
        try (PDDocument document = Loader.loadPDF(pdfResource.getInputStream().readAllBytes())) {
            if (pageNumber > document.getNumberOfPages()) {
                return null;
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(pageNumber);
            stripper.setEndPage(pageNumber);
            String text = stripper.getText(document);
            pageCache.put(cacheKey, text);
            return text;
        }
    }

    /**
     * Gets the total number of pages in a PDF.
     *
     * @param resourcePath The resource path to the PDF
     * @return The number of pages
     * @throws IOException if PDF reading fails
     */
    public int getPageCount(String resourcePath) throws IOException {
        Integer cached = pageCountCache.getIfPresent(resourcePath);
        if (cached != null) {
            return cached;
        }

        Resource pdfResource = resourceResolver.resolve(resourcePath);
        try (PDDocument document = Loader.loadPDF(pdfResource.getInputStream().readAllBytes())) {
            int count = document.getNumberOfPages();
            pageCountCache.put(resourcePath, count);
            return count;
        }
    }

    /**
     * Clears the cache for a specific resource.
     *
     * @param resourcePath The resource path to clear
     */
    public void clearResource(String resourcePath) {
        pageCache.asMap().keySet().removeIf(k -> k.startsWith(resourcePath + ":"));
        pageCountCache.invalidate(resourcePath);
    }

    /**
     * Clears the entire cache.
     */
    public void clearAll() {
        pageCache.invalidateAll();
        pageCountCache.invalidateAll();
    }
}
