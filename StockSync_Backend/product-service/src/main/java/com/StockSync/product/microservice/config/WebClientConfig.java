package com.StockSync.product.microservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient orderServiceClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8090")  // Order service port
                .build();
    }


        @Bean
        public WebClient supplierWebClient() {
            return WebClient.builder()
                    .baseUrl("http://localhost:5050/api/v1/suppliers")
                    .build();
        }
    }
