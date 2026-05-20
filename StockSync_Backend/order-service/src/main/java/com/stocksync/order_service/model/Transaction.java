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

    @Column(columnDefinition = "BIGINT")
    private Long productId;

    @Column(nullable = false)
    private String userId;

    private int quantity;

    @Column(nullable = false)
    private double pricePerUnit;

    @Column(nullable = false)
    private double totalAmount;


    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime transactionDate;


    public Transaction(
            Long productId,
            int quantity,
            double pricePerUnit,
            TransactionType type,
            String userId
    ) {
        this.productId = productId;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.totalAmount = quantity * pricePerUnit;
        this.type = type;
        this.userId = userId;
    }


    @PrePersist
    protected void onCreate() {
        this.transactionDate = LocalDateTime.now();
        // Default status is set here, ensuring every new record has a timestamp and initial status.
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }
}