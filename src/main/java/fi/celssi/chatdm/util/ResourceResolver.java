package fi.celssi.chatdm.util;

import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Resolves resource paths to Spring Resources.
 * Supports classpath (local) and GCS (cloud) backends.
 */
public interface ResourceResolver {

    /**
     * Resolves a path to a Resource.
     *
     * @param path Relative path (e.g. "pdfs/lotr/core_rulebook.pdf") or full GCS URI
     * @return The resource, or null if not found
     */
    Resource resolve(String path) throws IOException;

    /**
     * Checks if this resolver supports the given path.
     */
    boolean supports(String path);
}
