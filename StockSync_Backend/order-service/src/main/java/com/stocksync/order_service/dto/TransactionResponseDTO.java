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
        double pricePerUnit,
        double totalAmount,
        TransactionType type,
        OrderStatus status,
        LocalDateTime transactionDate
) {
    public static TransactionResponseDTO fromEntity(Transaction t) {
        return new TransactionResponseDTO(
                t.getOrderId(),
                t.getProductId(),
                t.getUserId(),
                t.getQuantity(),
                t.getPricePerUnit(),
                t.getTotalAmount(),
                t.getType(),
                t.getStatus(),
                t.getTransactionDate()
        );
    }
}