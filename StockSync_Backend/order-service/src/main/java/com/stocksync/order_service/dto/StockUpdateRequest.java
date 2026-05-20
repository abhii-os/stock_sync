package com.stocksync.order_service.dto;

import com.stocksync.order_service.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequest {
    @NotNull
    private Integer quantity;
    private TransactionType transactionType;
}
