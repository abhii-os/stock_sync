package com.stocksync.stock_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductDTO(
        Long id,
        String sku,
        String name,

        // Map the incoming JSON field "currentStock" to the DTO field "stockLevel"
        @JsonProperty("currentStock")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Integer stockLevel,

        // Map the incoming JSON field "lowQuantityThreshold" to the DTO field name
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Integer lowQuantityThreshold
) {}