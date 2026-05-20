package com.StockSync.product.microservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Sent from Stock Service to Product Service for final inventory modification
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductQuantityUpdateDTO {
    // The quantity to add or subtract (e.g., -5 for SELL, +10 for PURCHASE)
    private Integer quantityChange;

    // For logging/context in the Product Service ("SELL" or "PURCHASE")
    private String transactionType;
}