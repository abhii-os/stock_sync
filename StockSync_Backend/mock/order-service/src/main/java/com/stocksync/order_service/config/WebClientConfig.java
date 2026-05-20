package com.stocksync.order_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);
    @Value("${stock.service.base.url}")
    private String stockServiceBaseUrl;

    @Value("${product.service.base.url}")
    private String productServiceBaseUrl;

    // --- Stock Service WebClient ---
    @Bean
    public WebClient stockWebClient(WebClient.Builder builder) {
        log.info("Configuring Stock Service Client with base URL: {}", stockServiceBaseUrl);

        return builder
                .baseUrl(stockServiceBaseUrl)
                .build();
    }

    // --- Product Service WebClient ---
    @Bean
    public WebClient productWebClient(WebClient.Builder builder) {
        log.info("Configuring Product Service Client with base URL: {}", productServiceBaseUrl);

        return builder
                .baseUrl(productServiceBaseUrl)
                .build();
    }
}