package com.stocksync.order_service.service;

import com.stocksync.order_service.dto.ProductDTO;
import com.stocksync.order_service.dto.StockUpdateRequest;
import com.stocksync.order_service.exception.InsufficientStockException;
import com.stocksync.order_service.model.Transaction;
import com.stocksync.order_service.model.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
@Slf4j
@Service
public class ProductServiceClient {

    private final WebClient webClient;

    public ProductServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${product.service.base.url}") String productServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(productServiceUrl).build();
    }

    public Mono<ProductDTO> getProductDetails(Long productId) {
        return webClient.get()
                .uri("/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductDTO.class);
    }

    public Mono<Boolean> checkStockAvailability(Long productId, int quantity) {
        return webClient.get()
                .uri("/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .map(product -> {
                    if (product.getCurrentStock() < quantity) {
                        throw new InsufficientStockException(
                                String.format("Insufficient stock for product %d. Required: %d, Available: %d",
                                        productId, quantity, product.getCurrentStock())
                        );
                    }
                    return true;
                });
    }

    // Existing method for standard stock adjustment (COMPLETED status)
    public Mono<String> updateProductStock(Transaction transaction) {
        return webClient.patch()
                .uri("/products/{id}/stock", transaction.getProductId())
                .bodyValue(new StockUpdateRequest(
                        transaction.getQuantity(),
                        transaction.getType()
                ))
                .retrieve()
                .bodyToMono(String.class);
    }


    public Mono<String> reverseProductStockUpdate(Transaction transaction) {

        TransactionType reversedType =
                (transaction.getType() == TransactionType.SELL) ? TransactionType.PURCHASE : TransactionType.SELL;

        log.info("Reversing stock for Product ID {}. Original Type: {}, Reversal Type: {}", transaction.getProductId(), transaction.getType(), reversedType);
        return webClient.patch()
                .uri("/products/{id}/stock", transaction.getProductId())
                .bodyValue(new StockUpdateRequest(
                        transaction.getQuantity(),
                        reversedType
                ))
                .retrieve()
                .bodyToMono(String.class);
    }
}