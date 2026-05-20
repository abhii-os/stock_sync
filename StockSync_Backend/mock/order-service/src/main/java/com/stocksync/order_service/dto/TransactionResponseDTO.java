package com.stocksync.order_service.dto;

import com.stocksync.order_service.model.Transaction;
import com.stocksync.order_service.model.TransactionType;
import com.stocksync.order_service.model.OrderStatus;

import java.time.LocalDateTime;

public record TransactionResponseDTO(
        Long orderId,
        Long productId,
        String userId,
        int quantity,
        double pricePerUnit,    // <-- ADDED: Price of one unit at transaction time
        double totalAmount,     // <-- ADDED: Calculated total cost (quantity * pricePerUnit)
        TransactionType type,
        OrderStatus status,
        LocalDateTime transactionDate
) {
    /**
     * Maps a Transaction entity to a TransactionResponseDTO record.
     */
    public static TransactionResponseDTO fromEntity(Transaction t) {
        return new TransactionResponseDTO(
                t.getOrderId(),
                t.getProductId(),
                t.getUserId(),
                t.getQuantity(),
                t.getPricePerUnit(), // <-- Mapped from Transaction entity
                t.getTotalAmount(),  // <-- Mapped from Transaction entity
                t.getType(),
                t.getStatus(),
                t.getTransactionDate()
        );
    }
}