package fi.celssi.chatdm.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

/**
 * CORS configuration and request logging for cloud profile - allows MCP clients (Cursor, Claude) to connect.
 */
@Configuration
@Profile("cloud")
public class WebConfig {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("ChatDM cloud profile active - MCP server ready");
    }

    @Bean
    public Filter mcpRequestLoggingFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                if (!(request instanceof HttpServletRequest http)) {
                    chain.doFilter(request, response);
                    return;
                }
                String path = http.getRequestURI();
                if (path.startsWith("/mcp") || path.startsWith("/sse")) {
                    log.info("MCP {} {} from {}", http.getMethod(), path, http.getRemoteAddr());
                }
                chain.doFilter(request, response);
            }
        };
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }
}
