package com.stocksync.order_service.service;

import com.stocksync.order_service.dto.ProductDTO;
import com.stocksync.order_service.dto.StockUpdateRequest;
import com.stocksync.order_service.exception.InsufficientStockException;
import com.stocksync.order_service.model.Transaction;
import com.stocksync.order_service.model.TransactionType; // Import for TransactionType
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
@Slf4j
@Service
public class ProductServiceClient {

    private final WebClient webClient;
    private final String productServiceUrl;

    public ProductServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${product.service.base.url}") String productServiceUrl) {
        this.productServiceUrl = productServiceUrl;
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

    // ✨ NEW METHOD: To reverse the stock adjustment (Rollback logic)
    public Mono<String> reverseProductStockUpdate(Transaction transaction) {

        // 1. Determine the REVERSED TransactionType
        // If it was a SELL, the reversal is like a PURCHASE (stock increases).
        // If it was a PURCHASE, the reversal is like a SELL (stock decreases).
        TransactionType reversedType =
                (transaction.getType() == TransactionType.SELL) ? TransactionType.PURCHASE : TransactionType.SELL;

        log.info("Reversing stock for Product ID {}. Original Type: {}, Reversal Type: {}", transaction.getProductId(), transaction.getType(), reversedType);

        // 2. Send the reversal request to the Product Microservice
        // The Product Microservice's PATCH endpoint will receive the REVERSED_TYPE and act accordingly.
        return webClient.patch()
                .uri("/products/{id}/stock", transaction.getProductId())
                .bodyValue(new StockUpdateRequest(
                        transaction.getQuantity(),
                        reversedType // Use the reversed type to signal the opposite stock action
                ))
                .retrieve()
                .bodyToMono(String.class);
    }
}