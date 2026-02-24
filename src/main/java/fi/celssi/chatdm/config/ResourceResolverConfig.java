package fi.celssi.chatdm.config;

import fi.celssi.chatdm.util.ResourceResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

/**
 * Configures ResourceResolver based on profile.
 * Local: uses classpath. Cloud: uses GCS with configurable base path.
 */
@Configuration
public class ResourceResolverConfig {

    @Bean
    public ResourceResolver resourceResolver(
            ResourceLoader resourceLoader,
            @Value("${chatdm.pdfs.base-path:}") String pdfsBasePath) {
        return new ResourceResolver() {
            @Override
            public Resource resolve(String path) throws IOException {
                if (path == null || path.isEmpty()) {
                    throw new IllegalArgumentException("Path cannot be null or empty");
                }
                String location;
                if (path.startsWith("gs://")) {
                    location = path;
                } else if (pdfsBasePath != null && !pdfsBasePath.isEmpty() && pdfsBasePath.startsWith("gs://")) {
                    location = pdfsBasePath.replaceAll("/$", "") + "/" + path.replaceFirst("^/", "");
                } else {
                    location = "classpath:" + path.replaceFirst("^/", "");
                }
                return resourceLoader.getResource(location);
            }

            @Override
            public boolean supports(String path) {
                return path != null && !path.isEmpty();
            }
        };
    }
}
