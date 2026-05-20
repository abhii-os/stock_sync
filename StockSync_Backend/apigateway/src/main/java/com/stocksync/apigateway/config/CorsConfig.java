package com.stocksync.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    // The default value ensures local development still works if the property is missing.
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private List<String> allowedOrigins;

    /**
     * Defines a CorsWebFilter bean to handle Cross-Origin Resource Sharing (CORS)
     * configurations for all incoming requests in the reactive Spring WebFlux environment.
     * This is the recommended way to configure CORS in Spring Cloud Gateway.
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        // 1. Create a source for CORS configurations based on URL patterns
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 2. Define the specific CORS configuration
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (like cookies or authentication headers) to be sent
        config.setAllowCredentials(true);

        // --- ALLOWED ORIGINS ARE NOW EXTERNALIZED ---
        // Use the list injected from the configuration file
        config.setAllowedOrigins(allowedOrigins);

        // Define allowed methods (GET, POST, PUT, DELETE, etc.)
        // In production, consider explicitly listing only necessary methods instead of "*"
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Define allowed headers
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With","X-Auth-User-Id"));

        // Expose headers (optional, only needed if the frontend requires non-standard headers)
        config.setExposedHeaders(Arrays.asList("X-Custom-Header"));

        // Set max age for preflight requests (improves performance)
        config.setMaxAge(3600L);

        // 3. Register the configuration to apply to all paths ("/**")
        source.registerCorsConfiguration("/**", config);

        // 4. Return the filter
        return new CorsWebFilter(source);
    }
}
