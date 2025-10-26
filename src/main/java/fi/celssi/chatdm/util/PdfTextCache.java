package fi.celssi.chatdm.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe cache for PDF page text extraction.
 * Caches extracted text to avoid re-parsing PDFs on subsequent searches.
 */
@Component
public class PdfTextCache {

    private final Map<String, Map<Integer, String>> cache = new ConcurrentHashMap<>();

    /**
     * Retrieves page text from cache or extracts it from PDF if not cached.
     *
     * @param resourcePath The classpath resource path to the PDF
     * @param pageNumber   The page number (1-indexed)
     * @return The page text, or null if the page doesn't exist
     * @throws IOException if PDF reading fails
     */
    public String getPageText(String resourcePath, int pageNumber) throws IOException {
        Map<Integer, String> pageCache = cache.computeIfAbsent(resourcePath, k -> new ConcurrentHashMap<>());

        return pageCache.computeIfAbsent(pageNumber, page -> {
            try {
                ClassPathResource pdfResource = new ClassPathResource(resourcePath);
                try (PDDocument document = Loader.loadPDF(pdfResource.getInputStream().readAllBytes())) {
                    if (pageNumber > document.getNumberOfPages()) {
                        return null;
                    }

                    PDFTextStripper stripper = new PDFTextStripper();
                    stripper.setStartPage(pageNumber);
                    stripper.setEndPage(pageNumber);
                    return stripper.getText(document);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Gets the total number of pages in a PDF.
     *
     * @param resourcePath The classpath resource path to the PDF
     * @return The number of pages
     * @throws IOException if PDF reading fails
     */
    public int getPageCount(String resourcePath) throws IOException {
        ClassPathResource pdfResource = new ClassPathResource(resourcePath);
        try (PDDocument document = Loader.loadPDF(pdfResource.getInputStream().readAllBytes())) {
            return document.getNumberOfPages();
        }
    }

    /**
     * Clears the cache for a specific resource.
     *
     * @param resourcePath The resource path to clear
     */
    public void clearResource(String resourcePath) {
        cache.remove(resourcePath);
    }

    /**
     * Clears the entire cache.
     */
    public void clearAll() {
        cache.clear();
    }
}
