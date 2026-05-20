package com.inventory.stock_service.entity;

// Separate Enum for clarity
public enum TransactionType {
    INBOUND,  // Stock added (e.g., from a new shipment)
    OUTBOUND  // Stock removed (e.g., due to a customer order)
}
