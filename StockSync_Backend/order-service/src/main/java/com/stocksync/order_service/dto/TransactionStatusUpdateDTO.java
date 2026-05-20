package com.stocksync.order_service.dto;
import com.stocksync.order_service.exception.InvalidTransactionException;
import com.stocksync.order_service.model.OrderStatus;

public record TransactionStatusUpdateDTO(
    OrderStatus newStatus
) {
    public TransactionStatusUpdateDTO {
        if (newStatus != OrderStatus.COMPLETED && newStatus != OrderStatus.CANCELLED) {
            try {
                throw new InvalidTransactionException("Status update can only be to COMPLETED or CANCELLED.");
            } catch (InvalidTransactionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}