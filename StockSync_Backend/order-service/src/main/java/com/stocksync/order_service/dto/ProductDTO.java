package com.stocksync.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private Integer currentStock;
    private Integer lowQuantityThreshold;
    private String description;
    private BigDecimal price;
    private boolean active;
    private Long categoryId;
    private Long supplierId;
}
