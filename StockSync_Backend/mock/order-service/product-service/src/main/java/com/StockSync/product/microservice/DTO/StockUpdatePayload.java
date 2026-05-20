package com.StockSync.product.microservice.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdatePayload {
    
    private Long productId;
    private Long orderId;
    private int quantity;
    // Must be "SELL" or "PURCHASE"
    private String transactionType; 
}