package com.stocksync.stock_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "stock_transactions")
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The ID of the product this transaction relates to
    private String productId; 
    
    // The source/reason for the transaction (e.g., Order ID, Shipment ID)
    private String sourceId; 

    // The type of movement (e.g., INBOUND, OUTBOUND)
    @Enumerated(EnumType.STRING)
    private TransactionType type; 

    // Quantity changed (positive for INBOUND, negative for OUTBOUND)
    private Integer quantityChange; 

    // Timestamp of when the transaction occurred
    private Instant timestamp; 

    // Optional: Current stock level after this transaction was applied
    private Integer currentStockLevel; 

    // --- Constructor, Getters, and Setters below ---
    public StockTransaction() {
        this.timestamp = Instant.now();
    }

}

