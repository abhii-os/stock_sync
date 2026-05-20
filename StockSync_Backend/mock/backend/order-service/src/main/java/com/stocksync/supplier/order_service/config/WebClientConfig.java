package com.stocksync.supplier.order_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${stock.service.base.url}")
    private String stockServiceBaseUrl;

    @Value("${product.service.base.url}")
    private String productServiceBaseUrl;

    // --- Stock Service WebClient ---
    @Bean
    public WebClient stockWebClient(WebClient.Builder builder) {
        System.out.println("Configuring Stock Service Client with base URL: " + stockServiceBaseUrl);

        return builder
                .baseUrl(stockServiceBaseUrl)
                .build();
    }

    // --- Product Service WebClient ---
    @Bean
    public WebClient productWebClient(WebClient.Builder builder) {
        System.out.println("Configuring Product Service Client with base URL: " + productServiceBaseUrl);

        return builder
                .baseUrl(productServiceBaseUrl)
                .build();
    }
}