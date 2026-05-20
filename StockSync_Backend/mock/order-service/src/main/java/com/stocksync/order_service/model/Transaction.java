package com.stocksync.order_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    // FIX: Explicitly set the column type to BIGINT
    @Column(columnDefinition = "BIGINT")
    private Long productId;

    @Column(nullable = false)
    private String userId;

    private int quantity;

    // --- NEW FINANCIAL FIELDS ---
    @Column(nullable = false)
    private double pricePerUnit; // Price per unit at the time of transaction

    @Column(nullable = false)
    private double totalAmount; // Calculated total amount (quantity * pricePerUnit)
    // ----------------------------

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime transactionDate;


    public Transaction(
            Long productId,
            int quantity,
            double pricePerUnit, // <-- NEW PARAMETER
            TransactionType type,
            String userId
    ) {
        this.productId = productId;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit; // Set price
        this.totalAmount = quantity * pricePerUnit; // Calculate total amount
        this.type = type;
        this.userId = userId;
    }

    public Transaction(Long productId, Integer quantity, TransactionType transactionType) {
    }


    // Lifecycle Callback
    @PrePersist
    protected void onCreate() {
        this.transactionDate = LocalDateTime.now();
        // Default status is set here, ensuring every new record has a timestamp and initial status.
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }
}