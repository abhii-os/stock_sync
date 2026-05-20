package com.stocksync.stock_service.dto;

import com.stocksync.stock_service.entity.StockTransaction;
import com.stocksync.stock_service.entity.TransactionType;
import java.time.Instant;

public record StockTransactionResponseDTO(
        // Use String for IDs to match common microservice practices
        String productId,
        String sourceId, // e.g., Order ID, Shipment ID - used for tracing
        TransactionType type,
        int quantityChange, // The amount of stock added or removed
        int currentStockLevel, // The stock level after this transaction
        Instant timestamp // When the transaction occurred
) {
    /**
     * Converts the StockTransaction JPA Entity into a response DTO.
     */
    public static StockTransactionResponseDTO fromEntity(StockTransaction t) {
        return new StockTransactionResponseDTO(
                // Correctly mapping fields from the StockTransaction entity:
                t.getProductId(),
                t.getSourceId(),
                t.getType(),
                t.getQuantityChange(),
                t.getCurrentStockLevel(),
                t.getTimestamp()
        );
    }
}