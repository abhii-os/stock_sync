package com.stocksync.supplier.order_service.dto;

import com.stocksync.supplier.order_service.exception.InvalidTransactionException;
import jakarta.validation.constraints.NotNull;
// Note: You will need to create the InvalidTransactionRequestException class

public record TransactionRequestDTO(
        @NotNull
        Long productId,
        @NotNull
        int quantity
) {
    public TransactionRequestDTO {
        // 1. Corrected check for productId: must not be null AND must be positive.
        if (productId == null || productId <= 0) {
            // 2. Throw a custom RuntimeException directly for clean, unchecked errors.
            throw new InvalidTransactionException("Product ID must be provided and positive.");
        }

        if (quantity <= 0) {
            // 3. Throw a custom RuntimeException directly.
            throw new InvalidTransactionException("Quantity must be positive.");
        }
    }
}