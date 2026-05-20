package com.stocksync.stock_service.service;

import com.stocksync.stock_service.dto.ProductDTO;
import com.stocksync.stock_service.exception.ProductServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Service
    public class ProductServiceClient {
        private final WebClient webClient;

        public ProductServiceClient(@Qualifier("productWebClient") WebClient productWebClient) {
            this.webClient = productWebClient;
        }

        private final Function<ClientResponse, Mono<? extends Throwable>> handleProductError = response ->
            response.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new ProductServiceException("Status " + response.statusCode() + ": " + errorBody)));


        public List<ProductDTO> getProductsForAlerts() {
             return webClient.get()
                     .uri(uriBuilder -> uriBuilder
                             .queryParam("fields", "id,sku,name,stockLevel")
                             .build())
                     .accept(MediaType.APPLICATION_JSON)
                     .retrieve()
                     .onStatus(HttpStatusCode::isError, handleProductError)
                     .bodyToFlux(ProductDTO.class)
                     .collectList()
                     .block();
        }
    }