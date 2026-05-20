package com.inventory.stock_service.dto;

public record StockAlertDTO(
            Long productId,
            String productName,
            int currentStock,
            int lowQuantityThreshold
    ) {}