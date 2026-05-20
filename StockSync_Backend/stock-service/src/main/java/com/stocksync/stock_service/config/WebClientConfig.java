package com.stocksync.stock_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
@Configuration
public class WebClientConfig {
@Value("${order.service.base.url}")
private String orderServiceBaseUrl;

@Value("${product.service.base.url}")
private String productServiceBaseUrl;

        @Bean
        public WebClient orderWebClient(WebClient.Builder builder) {
            System.out.println("Configuring Order Service Client with base URL: " + orderServiceBaseUrl);

            return builder
                    .baseUrl(orderServiceBaseUrl)
                    .build();
        }

        @Bean
        public WebClient productWebClient(WebClient.Builder builder) {
            System.out.println("Configuring Product Service Client with base URL: " + productServiceBaseUrl);

            return builder
                    .baseUrl(productServiceBaseUrl)
                    .build();
        }
    }